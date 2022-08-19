package ru.aakhm.inflationrest.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.aakhm.inflationrest.dto.in.ProductCategoryInDTO;
import ru.aakhm.inflationrest.dto.out.ProductCategoryOutDTO;
import ru.aakhm.inflationrest.models.ProductCategory;
import ru.aakhm.inflationrest.models.validation.except.productcategory.ProductCategoryNotFoundException;
import ru.aakhm.inflationrest.repo.ProductCategoriesRepo;
import ru.aakhm.inflationrest.utils.Utils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

        when(productCategoriesRepo.getByName(productCategory.getName())).thenReturn(Optional.empty());
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
        final String EXTERNAL_ID = "123abc";
        ProductCategory productCategory = new ProductCategory();
        productCategory.setExternalId(EXTERNAL_ID);
        productCategory.setName("Test");

        // product category существует
        when(productCategoriesRepo.getByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(productCategory));

        productCategoriesService.deleteByExternalId(EXTERNAL_ID);
        verify(productCategoriesRepo, times(1)).deleteById(anyInt());

        // product category не существует
        reset(productCategoriesRepo);
        when(productCategoriesRepo.getByExternalId(EXTERNAL_ID)).thenReturn(Optional.empty());

        assertThrows(ProductCategoryNotFoundException.class, () -> productCategoriesService.deleteByExternalId(EXTERNAL_ID));
        verify(productCategoriesRepo, times(0)).deleteById(anyInt());
    }

    @Test
    void update() {
        final String EXTERNAL_ID = "123abc";
        final String NAME = "Test";

        ProductCategoryInDTO pcInDTO = new ProductCategoryInDTO();
        pcInDTO.setName("Test");

        ProductCategory productCategory = new ProductCategory();
        productCategory.setName(pcInDTO.getName());
        productCategory.setExternalId(EXTERNAL_ID);

        ProductCategoryOutDTO pcOutDTO = new ProductCategoryOutDTO();
        pcOutDTO.setName(pcInDTO.getName());
        pcOutDTO.setExternalId(EXTERNAL_ID);

        // product category существует
        when(productCategoriesRepo.getByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(productCategory));
        when(productCategoriesRepo.save(any(ProductCategory.class))).thenReturn(productCategory);
        when(modelMapper.map(any(ProductCategory.class), any())).thenReturn(pcOutDTO);

        ProductCategoryOutDTO resProductCategory = productCategoriesService.update(EXTERNAL_ID, pcInDTO);
        verify(productCategoriesRepo, times(1)).save(any(ProductCategory.class));

        assertNotNull(resProductCategory);
        assertEquals(EXTERNAL_ID, resProductCategory.getExternalId());
        assertEquals(NAME, resProductCategory.getName());

        // product category не существует
        reset(productCategoriesRepo);
        when(productCategoriesRepo.getByExternalId(EXTERNAL_ID)).thenReturn(Optional.empty());

        assertThrows(ProductCategoryNotFoundException.class, () -> productCategoriesService.update(EXTERNAL_ID, pcInDTO));
        verify(productCategoriesRepo, times(0)).save(any());
    }

    @Test
    void index() {
        when(productCategoriesRepo.findAll(any(Pageable.class))).thenReturn(repoIndex());
        when(modelMapper.map(any(ProductCategory.class), any())).thenReturn(new ProductCategoryOutDTO());

        List<ProductCategoryOutDTO> resList = productCategoriesService.index(0, 10);
        assertNotNull(resList);
        assertEquals(repoIndex().getContent().size(), resList.size());
        verify(productCategoriesRepo, times(1)).findAll(any(Pageable.class));
        verify(productCategoriesRepo, times(0)).findAll();
    }

    @Test
    void getByName() {
        final String NAME = "Test";
        ProductCategory productCategory = new ProductCategory();
        productCategory.setName(NAME);

        ProductCategoryOutDTO pcOutDTO = new ProductCategoryOutDTO();
        pcOutDTO.setName(NAME);

        // product category существует
        when(productCategoriesRepo.getByName(NAME)).thenReturn(Optional.of(productCategory));
        when(modelMapper.map(any(ProductCategory.class), any())).thenReturn(pcOutDTO);

        Optional<ProductCategoryOutDTO> resPC = productCategoriesService.getByName(NAME);
        assertNotNull(resPC);
        assertTrue(resPC.isPresent());
        assertEquals(NAME, resPC.get().getName());
        verify(productCategoriesRepo, times(1)).getByName(anyString());

        // product category не существует
        reset(productCategoriesRepo);
        resPC = productCategoriesService.getByName(NAME);
        assertFalse(resPC.isPresent());
        verify(productCategoriesRepo, times(1)).getByName(anyString());
    }


    @Test
    void getByExternalId() {
        final String EXTERNAL_ID = "123abc";
        final String NAME = "Test";
        ProductCategory productCategory = new ProductCategory() {
            {
                setName(NAME);
                setExternalId(EXTERNAL_ID);
            }
        };

        ProductCategoryOutDTO pcOutDTO = new ProductCategoryOutDTO() {
            {
                setName(NAME);
                setExternalId(EXTERNAL_ID);
            }
        };

        // product category существует
        when(productCategoriesRepo.getByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(productCategory));
        when(modelMapper.map(any(ProductCategory.class), any())).thenReturn(pcOutDTO);
        ProductCategoryOutDTO resPC = productCategoriesService.getByExternalId(EXTERNAL_ID);
        assertNotNull(resPC);
        assertEquals(NAME, resPC.getName());
        assertEquals(EXTERNAL_ID, resPC.getExternalId());

        // product category не существует
        reset(productCategoriesRepo);
        when(productCategoriesRepo.getByExternalId(EXTERNAL_ID)).thenReturn(Optional.empty());

        assertThrows(ProductCategoryNotFoundException.class, () -> productCategoriesService.getByExternalId(EXTERNAL_ID));
        verify(productCategoriesRepo, times(1)).getByExternalId(anyString());
    }


    private Page<ProductCategory> repoIndex() {
        return new PageImpl<>(List.of(
                new ProductCategory() {
                    {
                        setId(1);
                        setExternalId("a");
                        setName("Test1");
                    }
                },
                new ProductCategory() {
                    {
                        setId(2);
                        setExternalId("b");
                        setName("Test2");
                    }
                },
                new ProductCategory() {
                    {
                        setId(3);
                        setExternalId("c");
                        setName("Test3");
                    }
                }
        ));
    }
}