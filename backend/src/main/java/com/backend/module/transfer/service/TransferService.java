package com.backend.module.transfer.service;

import com.backend.module.auditlog.annotation.Auditable;
import com.backend.exception.BusinessException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.module.product.entity.Product;
import com.backend.module.product.repository.ProductRepository;
import com.backend.module.shared.enums.MovementType;
import com.backend.module.shared.enums.TransferStatus;
import com.backend.module.stock.entity.Stock;
import com.backend.module.stock.repository.StockRepository;
import com.backend.module.stockmovement.entity.StockMovement;
import com.backend.module.stockmovement.repository.StockMovementRepository;
import com.backend.module.transfer.dto.CancelTransferRequest;
import com.backend.module.transfer.dto.CreateTransferRequest;
import com.backend.module.transfer.dto.ReceiveTransferRequest;
import com.backend.module.transfer.dto.TransferResponse;
import com.backend.module.transfer.entity.Transfer;
import com.backend.module.transfer.entity.TransferLine;
import com.backend.module.transfer.repository.TransferRepository;
import com.backend.module.user.entity.User;
import com.backend.module.user.repository.UserRepository;
import com.backend.module.warehouse.entity.Warehouse;
import com.backend.module.warehouse.repository.WarehouseRepository;
import com.backend.module.zone.entity.Zone;
import com.backend.module.zone.repository.ZoneRepository;
import com.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TransferService {

    private final TransferRepository transferRepository;
    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final ZoneRepository zoneRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;
    private final com.backend.module.stock.service.CapacityService capacityService;

    // ─────────────────────────────────────────────────────────────
    // LECTURE — transferts sortants d'un entrepôt (gestionnaire source)
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<TransferResponse> findOutgoing(
            Long warehouseId, TransferStatus status, Pageable pageable) {
        getWarehouse(warehouseId);
        return transferRepository
                .findBySourceWarehouseWithFilters(warehouseId, status, pageable)
                .map(this::toResponse);
    }

    // ─────────────────────────────────────────────────────────────
    // LECTURE — transferts entrants vers un entrepôt (gestionnaire cible)
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<TransferResponse> findIncoming(
            Long warehouseId, TransferStatus status, Pageable pageable) {
        getWarehouse(warehouseId);
        return transferRepository
                .findByTargetWarehouseWithFilters(warehouseId, status, pageable)
                .map(this::toResponse);
    }

    // ─────────────────────────────────────────────────────────────
    // LECTURE — vue admin globale
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<TransferResponse> findAll(
            TransferStatus status, Long warehouseId, Pageable pageable) {
        return transferRepository
                .findAllWithFilters(status, warehouseId, pageable)
                .map(this::toResponse);
    }

    // ─────────────────────────────────────────────────────────────
    // LECTURE — détail
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public TransferResponse findById(Long transferId) {
        return toResponse(getTransfer(transferId));
    }

    // ─────────────────────────────────────────────────────────────
    // LECTURE — compteur VALIDATED entrants (badge gestionnaire cible)
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public long countIncomingValidated(Long warehouseId) {
        return transferRepository.countByTargetWarehouseIdAndStatus(
                warehouseId, TransferStatus.VALIDATED);
    }

    // ─────────────────────────────────────────────────────────────
    // CRÉATION — Gestionnaire source initie le transfert (PENDING)
    //
    // Règles :
    //   1. L'entrepôt cible doit être différent de la source.
    //   2. Le stock doit exister et être suffisant pour chaque ligne.
    //   3. Pas de décrément immédiat — l'admin valide d'abord.
    // ─────────────────────────────────────────────────────────────

    @Auditable(module = "transfer", action = "CREATE", entity = "Transfer",
               description = "Transfert inter-entrepôts créé")
    public TransferResponse create(Long sourceWarehouseId, CreateTransferRequest req) {
        Warehouse sourceWarehouse = getWarehouse(sourceWarehouseId);
        Warehouse targetWarehouse = getWarehouse(req.getTargetWarehouseId());

        if (sourceWarehouseId.equals(req.getTargetWarehouseId())) {
            throw new BusinessException("L'entrepôt source et l'entrepôt cible doivent être différents");
        }

        User creator = currentUserEntity();

        Transfer transfer = Transfer.builder()
                .transferNumber(generateTransferNumber())
                .status(TransferStatus.PENDING)
                .note(req.getNote())
                .sourceWarehouse(sourceWarehouse)
                .targetWarehouse(targetWarehouse)
                .createdBy(creator)
                .build();

        for (CreateTransferRequest.TransferLineRequest lineReq : req.getLines()) {
            Product product = productRepository.findByIdWithDetails(lineReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Produit introuvable : " + lineReq.getProductId()));

            Zone sourceZone = getZone(lineReq.getSourceZoneId(), sourceWarehouseId);

            // Vérifier que le stock existe et est suffisant
            Stock stock = stockRepository
                    .findByProductIdAndWarehouseIdAndZoneId(
                            product.getId(), sourceWarehouseId, sourceZone.getId())
                    .orElseThrow(() -> new BusinessException(
                            "Aucun stock trouvé pour " + product.getName()
                            + " dans la zone " + sourceZone.getName()));

            if (stock.getQuantityAvailable() < lineReq.getQuantity()) {
                throw new BusinessException(
                        "Stock insuffisant pour " + product.getName()
                        + " — disponible : " + stock.getQuantityAvailable()
                        + ", demandé : " + lineReq.getQuantity());
            }

            TransferLine line = TransferLine.builder()
                    .transfer(transfer)
                    .product(product)
                    .sourceZone(sourceZone)
                    .quantity(lineReq.getQuantity())
                    .note(lineReq.getNote())
                    .build();

            transfer.getLines().add(line);
        }

        return toResponse(transferRepository.save(transfer));
    }

    // ─────────────────────────────────────────────────────────────
    // VALIDATION ADMIN — PENDING → VALIDATED
    // Décrémente quantityAvailable source + incrémente quantityInTransit
    // Génère StockMovement TRANSFER côté source
    // ─────────────────────────────────────────────────────────────

    @Auditable(module = "transfer", action = "VALIDATE", entity = "Transfer",
               description = "Transfert validé — stock source décrémenté")
    public TransferResponse validate(Long transferId) {
        Transfer transfer = getTransfer(transferId);
        User validator = currentUserEntity();

        if (transfer.getStatus() != TransferStatus.PENDING) {
            throw new BusinessException("Seuls les transferts PENDING peuvent être validés");
        }

        for (TransferLine line : transfer.getLines()) {
            Long sourceWarehouseId = transfer.getSourceWarehouse().getId();

            Stock sourceStock = stockRepository
                    .findByProductIdAndWarehouseIdAndZoneId(
                            line.getProduct().getId(), sourceWarehouseId, line.getSourceZone().getId())
                    .orElseThrow(() -> new BusinessException(
                            "Stock source introuvable pour " + line.getProduct().getName()));

            if (sourceStock.getQuantityAvailable() < line.getQuantity()) {
                throw new BusinessException(
                        "Stock insuffisant pour " + line.getProduct().getName()
                        + " lors de la validation — disponible : "
                        + sourceStock.getQuantityAvailable()
                        + ", demandé : " + line.getQuantity());
            }

            // Décrémenter disponible et incrémenter in-transit côté source
            sourceStock.setQuantityAvailable(sourceStock.getQuantityAvailable() - line.getQuantity());
            sourceStock.setQuantityInTransit(sourceStock.getQuantityInTransit() + line.getQuantity());
            stockRepository.save(sourceStock);

            // StockMovement TRANSFER côté source (sortie physique en transit)
            StockMovement movement = StockMovement.builder()
                    .product(line.getProduct())
                    .warehouse(transfer.getSourceWarehouse())
                    .zone(line.getSourceZone())
                    .quantity(line.getQuantity())
                    .movementType(MovementType.TRANSFER)
                    .referenceDoc(transfer.getTransferNumber())
                    .note("Transfert vers " + transfer.getTargetWarehouse().getName())
                    .createdBy(validator)
                    .build();
            stockMovementRepository.save(movement);
        }

        transfer.setStatus(TransferStatus.VALIDATED);
        transfer.setValidatedAt(LocalDateTime.now());
        transfer.setValidatedBy(validator);

        TransferResponse response = toResponse(transferRepository.save(transfer));

        // Source : quantityAvailable décrémentée → recalcul
        capacityService.recalculate(transfer.getSourceWarehouse());

        return response;
    }

    // ─────────────────────────────────────────────────────────────
    // RÉCEPTION — Gestionnaire cible confirme la réception (VALIDATED → RECEIVED)
    // Il choisit les zones cibles pour chaque ligne
    // Incrémente quantityAvailable cible + décrémente quantityInTransit source
    // Génère StockMovement ENTRY côté cible
    // ─────────────────────────────────────────────────────────────

    @Auditable(module = "transfer", action = "RECEIVE", entity = "Transfer",
               description = "Transfert reçu — stock cible incrémenté")
    public TransferResponse receive(Long targetWarehouseId, Long transferId,
                                    ReceiveTransferRequest req) {
        Transfer transfer = getTransfer(transferId);
        User receiver = currentUserEntity();

        if (!transfer.getTargetWarehouse().getId().equals(targetWarehouseId)) {
            throw new BusinessException("Ce transfert ne concerne pas votre entrepôt");
        }
        if (transfer.getStatus() != TransferStatus.VALIDATED) {
            throw new BusinessException("Seuls les transferts VALIDATED peuvent être réceptionnés");
        }

        // Construire une map lineId → targetZoneId depuis la requête
        Map<Long, Long> targetZoneByLineId = req.getLines().stream()
                .collect(Collectors.toMap(
                        ReceiveTransferRequest.ReceiveLineRequest::getTransferLineId,
                        ReceiveTransferRequest.ReceiveLineRequest::getTargetZoneId
                ));

        // Construire une map des lignes existantes par id
        Map<Long, TransferLine> linesById = transfer.getLines().stream()
                .collect(Collectors.toMap(TransferLine::getId, Function.identity()));

        // Vérifier que toutes les lignes du transfert sont couvertes
        for (TransferLine line : transfer.getLines()) {
            if (!targetZoneByLineId.containsKey(line.getId())) {
                throw new BusinessException(
                        "Zone cible manquante pour le produit : " + line.getProduct().getName());
            }
        }

        for (ReceiveTransferRequest.ReceiveLineRequest lineReq : req.getLines()) {
            TransferLine line = linesById.get(lineReq.getTransferLineId());
            if (line == null) {
                throw new ResourceNotFoundException(
                        "Ligne de transfert introuvable : " + lineReq.getTransferLineId());
            }

            Zone targetZone = getZone(lineReq.getTargetZoneId(), targetWarehouseId);

            // Mettre à jour la zone cible sur la ligne
            line.setTargetZone(targetZone);

            // Décrémenter quantityInTransit côté source
            stockRepository
                    .findByProductIdAndWarehouseIdAndZoneId(
                            line.getProduct().getId(),
                            transfer.getSourceWarehouse().getId(),
                            line.getSourceZone().getId())
                    .ifPresent(sourceStock -> {
                        sourceStock.setQuantityInTransit(
                                Math.max(0, sourceStock.getQuantityInTransit() - line.getQuantity()));
                        stockRepository.save(sourceStock);
                    });

            // Trouver ou créer la ligne de stock côté cible
            Stock targetStock = stockRepository
                    .findByProductIdAndWarehouseIdAndZoneId(
                            line.getProduct().getId(), targetWarehouseId, targetZone.getId())
                    .orElseGet(() -> Stock.builder()
                            .product(line.getProduct())
                            .warehouse(transfer.getTargetWarehouse())
                            .zone(targetZone)
                            .quantityAvailable(0)
                            .quantityReserved(0)
                            .quantityInTransit(0)
                            .build());

            targetStock.setQuantityAvailable(targetStock.getQuantityAvailable() + line.getQuantity());
            stockRepository.save(targetStock);

            // StockMovement ENTRY côté cible
            StockMovement entry = StockMovement.builder()
                    .product(line.getProduct())
                    .warehouse(transfer.getTargetWarehouse())
                    .zone(targetZone)
                    .quantity(line.getQuantity())
                    .movementType(MovementType.ENTRY)
                    .referenceDoc(transfer.getTransferNumber())
                    .note("Réception transfert depuis " + transfer.getSourceWarehouse().getName())
                    .createdBy(receiver)
                    .build();
            stockMovementRepository.save(entry);
        }

        transfer.setStatus(TransferStatus.RECEIVED);
        transfer.setReceivedAt(LocalDateTime.now());
        transfer.setReceivedBy(receiver);

        TransferResponse response = toResponse(transferRepository.save(transfer));

        // Source : quantityInTransit décrémentée / Cible : quantityAvailable incrémentée
        capacityService.recalculate(transfer.getSourceWarehouse());
        capacityService.recalculate(transfer.getTargetWarehouse());

        return response;
    }

    // ─────────────────────────────────────────────────────────────
    // ANNULATION — Admin seulement (PENDING ou VALIDATED → CANCELLED)
    // Si VALIDATED : rollback du stock source
    // ─────────────────────────────────────────────────────────────

    @Auditable(module = "transfer", action = "CANCEL", entity = "Transfer",
               description = "Transfert annulé")
    public TransferResponse cancel(Long transferId, CancelTransferRequest req) {
        Transfer transfer = getTransfer(transferId);
        User canceller = currentUserEntity();

        if (transfer.getStatus() == TransferStatus.RECEIVED) {
            throw new BusinessException("Un transfert déjà reçu ne peut pas être annulé");
        }
        if (transfer.getStatus() == TransferStatus.CANCELLED) {
            throw new BusinessException("Ce transfert est déjà annulé");
        }

        // Si le transfert était VALIDATED, il faut rollback le stock source
        if (transfer.getStatus() == TransferStatus.VALIDATED) {
            for (TransferLine line : transfer.getLines()) {
                stockRepository
                        .findByProductIdAndWarehouseIdAndZoneId(
                                line.getProduct().getId(),
                                transfer.getSourceWarehouse().getId(),
                                line.getSourceZone().getId())
                        .ifPresent(sourceStock -> {
                            // Annuler le décrément disponible + in-transit
                            sourceStock.setQuantityAvailable(
                                    sourceStock.getQuantityAvailable() + line.getQuantity());
                            sourceStock.setQuantityInTransit(
                                    Math.max(0, sourceStock.getQuantityInTransit() - line.getQuantity()));
                            stockRepository.save(sourceStock);

                            // StockMovement ADJUSTMENT pour tracer le rollback
                            StockMovement rollback = StockMovement.builder()
                                    .product(line.getProduct())
                                    .warehouse(transfer.getSourceWarehouse())
                                    .zone(line.getSourceZone())
                                    .quantity(line.getQuantity())
                                    .movementType(MovementType.ADJUSTMENT)
                                    .referenceDoc(transfer.getTransferNumber())
                                    .note("Annulation transfert — " + transfer.getTransferNumber())
                                    .createdBy(canceller)
                                    .build();
                            stockMovementRepository.save(rollback);
                        });
            }
        }

        transfer.setStatus(TransferStatus.CANCELLED);
        transfer.setCancellationReason(req != null ? req.getReason() : null);

        TransferResponse response = toResponse(transferRepository.save(transfer));

        // Si rollback du stock source → recalcul
        if (transfer.getStatus() == TransferStatus.VALIDATED) {
            capacityService.recalculate(transfer.getSourceWarehouse());
        }

        return response;
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS PRIVÉS
    // ─────────────────────────────────────────────────────────────

    private String generateTransferNumber() {
        String prefix = "TRF-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        String number;
        do {
            int rand = (int) (Math.random() * 9000) + 1000;
            number = prefix + rand;
        } while (transferRepository.existsByTransferNumber(number));
        return number;
    }

    private Transfer getTransfer(Long id) {
        return transferRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfert introuvable : " + id));
    }

    private Warehouse getWarehouse(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + id));
    }

    private Zone getZone(Long zoneId, Long warehouseId) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone introuvable : " + zoneId));
        if (!zone.getWarehouse().getId().equals(warehouseId)) {
            throw new BusinessException("Cette zone n'appartient pas à l'entrepôt attendu");
        }
        return zone;
    }

    private User currentUserEntity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return userRepository.findById(details.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    public TransferResponse toResponse(Transfer t) {
        List<TransferResponse.TransferLineResponse> lines = t.getLines().stream()
                .map(l -> TransferResponse.TransferLineResponse.builder()
                        .id(l.getId())
                        .productId(l.getProduct().getId())
                        .productName(l.getProduct().getName())
                        .productReference(l.getProduct().getReference())
                        .categoryName(l.getProduct().getCategory().getName())
                        .quantity(l.getQuantity())
                        .note(l.getNote())
                        .sourceZoneId(l.getSourceZone().getId())
                        .sourceZoneName(l.getSourceZone().getName())
                        .targetZoneId(l.getTargetZone() != null ? l.getTargetZone().getId() : null)
                        .targetZoneName(l.getTargetZone() != null ? l.getTargetZone().getName() : null)
                        .build())
                .toList();

        return TransferResponse.builder()
                .id(t.getId())
                .transferNumber(t.getTransferNumber())
                .status(t.getStatus())
                .note(t.getNote())
                .cancellationReason(t.getCancellationReason())
                .validatedAt(t.getValidatedAt())
                .receivedAt(t.getReceivedAt())
                .sourceWarehouseId(t.getSourceWarehouse().getId())
                .sourceWarehouseName(t.getSourceWarehouse().getName())
                .targetWarehouseId(t.getTargetWarehouse().getId())
                .targetWarehouseName(t.getTargetWarehouse().getName())
                .createdByUsername(t.getCreatedBy().getUsername())
                .validatedByUsername(t.getValidatedBy() != null
                        ? t.getValidatedBy().getUsername() : null)
                .receivedByUsername(t.getReceivedBy() != null
                        ? t.getReceivedBy().getUsername() : null)
                .lines(lines)
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
