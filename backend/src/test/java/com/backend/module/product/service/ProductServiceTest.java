package com.backend.module.product.service;

import com.backend.module.category.entity.Category;
import com.backend.module.category.repository.CategoryRepository;
import com.backend.module.product.dto.CreateProductRequest;
import com.backend.module.product.entity.Product;
import com.backend.module.product.repository.ProductRepository;
import com.backend.module.role.entity.Role;
import com.backend.module.user.entity.User;
import com.backend.module.user.repository.UserRepository;
import com.backend.module.warehouse.entity.Warehouse;
import com.backend.module.warehouse.repository.WarehouseRepository;
import com.backend.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProductService productService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createInWarehouse_shouldAssociateProductWithManagerWarehouse() {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(10L);
        warehouse.setName("Entrepôt A");

        Category category = new Category();
        category.setId(5L);
        category.setName("Catégorie test");

        Role role = new Role();
        role.setId(2L);
        role.setName("Gestionnaire d'entrepôt");
        role.setPermissions(Set.of());

        User creator = new User();
        creator.setId(1L);
        creator.setUsername("manager");
        creator.setRole(role);
        creator.setAssignedWarehouse(warehouse);

        CustomUserDetails userDetails = new CustomUserDetails(creator);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);


        when(warehouseRepository.findById(10L)).thenReturn(Optional.of(warehouse));
        when(categoryRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(category));
        when(userRepository.findById(1L)).thenReturn(Optional.of(creator));
        when(productRepository.existsByReference(anyString())).thenReturn(false);
        when(productRepository.existsByBarcode(anyString())).thenReturn(false);
        when(productRepository.existsByNameIgnoreCaseAndCategoryId(anyString(), eq(5L))).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateProductRequest request = new CreateProductRequest();
        request.setName("Produit test");
        request.setCategoryId(5L);
        request.setDescription("Description");

        productService.createInWarehouse(10L, request);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());

        assertTrue(productCaptor.getValue().getWarehouses().contains(warehouse));
    }
}
