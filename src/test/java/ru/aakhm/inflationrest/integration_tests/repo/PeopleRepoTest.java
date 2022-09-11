package ru.aakhm.inflationrest.integration_tests.repo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import ru.aakhm.inflationrest.models.Person;
import ru.aakhm.inflationrest.repo.PeopleRepo;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource("/application-test.properties")
@Sql(value = {"/sql/PeopleTest_before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PeopleRepoTest {
    @Autowired
    PeopleRepo peopleRepo;
    private static final String LOGIN_USER = "user";
    private static final String EXTERNAL_ID = "person1";

    private static final String UNKNOWN_REF = "UNKNOWN";

    @BeforeEach
    void setUp() {
    }

    @Test
    void getByLogin() {
        Optional<Person> resPerson = assertDoesNotThrow(() -> peopleRepo.getByLogin(LOGIN_USER));
        assertTrue(resPerson.isPresent());
        assertEquals(LOGIN_USER, resPerson.get().getLogin());

        resPerson = assertDoesNotThrow(() -> peopleRepo.getByLogin(UNKNOWN_REF));
        assertFalse(resPerson.isPresent());
    }

    @Test
    void getByExternalId() {
        Optional<Person> resPerson = assertDoesNotThrow(() -> peopleRepo.getByExternalId(EXTERNAL_ID));
        assertTrue(resPerson.isPresent());
        assertEquals(EXTERNAL_ID, resPerson.get().getExternalId());

        resPerson = assertDoesNotThrow(() -> peopleRepo.getByExternalId(UNKNOWN_REF));
        assertFalse(resPerson.isPresent());
    }
}