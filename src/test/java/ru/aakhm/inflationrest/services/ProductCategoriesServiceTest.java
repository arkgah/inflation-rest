package ru.aakhm.inflationrest.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import ru.aakhm.inflationrest.dto.in.ProductCategoryInDTO;
import ru.aakhm.inflationrest.dto.out.ProductCategoryOutDTO;
import ru.aakhm.inflationrest.models.ProductCategory;
import ru.aakhm.inflationrest.repo.ProductCategoriesRepo;
import ru.aakhm.inflationrest.utils.Utils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class ProductCategoriesServiceTest {
    @InjectMocks
    private ProductCategoriesService productCategoriesService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private Utils utils;

    @Mock
    private ProductCategoriesRepo productCategoriesRepo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void save() {
        final String EXTERNAL_ID = "123abc";
        ProductCategoryInDTO pcInDTO = new ProductCategoryInDTO();
        pcInDTO.setName("Test");

        ProductCategory productCategory = new ProductCategory();
        productCategory.setName(pcInDTO.getName());

        ProductCategoryOutDTO pcOutDTO = new ProductCategoryOutDTO();
        pcOutDTO.setName(pcInDTO.getName());
        pcOutDTO.setExternalId(EXTERNAL_ID);

        when(productCategoriesRepo.findByName(productCategory.getName())).thenReturn(Optional.empty());
        when(productCategoriesRepo.save(any(ProductCategory.class))).thenReturn(productCategory);
        when(utils.generateExternalId()).thenReturn(EXTERNAL_ID);

        when(modelMapper.map(any(ProductCategoryInDTO.class), any())).thenReturn(productCategory);
        when(modelMapper.map(any(ProductCategory.class), any())).thenReturn(pcOutDTO);

        ProductCategoryOutDTO resProductCategory = productCategoriesService.save(pcInDTO);
        verify(productCategoriesRepo, times(1)).save(any(ProductCategory.class));
        verify(modelMapper, times(1)).map(any(ProductCategoryInDTO.class), any());
        verify(modelMapper, times(1)).map(any(ProductCategory.class), any());

        assertNotNull(resProductCategory);
        assertEquals(productCategory.getName(), resProductCategory.getName());
        assertEquals(EXTERNAL_ID, resProductCategory.getExternalId());
    }

    @Test
    void deleteByExternalId() {
    }

    @Test
    void update() {
    }

    @Test
    void index() {
    }

    @Test
    void getByName() {
    }

    @Test
    void getByExternalId() {
    }
}