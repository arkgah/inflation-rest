package ru.aakhm.inflationrest.integration_tests.repo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import ru.aakhm.inflationrest.models.ProductCategory;
import ru.aakhm.inflationrest.repo.ProductCategoriesRepo;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource("/application-test.properties")
class ProductCategoriesRepoTest {
    @Autowired
    private ProductCategoriesRepo productCategoriesRepo;

    private static final String NAME = "Продукты";
    private static final String EXTERNAL_ID = "pc1";
    private static final String UNKNOWN_REF = "UNKNOWN";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @Sql(value = {"/sql/ProductCategoriesRepoTest_before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getByName() {
        Optional<ProductCategory> res = assertDoesNotThrow(() -> productCategoriesRepo.getByName(NAME));
        assertTrue(res.isPresent());
        assertEquals(NAME, res.get().getName());

        res = assertDoesNotThrow(() -> productCategoriesRepo.getByName(UNKNOWN_REF));
        assertFalse(res.isPresent());
    }

    @Test
    @Sql(value = {"/sql/ProductCategoriesRepoTest_before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getByExternalId() {
        Optional<ProductCategory> res = assertDoesNotThrow(() -> productCategoriesRepo.getByExternalId(EXTERNAL_ID));
        assertTrue(res.isPresent());
        assertEquals(EXTERNAL_ID, res.get().getExternalId());

        res = assertDoesNotThrow(() -> productCategoriesRepo.getByExternalId(UNKNOWN_REF));
        assertFalse(res.isPresent());
    }
}