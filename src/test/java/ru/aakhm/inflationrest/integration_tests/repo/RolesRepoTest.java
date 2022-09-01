package ru.aakhm.inflationrest.integration_tests.repo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.aakhm.inflationrest.models.PersonRole;
import ru.aakhm.inflationrest.repo.RolesRepo;
import ru.aakhm.inflationrest.security.Role;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RolesRepoTest {

    @Autowired
    RolesRepo rolesRepo;

    private PersonRole roleUser;
    private PersonRole roleAdmin;

    @BeforeEach
    void setUp() {
        rolesRepo.deleteAll();
        rolesRepo.flush();

        roleUser = new PersonRole();
        roleUser.setId(1);
        roleUser.setName(Role.ROLE_USER.name());

        roleAdmin = new PersonRole();
        roleAdmin.setId(2);
        roleAdmin.setName(Role.ROLE_ADMIN.name());

        rolesRepo.saveAll(List.of(roleUser, roleAdmin));
    }

    @AfterEach
    void tearDown() {
        rolesRepo.deleteAll();
        rolesRepo.flush();
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