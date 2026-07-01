package com.backend.module.dispatch.controller;

import com.backend.module.dispatch.dto.CreateDispatchRequest;
import com.backend.module.dispatch.dto.DispatchResponse;
import com.backend.module.dispatch.entity.Dispatch;
import com.backend.module.dispatch.service.DispatchBordereauService;
import com.backend.module.dispatch.service.DispatchService;
import com.backend.module.shared.enums.DispatchStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouses/{warehouseId}/dispatches")
@RequiredArgsConstructor
public class DispatchController {

    private final DispatchService dispatchService;
    private final DispatchBordereauService dispatchBordereauService;

    // ── GET — liste des bons (magasinier + gestionnaire en lecture)
    @GetMapping
    @PreAuthorize("hasAuthority('dispatch.create') or hasAuthority('warehouse.read')")
    public ResponseEntity<Page<DispatchResponse>> getAll(
            @PathVariable Long warehouseId,
            @RequestParam(required = false) DispatchStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(dispatchService.findByWarehouse(warehouseId, status, pageable));
    }

    // ── GET — détail d'un bon
    @GetMapping("/{dispatchId}")
    @PreAuthorize("hasAuthority('dispatch.create') or hasAuthority('warehouse.read')")
    public ResponseEntity<DispatchResponse> getById(
            @PathVariable Long warehouseId,
            @PathVariable Long dispatchId
    ) {
        return ResponseEntity.ok(dispatchService.findById(warehouseId, dispatchId));
    }

    // ── POST — magasinier crée et valide le bon directement
    @PostMapping
    @PreAuthorize("hasAuthority('dispatch.create')")
    public ResponseEntity<DispatchResponse> create(
            @PathVariable Long warehouseId,
            @Valid @RequestBody CreateDispatchRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dispatchService.create(warehouseId, req));
    }

    // ── GET — bordereau de sortie
    @GetMapping("/{dispatchId}/bordereau")
    @PreAuthorize("hasAuthority('dispatch.print_bordereau')")
    public ResponseEntity<String> getBordereau(
            @PathVariable Long warehouseId,
            @PathVariable Long dispatchId
    ) {
        Dispatch dispatch = dispatchService.getDispatchForBordereau(dispatchId, warehouseId);
        String html = dispatchBordereauService.generateHtml(dispatch);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}
