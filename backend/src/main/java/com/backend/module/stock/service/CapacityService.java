package com.backend.module.stock.service;

import com.backend.module.stock.repository.StockRepository;
import com.backend.module.warehouse.entity.Warehouse;
import com.backend.module.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service transversal de recalcul de capacité.
 *
 * Centralise la logique de mise à jour de warehouse.usedCapacity
 * afin que tous les services métier (réception, sortie, transfert,
 * inventaire, stock) utilisent exactement le même calcul.
 *
 * Formule : usedCapacity = SUM(quantityAvailable × product.volume)
 * pour toutes les lignes de stock de l'entrepôt dont le produit
 * a un volume renseigné.
 */
@Service
@RequiredArgsConstructor
public class CapacityService {

    private final StockRepository stockRepository;
    private final WarehouseRepository warehouseRepository;

    /**
     * Recalcule et persiste warehouse.usedCapacity.
     * Appelé après toute opération qui modifie quantityAvailable.
     *
     * @param warehouse l'entrepôt à mettre à jour
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void recalculate(Warehouse warehouse) {
        double usedVolume = stockRepository.sumVolumeByWarehouse(warehouse.getId());
        warehouse.setUsedCapacity(usedVolume);
        warehouseRepository.save(warehouse);
    }

    /**
     * Surcharge pratique quand on n'a que l'ID de l'entrepôt.
     * Charge l'entrepôt depuis la base avant de recalculer.
     *
     * @param warehouseId l'ID de l'entrepôt à mettre à jour
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void recalculate(Long warehouseId) {
        warehouseRepository.findById(warehouseId).ifPresent(this::recalculate);
    }
}
