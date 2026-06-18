package com.backend.module.role.controller;

import com.backend.module.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleRepository roleRepository;

    @GetMapping
    public ResponseEntity<List<RoleResponse>> getAll() {
        List<RoleResponse> roles = roleRepository.findAll().stream()
                .filter(r -> r.getIsActive())
                .map(r -> new RoleResponse(r.getId(), r.getName()))
                .toList();
        return ResponseEntity.ok(roles);
    }

    public record RoleResponse(Long id, String name) {}
}
