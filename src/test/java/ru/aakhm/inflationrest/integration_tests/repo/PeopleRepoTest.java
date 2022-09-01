package ru.aakhm.inflationrest.integration_tests.repo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.aakhm.inflationrest.models.Person;
import ru.aakhm.inflationrest.models.PersonRole;
import ru.aakhm.inflationrest.repo.PeopleRepo;
import ru.aakhm.inflationrest.security.Role;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PeopleRepoTest {

    @Autowired
    PeopleRepo peopleRepo;

    private Person personUser;
    private Person personAdmin;

    private PersonRole roleUser;
    private PersonRole roleAdmin;

    private static final String LOGIN_USER = "user";
    private static final String EXTERNAL_ID = "ext1";

    @BeforeEach
    void setUp() {
        peopleRepo.deleteAll();
        peopleRepo.flush();

        roleUser = new PersonRole();
        roleUser.setId(1);
        roleUser.setName(Role.ROLE_USER.name());

        roleAdmin = new PersonRole();
        roleAdmin.setId(2);
        roleAdmin.setName(Role.ROLE_ADMIN.name());


        personUser = newPerson(1, "p1", roleUser, LOGIN_USER, "password",
                "UserFirst", "UserLast");
        personAdmin = newPerson(2, EXTERNAL_ID, roleAdmin, "admin", "password",
                "AdminFirst", "AdminLast");
        peopleRepo.saveAll(List.of(personUser, personAdmin));
    }

    @Test
    void getByLogin() {
        Optional<Person> resPerson = assertDoesNotThrow(() -> peopleRepo.getByLogin("user"));
        assertTrue(resPerson.isPresent());
        assertEquals(LOGIN_USER, resPerson.get().getLogin());
    }

    @Test
    void getByExternalId() {
        Optional<Person> resPerson = assertDoesNotThrow(() -> peopleRepo.getByExternalId(EXTERNAL_ID));
        assertTrue(resPerson.isPresent());
        assertEquals(EXTERNAL_ID, resPerson.get().getExternalId());
    }

    private Person newPerson(int id, String externalId, PersonRole role, String login, String password,
                             String firstName, String lastName) {
        Person res = new Person();
        res.setId(id);
        res.setExternalId(externalId);
        res.setRole(role);
        res.setLogin(login);
        res.setPassword(password);
        res.setFirstName(firstName);
        res.setLastName(lastName);
        return res;
    }
}