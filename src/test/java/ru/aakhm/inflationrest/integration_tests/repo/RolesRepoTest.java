package ru.aakhm.inflationrest.integration_tests.repo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import ru.aakhm.inflationrest.models.PersonRole;
import ru.aakhm.inflationrest.repo.RolesRepo;
import ru.aakhm.inflationrest.security.Role;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource("/application-test.properties")
@Sql(value = "/sql/RolesTest_before.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class RolesRepoTest {
    @Autowired
    RolesRepo rolesRepo;

    private PersonRole roleUser;
    private PersonRole roleAdmin;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getByName() {
        Optional<PersonRole> resUserRole = assertDoesNotThrow(() -> rolesRepo.getByName(Role.ROLE_USER.name()));
        assertTrue(resUserRole.isPresent());
        assertEquals(Role.ROLE_USER.name(), resUserRole.get().getName());

        Optional<PersonRole> resAdminRole = assertDoesNotThrow(() -> rolesRepo.getByName(Role.ROLE_ADMIN.name()));
        assertTrue(resAdminRole.isPresent());
        assertEquals(Role.ROLE_ADMIN.name(), resAdminRole.get().getName());
    }
}