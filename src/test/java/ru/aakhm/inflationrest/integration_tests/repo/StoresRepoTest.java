package ru.aakhm.inflationrest.integration_tests.repo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import ru.aakhm.inflationrest.models.Store;
import ru.aakhm.inflationrest.repo.StoresRepo;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource("/application-test.properties")
class StoresRepoTest {
    @Autowired
    StoresRepo storesRepo;
    public static final String SQL_BEFORE_TEST = "/sql/StoresRepoTest_before.sql";

    private static final String NAME = "Пятёрочка";
    private static final String EXTERNAL_ID = "s1";
    private static final String UNKNOWN_REF = "UNKNOWN";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @Sql(value = {SQL_BEFORE_TEST}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getByName() {
        Optional<Store> res = assertDoesNotThrow(() -> storesRepo.getByName(NAME));
        assertTrue(res.isPresent());
        assertEquals(NAME, res.get().getName());

        res = assertDoesNotThrow(() -> storesRepo.getByName(UNKNOWN_REF));
        assertFalse(res.isPresent());
    }

    @Test
    @Sql(value = {SQL_BEFORE_TEST}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getByExternalId() {
        Optional<Store> res = assertDoesNotThrow(() -> storesRepo.getByExternalId(EXTERNAL_ID));
        assertTrue(res.isPresent());
        assertEquals(EXTERNAL_ID, res.get().getExternalId());

        res = assertDoesNotThrow(() -> storesRepo.getByExternalId(UNKNOWN_REF));
        assertFalse(res.isPresent());
    }
}