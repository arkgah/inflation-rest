package ru.aakhm.inflationrest.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

class PurchasesServiceTest {
    private final String PURCHASE_EXTERNAL_ID = "prch987";
    private final BigDecimal PURCHASE_PRICE = BigDecimal.valueOf(123.45);
    private final String PRODUCT_EXTERNAL_ID = "123abc";
    private final String PRODUCT_NAME = "Test Product";
    private final String PRODUCT_CAT_NAME = "Test Category";
    private final String PRODUCT_CAT_EXTERNAL_ID = "456def";
    private final Double UNIT = 0.5;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save() {
    }

    @Test
    void saveForPerson() {
    }

    @Test
    void update() {
    }

    @Test
    void deleteByExternalId() {
    }

    @Test
    void index() {
    }

    @Test
    void getByExternalId() {
    }
}