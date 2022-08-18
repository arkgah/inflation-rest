package ru.aakhm.inflationrest.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import ru.aakhm.inflationrest.dto.in.ProductCategoryInDTO;
import ru.aakhm.inflationrest.dto.in.ProductInDTO;
import ru.aakhm.inflationrest.dto.out.ProductCategoryOutDTO;
import ru.aakhm.inflationrest.dto.out.ProductOutDTO;
import ru.aakhm.inflationrest.models.Product;
import ru.aakhm.inflationrest.models.ProductCategory;
import ru.aakhm.inflationrest.repo.ProductCategoriesRepo;
import ru.aakhm.inflationrest.repo.ProductsRepo;
import ru.aakhm.inflationrest.utils.Utils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save() {
        final String EXTERNAL_ID = "123abc";
        final String PRODUCT_NAME = "Test Product";
        final String PRODUCT_CAT_NAME = "Test Category";
        final double UNIT = 0.5;

        ProductInDTO pInDTO = new ProductInDTO() {
            {
                setName(PRODUCT_NAME);
                setUnit(UNIT);
                setCategory(new ProductCategoryInDTO() {
                    {
                        setName(PRODUCT_NAME);
                    }
                });
            }
        };

        Product product = new Product() {
            {
                setName(PRODUCT_NAME);
                setExternalId(EXTERNAL_ID);
            }
        };

        ProductCategory productCategory = new ProductCategory() {
            {
                setName(PRODUCT_CAT_NAME);
            }
        };

        ProductOutDTO pOutDTO = new ProductOutDTO() {
            {
                setName(PRODUCT_NAME);
                setCategory(new ProductCategoryOutDTO() {
                    {
                        setName(PRODUCT_CAT_NAME);
                    }
                });
                setExternalId(EXTERNAL_ID);
            }
        };

        when(productsRepo.save(any(Product.class))).thenReturn(product);
        when(utils.generateExternalId()).thenReturn(EXTERNAL_ID);

        when(productCategoriesRepo.getByName(any(String.class))).thenReturn(Optional.of(productCategory));

        when(modelMapper.map(any(ProductInDTO.class), any())).thenReturn(product);
        when(modelMapper.map(any(Product.class), any())).thenReturn(pOutDTO);

        ProductOutDTO resProduct = productsService.save(pInDTO);
        verify(productsRepo, times(1)).save(any(Product.class));
        verify(modelMapper, times(1)).map(any(ProductInDTO.class), any());
        verify(modelMapper, times(1)).map(any(Product.class), any());

        assertNotNull(resProduct);
        assertEquals(PRODUCT_NAME, resProduct.getName());
        assertEquals(EXTERNAL_ID, resProduct.getExternalId());

        assertNotNull(resProduct.getCategory());
        assertEquals(PRODUCT_CAT_NAME, resProduct.getCategory().getName());
    }

    @Test
    void update() {
    }

    @Test
    void deleteByExternalId() {
    }

    @Test
    void getByNameAndCategoryName() {
    }

    @Test
    void index() {
    }

    @Test
    void testIndex() {
    }

    @Test
    void getByExternalId() {
    }
}