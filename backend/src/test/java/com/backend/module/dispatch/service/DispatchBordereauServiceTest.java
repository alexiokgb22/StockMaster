package com.backend.module.dispatch.service;

import com.backend.module.dispatch.entity.Dispatch;
import com.backend.module.dispatch.entity.DispatchLine;
import com.backend.module.product.entity.Product;
import com.backend.module.shared.enums.DispatchStatus;
import com.backend.module.warehouse.entity.Warehouse;
import com.backend.module.zone.entity.Zone;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DispatchBordereauServiceTest {

    @Test
    void shouldIncludeClientAndProductDetailsInGeneratedBordereau() {
        DispatchBordereauService service = new DispatchBordereauService();

        Warehouse warehouse = Warehouse.builder().id(10L).name("Entrepôt principal").build();
        Zone zone = Zone.builder().id(20L).name("Zone A").warehouse(warehouse).build();
        Product product = Product.builder().id(30L).name("Produit Test").reference("REF-001").build();

        Dispatch dispatch = Dispatch.builder()
                .dispatchNumber("DSP-20260101-1234")
                .status(DispatchStatus.VALIDATED)
                .note("Livraison urgente")
                .warehouse(warehouse)
                .clientFirstName("Jane")
                .clientLastName("Doe")
                .clientPhone("0600000000")
                .deliveryAddress("12 rue des Lilas, Paris")
                .build();

        DispatchLine line = DispatchLine.builder()
                .product(product)
                .zone(zone)
                .quantityRequested(5)
                .note("Palette fragile")
                .build();
        dispatch.getLines().add(line);

        String html = service.generateHtml(dispatch);

        assertTrue(html.contains("Bordereau de sortie"));
        assertTrue(html.contains("Jane Doe"));
        assertTrue(html.contains("0600000000"));
        assertTrue(html.contains("12 rue des Lilas, Paris"));
        assertTrue(html.contains("Produit Test"));
        assertTrue(html.contains("5"));
    }
}
