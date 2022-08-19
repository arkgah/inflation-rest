package ru.aakhm.inflationrest.services;

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
import ru.aakhm.inflationrest.dto.in.ProductInDTO;
import ru.aakhm.inflationrest.dto.out.ProductCategoryOutDTO;
import ru.aakhm.inflationrest.dto.out.ProductOutDTO;
import ru.aakhm.inflationrest.models.Product;
import ru.aakhm.inflationrest.models.ProductCategory;
import ru.aakhm.inflationrest.models.validation.except.product.ProductNameCategoryUniqException;
import ru.aakhm.inflationrest.models.validation.except.product.ProductNotFoundException;
import ru.aakhm.inflationrest.models.validation.except.productcategory.ProductCategoryNotFoundException;
import ru.aakhm.inflationrest.repo.ProductCategoriesRepo;
import ru.aakhm.inflationrest.repo.ProductsRepo;
import ru.aakhm.inflationrest.utils.Utils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductsServiceTest {
    @InjectMocks
    private ProductsService productsService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private Utils utils;

    @Mock
    private ProductsRepo productsRepo;

    @Mock
    private ProductCategoriesRepo productCategoriesRepo;


    private ProductInDTO productInDTO;
    private Product product;
    private ProductCategory productCategory;
    private ProductOutDTO productOutDTO;

    private final String PRODUCT_EXTERNAL_ID = "123abc";
    private final String PRODUCT_NAME = "Test Product";
    private final String PRODUCT_CAT_NAME = "Test Category";
    private final String PRODUCT_CAT_EXTERNAL_ID = "456def";
    private final Double UNIT = 0.5;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        initProducts();
    }

    private void initProducts() {
        productInDTO = new ProductInDTO() {
            {
                setName(PRODUCT_NAME);
                setUnit(UNIT);
                setCategory(new ProductCategoryInDTO() {
                    {
                        setName(PRODUCT_CAT_NAME);
                    }
                });
            }
        };

        productCategory = new ProductCategory() {
            {
                setName(PRODUCT_CAT_NAME);
                setExternalId(PRODUCT_CAT_EXTERNAL_ID);
            }
        };

        product = new Product() {
            {
                setId(1);
                setName(PRODUCT_NAME);
                setUnit(UNIT);
                setExternalId(PRODUCT_EXTERNAL_ID);
                setCategory(productCategory);
            }
        };

        productOutDTO = new ProductOutDTO() {
            {
                setName(PRODUCT_NAME);
                setUnit(UNIT);
                setCategory(new ProductCategoryOutDTO() {
                    {
                        setName(PRODUCT_CAT_NAME);
                        setExternalId(PRODUCT_CAT_EXTERNAL_ID);
                    }
                });
                setExternalId(PRODUCT_EXTERNAL_ID);
            }
        };
    }

    @Test
    void save() {
        when(productsRepo.save(any(Product.class))).thenReturn(product);
        when(utils.generateExternalId()).thenReturn(PRODUCT_EXTERNAL_ID);

        when(productCategoriesRepo.getByName(any(String.class))).thenReturn(Optional.of(productCategory));

        when(modelMapper.map(any(ProductInDTO.class), any())).thenReturn(product);
        when(modelMapper.map(any(Product.class), any())).thenReturn(productOutDTO);

        ProductOutDTO resProduct = productsService.save(productInDTO);
        verify(productsRepo, times(1)).save(any(Product.class));
        verify(modelMapper, times(1)).map(any(ProductInDTO.class), any());
        verify(modelMapper, times(1)).map(any(Product.class), any());

        assertNotNull(resProduct);
        assertEquals(PRODUCT_NAME, resProduct.getName());
        assertEquals(PRODUCT_EXTERNAL_ID, resProduct.getExternalId());

        assertNotNull(resProduct.getCategory());
        assertEquals(PRODUCT_CAT_NAME, resProduct.getCategory().getName());
    }

    @Test
    void update() {
        // product && productCategory существуют, имя товара и категория уникальны
        when(productsRepo.getByExternalId(PRODUCT_EXTERNAL_ID)).thenReturn(Optional.of(product));
        when(productsRepo.save(any(Product.class))).thenReturn(product);
        when(productsRepo.getProductByNameAndCategory(PRODUCT_NAME, productCategory)).thenReturn(Optional.empty());

        when(productCategoriesRepo.getByName(PRODUCT_CAT_NAME)).thenReturn(Optional.of(productCategory));
        when(modelMapper.map(any(ProductInDTO.class), any())).thenReturn(product);
        when(modelMapper.map(any(Product.class), any())).thenReturn(productOutDTO);

        ProductOutDTO resProduct = productsService.update(PRODUCT_EXTERNAL_ID, productInDTO);
        verify(productsRepo, times(1)).getProductByNameAndCategory(PRODUCT_NAME, productCategory);
        verify(productsRepo, times(1)).save(any(Product.class));
        assertNotNull(resProduct);
        assertEquals(PRODUCT_EXTERNAL_ID, resProduct.getExternalId());
        assertEquals(PRODUCT_NAME, resProduct.getName());
        assertEquals(UNIT, resProduct.getUnit());
        assertNotNull(resProduct.getCategory());
        assertEquals(PRODUCT_CAT_EXTERNAL_ID, resProduct.getCategory().getExternalId());
        assertEquals(PRODUCT_CAT_NAME, resProduct.getCategory().getName());

        // product не существует
        when(productsRepo.getByExternalId(PRODUCT_EXTERNAL_ID)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> productsService.update(PRODUCT_EXTERNAL_ID, productInDTO));

        // productCategory не существует
        when(productsRepo.getByExternalId(PRODUCT_EXTERNAL_ID)).thenReturn(Optional.of(product));
        when(productCategoriesRepo.getByName(PRODUCT_CAT_NAME)).thenReturn(Optional.empty());
        assertThrows(ProductCategoryNotFoundException.class, () -> productsService.update(PRODUCT_EXTERNAL_ID, productInDTO));

        // неуникальная имя и категория товара
        when(productsRepo.getProductByNameAndCategory(PRODUCT_NAME, productCategory)).thenReturn(Optional.of(product));
        when(productsRepo.getByExternalId(PRODUCT_EXTERNAL_ID)).thenReturn(Optional.of(product));
        when(productCategoriesRepo.getByName(PRODUCT_CAT_NAME)).thenReturn(Optional.of(productCategory));
        assertThrows(ProductNameCategoryUniqException.class, () -> productsService.update(PRODUCT_EXTERNAL_ID, productInDTO));

    }

    @Test
    void deleteByExternalId() {
        // product существует
        when(productsRepo.getByExternalId(PRODUCT_EXTERNAL_ID)).thenReturn(Optional.of(product));
        assertDoesNotThrow(() -> productsService.deleteByExternalId(PRODUCT_EXTERNAL_ID));
        verify(productsRepo, times(1)).delete(any(Product.class));

        // product не существует
        reset(productsRepo);
        when(productsRepo.getByExternalId(PRODUCT_EXTERNAL_ID)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> productsService.deleteByExternalId(PRODUCT_EXTERNAL_ID));
        verify(productsRepo, times(0)).delete(any(Product.class));
    }

    @Test
    void getByNameAndCategoryName() {
        // product category существует
        when(productCategoriesRepo.getByName(PRODUCT_CAT_NAME)).thenReturn(Optional.of(productCategory));
        when(productsRepo.getProductByNameAndCategory(PRODUCT_NAME, productCategory)).thenReturn(Optional.of(product));
        when(modelMapper.map(any(Product.class), any())).thenReturn(productOutDTO);

        Optional<ProductOutDTO> resProduct = productsService.getByNameAndCategoryName(PRODUCT_NAME, PRODUCT_CAT_NAME);
        verify(productCategoriesRepo, times(1)).getByName(any(String.class));
        verify(productsRepo, times(1)).getProductByNameAndCategory(any(String.class), any(ProductCategory.class));

        assertTrue(resProduct.isPresent());
        assertEquals(PRODUCT_NAME, resProduct.get().getName());
        assertEquals(PRODUCT_EXTERNAL_ID, resProduct.get().getExternalId());
        assertNotNull(resProduct.get().getCategory());
        assertEquals(PRODUCT_CAT_NAME, resProduct.get().getCategory().getName());

        // product category не существует
        reset(productsRepo);
        reset(productCategoriesRepo);
        when(productCategoriesRepo.getByName(PRODUCT_CAT_NAME)).thenReturn(Optional.empty());
        assertThrows(ProductCategoryNotFoundException.class,
                () -> productsService.getByNameAndCategoryName(PRODUCT_NAME, PRODUCT_CAT_NAME));
        verify(productCategoriesRepo, times(1)).getByName(any(String.class));
        verify(productsRepo, times(0)).getProductByNameAndCategory(any(String.class), any(ProductCategory.class));
    }

    @Test
    void index() {
        when(productsRepo.findAll(any(Pageable.class))).thenReturn(repoIndex());
        when(modelMapper.map(any(Product.class), any())).thenReturn(new ProductOutDTO());

        List<ProductOutDTO> resProducts = productsService.index(0, 10);
        assertNotNull(resProducts);
        assertEquals(repoIndex().getContent().size(), resProducts.size());
        verify(productsRepo, times(1)).findAll(any(Pageable.class));
        verify(productsRepo, times(0)).findAll();
    }

    @Test
    void getByExternalId() {
        // product существует
        when(productsRepo.getByExternalId(PRODUCT_EXTERNAL_ID)).thenReturn(Optional.of(product));
        when(modelMapper.map(any(Product.class), any())).thenReturn(productOutDTO);

        ProductOutDTO resProduct = productsService.getByExternalId(PRODUCT_EXTERNAL_ID);
        verify(productsRepo, times(1)).getByExternalId(any(String.class));
        assertNotNull(resProduct);
        assertEquals(PRODUCT_NAME, resProduct.getName());
        assertEquals(PRODUCT_EXTERNAL_ID, resProduct.getExternalId());

        // product не существует
        reset(productsRepo);
        when(productsRepo.getByExternalId(PRODUCT_EXTERNAL_ID)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> productsService.getByExternalId(PRODUCT_EXTERNAL_ID));
    }

    private Page<Product> repoIndex() {
        return new PageImpl<>(List.of(
                new Product(),
                new Product(),
                new Product()
        ));
    }
}