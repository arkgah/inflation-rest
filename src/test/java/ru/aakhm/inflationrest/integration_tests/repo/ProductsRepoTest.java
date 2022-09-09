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
import ru.aakhm.inflationrest.models.Product;
import ru.aakhm.inflationrest.models.ProductCategory;
import ru.aakhm.inflationrest.repo.ProductsRepo;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductsRepoTest {

    @Autowired
    ProductsRepo productsRepo;

    @Autowired
    private TestEntityManager entityManager;

    private ProductCategory pc1;
    private ProductCategory pc2;

    private Product p1;
    private Product p2;


    @BeforeEach
    void setUp() {
        pc1 = new ProductCategory();
        pc1.setName("Cat1");
        pc1.setExternalId("cat1");
        pc1 = entityManager.persistAndFlush(pc1);

        pc2 = new ProductCategory();
        pc2.setName("Cat2");
        pc2.setExternalId("cat2");
        pc2 = entityManager.persistAndFlush(pc2);

        p1 = new Product();
        p1.setCategory(pc1);
        p1.setUnit(1.);
        p1.setName("P1");
        p1.setExternalId("p1");

        p2 = new Product();
        p2.setCategory(pc2);
        p2.setUnit(1.);
        p2.setName("P2");
        p2.setExternalId("p2");

        pc1.setProducts(List.of(p1));
        pc2.setProducts(List.of(p2));

        entityManager.persistAndFlush(p1);
        entityManager.persistAndFlush(p2);
    }

    @AfterEach
    void tearDown() {
        productsRepo.deleteAll();
        productsRepo.flush();
    }

    @Test
    void getProductByNameAndCategory() {
        Optional<Product> res = assertDoesNotThrow(() -> productsRepo.getProductByNameAndCategory("P1", pc1));
        assertTrue(res.isPresent());
        assertEquals("P1", res.get().getName());

        // wrong name
        res = assertDoesNotThrow(() -> productsRepo.getProductByNameAndCategory("Absent Name", pc1));
        assertFalse(res.isPresent());

        // wrong category
        res = assertDoesNotThrow(() -> productsRepo.getProductByNameAndCategory("P1", pc2));
        assertFalse(res.isPresent());
    }

    @Test
    void getByExternalId() {
        Optional<Product> res = assertDoesNotThrow(() -> productsRepo.getByExternalId("p1"));
        assertTrue(res.isPresent());
        assertEquals("p1", res.get().getExternalId());

        res = assertDoesNotThrow(() -> productsRepo.getByExternalId("AbsentExtId"));
        assertFalse(res.isPresent());
    }

    @Test
    void getAllByNameContainingIgnoreCaseAndCategory_NameContainingIgnoreCase() {
        Page<Product> res = assertDoesNotThrow(() -> productsRepo.getAllByNameContainingIgnoreCaseAndCategory_NameContainingIgnoreCase(
                PageRequest.of(0, 10),
                "p", "c"
        ));
        List<Product> products = res.getContent();

        Assertions.assertThat(products)
                .hasSize(2)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("P1", "P2");

        // strict by category
        res = assertDoesNotThrow(() -> productsRepo.getAllByNameContainingIgnoreCaseAndCategory_NameContainingIgnoreCase(
                PageRequest.of(0, 10),
                "p", "Cat1"
        ));
        products = res.getContent();
        assertEquals(1, products.size());

        // no products
        res = assertDoesNotThrow(() -> productsRepo.getAllByNameContainingIgnoreCaseAndCategory_NameContainingIgnoreCase(
                PageRequest.of(0, 10),
                "Unknown Product", "Cat1"
        ));
        products = res.getContent();
        assertEquals(0, products.size());

    }
}