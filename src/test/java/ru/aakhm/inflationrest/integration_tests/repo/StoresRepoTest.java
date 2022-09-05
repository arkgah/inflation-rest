package ru.aakhm.inflationrest.integration_tests.repo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.aakhm.inflationrest.models.Store;
import ru.aakhm.inflationrest.repo.StoresRepo;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StoresRepoTest {
    @Autowired
    StoresRepo storesRepo;

    private static final String NAME = "Test";
    private static final String NAME_DOESNT_EXIST = "Test2";

    private static final String EXTERNAL_ID = "123abc";
    private static final String EXTERNAL_ID_DOESNT_EXIST = "bca321";

    @BeforeEach
    void setUp() {
        storesRepo.deleteAll();
        storesRepo.flush();

        Store store = new Store();
        store.setName(NAME);
        store.setId(1);
        store.setExternalId(EXTERNAL_ID);

        storesRepo.save(store);
    }

    @AfterEach
    void tearDown() {
        storesRepo.deleteAll();
        storesRepo.flush();
    }

    @Test
    void getByName() {
        Optional<Store> res = assertDoesNotThrow(() -> storesRepo.getByName(NAME));
        assertTrue(res.isPresent());
        assertEquals(NAME, res.get().getName());

        res = assertDoesNotThrow(() -> storesRepo.getByName(NAME_DOESNT_EXIST));
        assertFalse(res.isPresent());
    }

    @Test
    void getByExternalId() {
        Optional<Store> res = assertDoesNotThrow(() -> storesRepo.getByExternalId(EXTERNAL_ID));
        assertTrue(res.isPresent());
        assertEquals(EXTERNAL_ID, res.get().getExternalId());

        res = assertDoesNotThrow(() -> storesRepo.getByExternalId(EXTERNAL_ID_DOESNT_EXIST));
        assertFalse(res.isPresent());
    }
}