package com.backend.module.permission.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class PermissionCatalogTest {

    @Test
    void shouldExposeDefaultPermissionsForCoreModules() {
        List<PermissionDefinition> definitions = PermissionCatalog.defaultDefinitions();

        assertTrue(definitions.size() >= 20);
        assertTrue(definitions.stream().anyMatch(definition -> "user".equals(definition.module()) && "read".equals(definition.action())));
        assertTrue(definitions.stream().anyMatch(definition -> "stock".equals(definition.module()) && "update".equals(definition.action())));
        assertTrue(definitions.stream().anyMatch(definition -> "transfer".equals(definition.module()) && "validate".equals(definition.action())));
    }
}
