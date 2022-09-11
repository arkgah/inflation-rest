package ru.aakhm.inflationrest.integration_tests.repo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import ru.aakhm.inflationrest.models.Product;
import ru.aakhm.inflationrest.models.ProductCategory;
import ru.aakhm.inflationrest.repo.ProductsRepo;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource("/application-test.properties")
@Sql(value = "/sql/ProductsTest_before.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ProductsRepoTest {
    @Autowired
    ProductsRepo productsRepo;

    @Autowired
    private TestEntityManager entityManager;

    private static final String PRODUCT_NAME1 = "Хлеб ржаной";
    private static final String PRODUCT_EXTERNAL_ID1 = "p1";

    private static final String PRODUCT_NAME2 = "Хлеб белый";
    private static final String PRODUCT_EXTERNAL_ID2 = "p2";

    private static final String PRODUCT_NAME_LIKE = "хлеб";

    private static final String UNKNOWN_REF = "UNKNOWN";

    private static final int PRODUCT_CAT_EXTERNAL_ID1 = 1;
    private static final int PRODUCT_CAT_EXTERNAL_ID2 = 2;

    private static final String PRODUCT_CAT_NAME1 = "Продукты";
    private static final String PRODUCT_CAT_NAME_LIKE = "прод";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getProductByNameAndCategory() {
        ProductCategory pc1 = entityManager.find(ProductCategory.class, PRODUCT_CAT_EXTERNAL_ID1);
        ProductCategory pc2 = entityManager.find(ProductCategory.class, PRODUCT_CAT_EXTERNAL_ID2);

        Optional<Product> res = assertDoesNotThrow(() -> productsRepo.getProductByNameAndCategory(PRODUCT_NAME1, pc1));
        assertTrue(res.isPresent());
        assertEquals(PRODUCT_NAME1, res.get().getName());

        // wrong name
        res = assertDoesNotThrow(() -> productsRepo.getProductByNameAndCategory(UNKNOWN_REF, pc1));
        assertFalse(res.isPresent());

        // wrong category
        res = assertDoesNotThrow(() -> productsRepo.getProductByNameAndCategory(PRODUCT_NAME1, pc2));
        assertFalse(res.isPresent());
    }

    @Test
    void getByExternalId() {
        Optional<Product> res = assertDoesNotThrow(() -> productsRepo.getByExternalId(PRODUCT_EXTERNAL_ID1));
        assertTrue(res.isPresent());
        assertEquals(PRODUCT_EXTERNAL_ID1, res.get().getExternalId());

        res = assertDoesNotThrow(() -> productsRepo.getByExternalId(UNKNOWN_REF));
        assertFalse(res.isPresent());
    }

    @Test
    void getAllByNameContainingIgnoreCaseAndCategory_NameContainingIgnoreCase() {
        Page<Product> res = assertDoesNotThrow(() -> productsRepo.getAllByNameContainingIgnoreCaseAndCategory_NameContainingIgnoreCase(
                PageRequest.of(0, 10),
                PRODUCT_NAME_LIKE, PRODUCT_CAT_NAME_LIKE
        ));
        List<Product> products = res.getContent();

        Assertions.assertThat(products)
                .hasSize(2)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder(PRODUCT_NAME1, PRODUCT_NAME2);

        // strict by product
        res = assertDoesNotThrow(() -> productsRepo.getAllByNameContainingIgnoreCaseAndCategory_NameContainingIgnoreCase(
                PageRequest.of(0, 10),
                PRODUCT_NAME1, PRODUCT_CAT_NAME_LIKE
        ));
        products = res.getContent();
        assertEquals(1, products.size());

        // strict by product and category
        res = assertDoesNotThrow(() -> productsRepo.getAllByNameContainingIgnoreCaseAndCategory_NameContainingIgnoreCase(
                PageRequest.of(0, 10),
                PRODUCT_NAME1, PRODUCT_CAT_NAME1
        ));
        products = res.getContent();
        assertEquals(1, products.size());

        // no products
        res = assertDoesNotThrow(() -> productsRepo.getAllByNameContainingIgnoreCaseAndCategory_NameContainingIgnoreCase(
                PageRequest.of(0, 10),
                UNKNOWN_REF, PRODUCT_CAT_NAME_LIKE
        ));
        products = res.getContent();
        assertEquals(0, products.size());
    }
}