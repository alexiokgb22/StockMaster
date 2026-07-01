package com.backend.module.inventory.service;

import com.backend.exception.BusinessException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.module.inventory.dto.CreateInventoryRequest;
import com.backend.module.inventory.dto.InventoryResponse;
import com.backend.module.inventory.dto.UpdateInventoryLineRequest;
import com.backend.module.inventory.entity.Inventory;
import com.backend.module.inventory.repository.InventoryRepository;
import com.backend.module.inventoryline.entity.InventoryLine;
import com.backend.module.inventoryline.repository.InventoryLineRepository;
import com.backend.module.shared.enums.InventoryStatus;
import com.backend.module.shared.enums.InventoryType;
import com.backend.module.shared.enums.MovementType;
import com.backend.module.stock.entity.Stock;
import com.backend.module.stock.repository.StockRepository;
import com.backend.module.stockmovement.entity.StockMovement;
import com.backend.module.stockmovement.repository.StockMovementRepository;
import com.backend.module.user.entity.User;
import com.backend.module.user.repository.UserRepository;
import com.backend.module.warehouse.entity.Warehouse;
import com.backend.module.warehouse.repository.WarehouseRepository;
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

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryLineRepository inventoryLineRepository;
    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;
    private final com.backend.module.stock.service.CapacityService capacityService;

    // ─────────────────────────────────────────────────────────────
    // LECTURE — liste paginée
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<InventoryResponse> findByWarehouse(
            Long warehouseId, InventoryStatus status, Pageable pageable) {
        getWarehouse(warehouseId);
        return inventoryRepository
                .findByWarehouseWithFilters(warehouseId, status, pageable)
                .map(this::toResponse);
    }

    // ─────────────────────────────────────────────────────────────
    // LECTURE — détail complet avec lignes
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public InventoryResponse findById(Long warehouseId, Long inventoryId) {
        Inventory inventory = getInventoryInWarehouse(inventoryId, warehouseId);
        return toResponse(inventory);
    }

    // ─────────────────────────────────────────────────────────────
    // CRÉATION — Gestionnaire initie l'inventaire (IN_PROGRESS)
    //
    // Règles :
    //   1. Un seul inventaire IN_PROGRESS par entrepôt à la fois.
    //   2. FULL  → snapshot de toutes les lignes de stock de l'entrepôt.
    //   3. PARTIAL → snapshot uniquement des zones fournies.
    //   4. theoreticalQty = quantityAvailable courant au moment du snapshot.
    //   5. actualQty = null (non saisie).
    // ─────────────────────────────────────────────────────────────

    public InventoryResponse create(Long warehouseId, CreateInventoryRequest req) {
        Warehouse warehouse = getWarehouse(warehouseId);
        User creator = currentUserEntity();

        // Règle 1
        if (inventoryRepository.existsByWarehouseIdAndInventoryStatus(
                warehouseId, InventoryStatus.IN_PROGRESS)) {
            throw new BusinessException(
                "Un inventaire est déjà en cours dans cet entrepôt. "
                + "Clôturez-le avant d'en créer un nouveau.");
        }

        Inventory inventory = Inventory.builder()
                .inventoryNumber(generateInventoryNumber())
                .inventoryType(req.getInventoryType())
                .inventoryStatus(InventoryStatus.IN_PROGRESS)
                .note(req.getNote())
                .startedAt(LocalDateTime.now())
                .warehouse(warehouse)
                .createdBy(creator)
                .build();

        // Snapshot des stocks selon le type
        List<Stock> stocks;
        if (req.getInventoryType() == InventoryType.FULL
                || req.getZoneIds() == null || req.getZoneIds().isEmpty()) {
            // FULL : toutes les lignes de stock de l'entrepôt
            stocks = stockRepository.findAllByWarehouseId(warehouseId);
        } else {
            // PARTIAL : uniquement les zones sélectionnées
            stocks = stockRepository.findAllByWarehouseIdAndZoneIdIn(
                    warehouseId, req.getZoneIds());
        }

        if (stocks.isEmpty()) {
            throw new BusinessException(
                "Aucune ligne de stock à inventorier dans cet entrepôt. "
                + "Créez d'abord des stocks.");
        }

        // Construire les lignes avec snapshot théorique
        for (Stock stock : stocks) {
            InventoryLine line = InventoryLine.builder()
                    .inventory(inventory)
                    .stock(stock)
                    .product(stock.getProduct())
                    .zone(stock.getZone())
                    .theoreticalQty(stock.getQuantityAvailable())
                    .actualQty(null)   // non saisie
                    .build();
            inventory.getLines().add(line);
        }

        return toResponse(inventoryRepository.save(inventory));
    }

    // ─────────────────────────────────────────────────────────────
    // SAISIE — Magasinier (ou gestionnaire) saisit la quantité physique
    // Opération idempotente : peut être modifiée tant que IN_PROGRESS
    // ─────────────────────────────────────────────────────────────

    public InventoryResponse updateLine(
            Long warehouseId, Long inventoryId, Long lineId,
            UpdateInventoryLineRequest req) {

        Inventory inventory = getInventoryInWarehouse(inventoryId, warehouseId);

        if (inventory.getInventoryStatus() != InventoryStatus.IN_PROGRESS) {
            throw new BusinessException(
                "Impossible de modifier les lignes d'un inventaire clôturé ou annulé");
        }

        InventoryLine line = inventoryLineRepository
                .findByIdAndWarehouseId(lineId, warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ligne d'inventaire introuvable : " + lineId));

        if (!line.getInventory().getId().equals(inventoryId)) {
            throw new BusinessException("Cette ligne n'appartient pas à cet inventaire");
        }

        line.setActualQty(req.getActualQty());
        if (req.getNote() != null) line.setNote(req.getNote());
        inventoryLineRepository.save(line);

        // Recharger avec détails pour la réponse
        return toResponse(getInventoryInWarehouse(inventoryId, warehouseId));
    }

    // ─────────────────────────────────────────────────────────────
    // CLÔTURE — Gestionnaire clôture (IN_PROGRESS → COMPLETED)
    //
    // Règles :
    //   1. Toutes les lignes doivent avoir actualQty saisie.
    //   2. Pour chaque ligne avec gap ≠ 0 :
    //      - stock.quantityAvailable = actualQty
    //      - StockMovement ADJUSTMENT créé
    //   3. Irréversible.
    // ─────────────────────────────────────────────────────────────

    public InventoryResponse complete(Long warehouseId, Long inventoryId) {
        Inventory inventory = getInventoryInWarehouse(inventoryId, warehouseId);
        User validator = currentUserEntity();

        if (inventory.getInventoryStatus() != InventoryStatus.IN_PROGRESS) {
            throw new BusinessException("Seuls les inventaires IN_PROGRESS peuvent être clôturés");
        }

        // Règle 1 : toutes les lignes doivent être saisies
        long uncounted = inventoryLineRepository.countUncounted(inventoryId);
        if (uncounted > 0) {
            throw new BusinessException(
                uncounted + " ligne(s) n'ont pas encore été saisies. "
                + "Complétez toutes les lignes avant de clôturer.");
        }

        // Recharger l'inventaire avec toutes les lignes et stocks
        Inventory fullInventory = inventoryRepository.findByIdWithDetails(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventaire introuvable : " + inventoryId));

        // Règle 2 : appliquer les ajustements
        for (InventoryLine line : fullInventory.getLines()) {
            int gap = line.getActualQty() - line.getTheoreticalQty();
            if (gap == 0) continue;

            // Ajuster le stock
            Stock stock = line.getStock();
            stock.setQuantityAvailable(line.getActualQty());
            stockRepository.save(stock);

            // Tracer le mouvement d'ajustement
            String note = gap > 0
                ? "Inventaire " + inventory.getInventoryNumber() + " — surplus de " + gap
                : "Inventaire " + inventory.getInventoryNumber() + " — manquant de " + Math.abs(gap);

            StockMovement movement = StockMovement.builder()
                    .product(line.getProduct())
                    .warehouse(inventory.getWarehouse())
                    .zone(line.getZone())
                    .quantity(Math.abs(gap))
                    .movementType(MovementType.ADJUSTMENT)
                    .referenceDoc(inventory.getInventoryNumber())
                    .note(note)
                    .createdBy(validator)
                    .build();
            stockMovementRepository.save(movement);
        }

        fullInventory.setInventoryStatus(InventoryStatus.COMPLETED);
        fullInventory.setCompletedAt(LocalDateTime.now());

        InventoryResponse response = toResponse(inventoryRepository.save(fullInventory));

        // Recalcul de la capacité après ajustements de stock
        capacityService.recalculate(fullInventory.getWarehouse());

        return response;
    }

    // ─────────────────────────────────────────────────────────────
    // ANNULATION — Gestionnaire annule (IN_PROGRESS → CANCELLED)
    // Aucun ajustement stock
    // ─────────────────────────────────────────────────────────────

    public InventoryResponse cancel(Long warehouseId, Long inventoryId) {
        Inventory inventory = getInventoryInWarehouse(inventoryId, warehouseId);

        if (inventory.getInventoryStatus() != InventoryStatus.IN_PROGRESS) {
            throw new BusinessException(
                "Seuls les inventaires IN_PROGRESS peuvent être annulés");
        }

        inventory.setInventoryStatus(InventoryStatus.CANCELLED);
        return toResponse(inventoryRepository.save(inventory));
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS PRIVÉS
    // ─────────────────────────────────────────────────────────────

    private String generateInventoryNumber() {
        String prefix = "INV-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        String number;
        do {
            int rand = (int) (Math.random() * 9000) + 1000;
            number = prefix + rand;
        } while (inventoryRepository.existsByInventoryNumber(number));
        return number;
    }

    private Inventory getInventoryInWarehouse(Long inventoryId, Long warehouseId) {
        Inventory inv = inventoryRepository.findByIdWithDetails(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventaire introuvable : " + inventoryId));
        if (!inv.getWarehouse().getId().equals(warehouseId)) {
            throw new ResourceNotFoundException(
                    "Inventaire introuvable dans cet entrepôt : " + inventoryId);
        }
        return inv;
    }

    private Warehouse getWarehouse(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + id));
    }

    private User currentUserEntity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return userRepository.findById(details.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    public InventoryResponse toResponse(Inventory inv) {
        List<InventoryResponse.InventoryLineResponse> lines = inv.getLines().stream()
                .sorted((a, b) -> {
                    // Trier : non saisies en premier, puis par zone + produit
                    if (a.isCounted() != b.isCounted()) {
                        return a.isCounted() ? 1 : -1;
                    }
                    int zoneCompare = a.getZone().getName().compareTo(b.getZone().getName());
                    if (zoneCompare != 0) return zoneCompare;
                    return a.getProduct().getName().compareTo(b.getProduct().getName());
                })
                .map(l -> InventoryResponse.InventoryLineResponse.builder()
                        .id(l.getId())
                        .stockId(l.getStock().getId())
                        .productId(l.getProduct().getId())
                        .productName(l.getProduct().getName())
                        .productReference(l.getProduct().getReference())
                        .categoryName(l.getProduct().getCategory().getName())
                        .zoneId(l.getZone().getId())
                        .zoneName(l.getZone().getName())
                        .theoreticalQty(l.getTheoreticalQty())
                        .actualQty(l.getActualQty())
                        .gap(l.getGap())
                        .counted(l.isCounted())
                        .note(l.getNote())
                        .build())
                .toList();
 
        int totalLines   = lines.size();
        int countedLines = (int) lines.stream().filter(InventoryResponse.InventoryLineResponse::isCounted).count();
        int gapLines     = (int) lines.stream()
                .filter(l -> l.getGap() != null && l.getGap() != 0).count();
        int totalGap     = lines.stream()
                .filter(l -> l.getGap() != null)
                .mapToInt(l -> Math.abs(l.getGap()))
                .sum();

        return InventoryResponse.builder()
                .id(inv.getId())
                .inventoryNumber(inv.getInventoryNumber())
                .inventoryType(inv.getInventoryType())
                .inventoryStatus(inv.getInventoryStatus())
                .note(inv.getNote())
                .startedAt(inv.getStartedAt())
                .completedAt(inv.getCompletedAt())
                .warehouseId(inv.getWarehouse().getId())
                .warehouseName(inv.getWarehouse().getName())
                .createdByUsername(inv.getCreatedBy().getUsername())
                .lines(lines)
                .totalLines(totalLines)
                .countedLines(countedLines)
                .gapLines(gapLines)
                .totalGap(totalGap)
                .createdAt(inv.getCreatedAt())
                .updatedAt(inv.getUpdatedAt())
                .build();
    }
}
