package com.backend.module.zone.controller;

import com.backend.module.zone.dto.AssignCategoryRequest;
import com.backend.module.zone.dto.CreateZoneRequest;
import com.backend.module.zone.dto.UpdateZoneRequest;
import com.backend.module.zone.dto.ZoneResponse;
import com.backend.module.zone.service.ZoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses/{warehouseId}/zones")
@RequiredArgsConstructor
public class ZoneController {

    private final ZoneService zoneService;

    @GetMapping
    @PreAuthorize("hasAuthority('zone.read')")
    public ResponseEntity<Page<ZoneResponse>> getAll(
            @PathVariable Long warehouseId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("sequenceNumber").ascending());
        return ResponseEntity.ok(zoneService.findByWarehouse(warehouseId, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('zone.create')")
    public ResponseEntity<ZoneResponse> create(
            @PathVariable Long warehouseId,
            @Valid @RequestBody CreateZoneRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(zoneService.createZone(warehouseId, req));
    }

    @PatchMapping("/{zoneId}/assign-category")
    @PreAuthorize("hasAuthority('zone.update')")
    public ResponseEntity<ZoneResponse> assignCategory(
            @PathVariable Long warehouseId,
            @PathVariable Long zoneId,
            @Valid @RequestBody AssignCategoryRequest req
    ) {
        return ResponseEntity.ok(zoneService.assignCategory(warehouseId, zoneId, req));
    }

    /**
     * Mise à jour d'une zone (type, capacité).
     * Le gestionnaire ne peut modifier que les zones qu'il a créées.
     */
    @PutMapping("/{zoneId}")
    @PreAuthorize("hasAuthority('zone.update')")
    public ResponseEntity<ZoneResponse> update(
            @PathVariable Long warehouseId,
            @PathVariable Long zoneId,
            @RequestBody UpdateZoneRequest req
    ) {
        return ResponseEntity.ok(zoneService.updateZone(warehouseId, zoneId, req));
    }

    /**
     * IDs des catégories déjà couvertes par une zone Admin dans cet entrepôt.
     * Utilisé par le frontend pour filtrer le select du gestionnaire :
     * seules les catégories non couvertes lui sont proposées.
     */
    @GetMapping("/covered-categories")
    @PreAuthorize("hasAuthority('zone.read')")
    public ResponseEntity<List<Long>> getCoveredCategoryIds(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(zoneService.getCoveredCategoryIds(warehouseId));
    }
}
