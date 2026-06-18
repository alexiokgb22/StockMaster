package com.backend.module.warehouse.controller;

import com.backend.module.warehouse.dto.WarehouseResponse;
import com.backend.module.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseRepository warehouseRepository;

    @GetMapping
    public ResponseEntity<List<WarehouseResponse>> getAll() {
        List<WarehouseResponse> warehouses = warehouseRepository.findAll().stream()
                .filter(w -> w.getIsActive())
                .map(w -> WarehouseResponse.builder()
                        .id(w.getId())
                        .name(w.getName())
                        .city(w.getCity())
                        .build())
                .toList();
        return ResponseEntity.ok(warehouses);
    }
}
