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
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import ru.aakhm.inflationrest.models.Person;
import ru.aakhm.inflationrest.models.Product;
import ru.aakhm.inflationrest.models.Purchase;
import ru.aakhm.inflationrest.models.Store;
import ru.aakhm.inflationrest.repo.PurchasesRepo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource("/application-test.properties")
class PurchasesRepoTest {

    private static final int PER_PAGE = 10;
    private static final int PURCHASES_TOTAL = 3;
    private static final String EXTERNAL_ID = "prc1";
    private Date date1;
    private Date date2;

    @Autowired
    private PurchasesRepo purchasesRepo;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() throws ParseException {
        date1 = new SimpleDateFormat("yyyy-M-dd hh:mm").parse("2022-09-01 09:00");
        date2 = new SimpleDateFormat("yyyy-M-dd hh:mm").parse("2022-10-01 09:00");
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @Sql(value = {"/PurchasesRepoTest_before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void findAll() {
        Page<Purchase> purchases = assertDoesNotThrow(() -> purchasesRepo.findAll(PageRequest.of(0, PER_PAGE,
                Sort.by(Sort.Direction.ASC, "purchasedAt"))));
        assertNotNull(purchases);
        List<Purchase> res = purchases.getContent();

        Assertions.assertThat(res)
                .hasSize(PURCHASES_TOTAL)
                .extracting(Purchase::getPurchasedAt)
                .isSortedAccordingTo(Comparator.naturalOrder());

        purchases = assertDoesNotThrow(() -> purchasesRepo.findAll(PageRequest.of(0, PER_PAGE,
                Sort.by(Sort.Direction.DESC, "purchasedAt"))));
        assertNotNull(purchases);
        res = purchases.getContent();

        Assertions.assertThat(res)
                .hasSize(PURCHASES_TOTAL)
                .extracting(Purchase::getPurchasedAt)
                .isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    @Sql(value = {"/PurchasesRepoTest_before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getByExternalId() {
        Optional<Purchase> res = assertDoesNotThrow(() -> purchasesRepo.getByExternalId(EXTERNAL_ID));
        assertNotNull(res);
        assertTrue(res.isPresent());

        res = assertDoesNotThrow(() -> purchasesRepo.getByExternalId("NOT_EXISTS"));
        assertNotNull(res);
        assertFalse(res.isPresent());

    }

    @Test
    @Sql(value = {"/PurchasesRepoTest_before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getAllByPurchasedAtBetween() {
        List<Purchase> res = assertDoesNotThrow(() -> purchasesRepo.getAllByPurchasedAtBetween(date1, date2));
        assertNotNull(res);
        assertEquals(2, res.size());
    }

    @Test
    @Sql(value = {"/PurchasesRepoTest_before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getByPurchasedAtAndProductAndStoreAndPerson() {
        Person personUser = entityManager.find(Person.class, 1);
        Person personAdmin = entityManager.find(Person.class, 2);
        Product product = entityManager.find(Product.class, 1);
        Store store = entityManager.find(Store.class, 1);
        Optional<Purchase> res = assertDoesNotThrow(() -> purchasesRepo.getByPurchasedAtAndProductAndStoreAndPerson(date1, product, store, personUser));
        assertNotNull(res);
        assertTrue(res.isPresent());

        res = assertDoesNotThrow(() -> purchasesRepo.getByPurchasedAtAndProductAndStoreAndPerson(date1, product, store, personAdmin));
        assertNotNull(res);
        assertFalse(res.isPresent());
    }
}