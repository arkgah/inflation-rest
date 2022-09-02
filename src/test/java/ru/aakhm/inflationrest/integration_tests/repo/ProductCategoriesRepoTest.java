package ru.aakhm.inflationrest.integration_tests.repo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.aakhm.inflationrest.models.ProductCategory;
import ru.aakhm.inflationrest.repo.ProductCategoriesRepo;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductCategoriesRepoTest {
    @Autowired
    private ProductCategoriesRepo productCategoriesRepo;

    private ProductCategory category;

    private static final String NAME = "Test";
    private static final String EXTERNAL_ID = "123abc";

    @BeforeEach
    void setUp() {
        productCategoriesRepo.deleteAll();
        productCategoriesRepo.flush();

        category = new ProductCategory();
        category.setId(1);
        category.setName(NAME);
        category.setExternalId(EXTERNAL_ID);

        productCategoriesRepo.save(category);
    }

    @AfterEach
    void tearDown() {
        productCategoriesRepo.deleteAll();
        productCategoriesRepo.flush();
    }

    @Test
    void getByName() {
        Optional<ProductCategory> res = assertDoesNotThrow(() -> productCategoriesRepo.getByName(NAME));
        assertTrue(res.isPresent());
        assertEquals(NAME, res.get().getName());
    }

    @Test
    void getByExternalId() {
        Optional<ProductCategory> res = assertDoesNotThrow(() -> productCategoriesRepo.getByExternalId(EXTERNAL_ID));
        assertTrue(res.isPresent());
        assertEquals(EXTERNAL_ID, res.get().getExternalId());
    }
}