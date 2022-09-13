package ru.aakhm.inflationrest.integration_tests.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.aakhm.inflationrest.dto.in.PersonInDTO;
import ru.aakhm.inflationrest.dto.in.PersonLoginDTO;
import ru.aakhm.inflationrest.dto.out.PersonOutDTO;
import ru.aakhm.inflationrest.security.Role;
import ru.aakhm.inflationrest.services.PersonDetailsService;
import ru.aakhm.inflationrest.services.RegistrationService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@Sql(value = {"/sql/AuthControllerTest_before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private PersonDetailsService personDetailsService;

    // 2 пользователя с ролями USER и ADMIN уже создаются в базе из SQL скрипта
    // Это необходимо для работы @WithUserDetails
    // Дополнительно вручную создаём пользователей для возможности проверок performLogin (из-за пароля)
    private static final String EXT_ID_USER_DB = "person1";
    private static final String LOGIN_USER_DB = "user";
    private static final String LOGIN_ADMIN_DB = "admin";

    private static final String LOGIN_ADMIN = "admin2";
    private static final String FIRSTNAME_ADMIN = "AdminFirst";
    private static final String LASTNAME_ADMIN = "AdminLast";
    private static final String PASSWORD_WEAK = "12345678";
    private static final String PASSWORD_OK = "aBc%d1eF";

    private static final String LOGIN_USER = "user2";
    private static final String FIRSTNAME_USER = "UserFirst";
    private static final String LASTNAME_USER = "UserLast";

    private static final String LOGIN_NEW = "new";


    private static final String PATH = "/auth/";
    private static final String UNKNOWN_REF = "UNKNOWN";

    private PersonInDTO personInAdmin;
    private PersonOutDTO personOutAdmin;

    private PersonInDTO personInUser;
    private PersonOutDTO personOutUser;


    @BeforeEach
    void setUp() {
        personInAdmin = new PersonInDTO() {
            {
                setLogin(LOGIN_ADMIN);
                setPassword(PASSWORD_OK);
                setFirstName(FIRSTNAME_ADMIN);
                setLastName(LASTNAME_ADMIN);
            }
        };
        personOutAdmin = registrationService.register(personInAdmin);
        personDetailsService.assignRole(personOutAdmin.getExternalId(), Role.ROLE_ADMIN.name());

        personInUser = new PersonInDTO() {
            {
                setLogin(LOGIN_USER);
                setPassword(PASSWORD_OK);
                setFirstName(FIRSTNAME_USER);
                setLastName(LASTNAME_USER);
            }
        };
        personOutUser = registrationService.register(personInUser);
        personDetailsService.assignRole(personOutUser.getExternalId(), Role.ROLE_USER.name());
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void performRegistration() throws Exception {
        final String PATH_REG = PATH + "registration";
        // no body
        mockMvc.perform(post(PATH_REG).with(anonymous())).andDo(print()).andExpect(status().isBadRequest());
        // login exists
        mockMvc.perform(post(PATH_REG)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(personInUser))
                .with(anonymous())).andDo(print()).andExpect(status().isBadRequest());
        // weak password
        personInUser.setLogin(LOGIN_NEW);
        personInUser.setPassword(PASSWORD_WEAK);
        mockMvc.perform(post(PATH_REG)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(personInUser))
                .with(anonymous())).andDo(print()).andExpect(status().isBadRequest());
        // should be ok
        personInUser.setPassword(PASSWORD_OK);
        mockMvc.perform(post(PATH_REG)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(personInUser))
                .with(anonymous())).andDo(print()).andExpectAll(
                status().isCreated(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.login").value(LOGIN_NEW),
                jsonPath("$.firstName").value(FIRSTNAME_USER),
                jsonPath("$.lastName").value(LASTNAME_USER),
                jsonPath("$.externalId").isNotEmpty()
        );
    }

    @Test
    void update_notAllowedAsNotLoggedIn() throws Exception {
        final String PATH_UPDATE = PATH + "update/" + personOutUser.getExternalId();
        mockMvc.perform(put(PATH_UPDATE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(personInUser))
                .with(anonymous())).andDo(print()).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("user")
    void update_notAllowedForPermissions() throws Exception {
        final String PATH_UPDATE = PATH + "update/" + personOutAdmin.getExternalId();
        mockMvc.perform(put(PATH_UPDATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(personInAdmin)))
                .andDo(print()).andExpect(status().isBadRequest());

    }

    @Test
    @WithUserDetails(LOGIN_USER_DB)
    void update_OkForTheSamePerson() throws Exception {
        final String PATH_UPDATE = PATH + "update/" + EXT_ID_USER_DB;
        // поле PersonInDTO.login игнорируется по дизайну при обновлении Person, поэтому не нужно менять в personInUser
        mockMvc.perform(put(PATH_UPDATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(personInUser)))
                .andDo(print()).andExpect(status().isBadRequest());

    }

    @Test
    @WithUserDetails(LOGIN_ADMIN_DB)
    void update_OkIfPerformedByAdmin() throws Exception {
        final String PATH_UPDATE = PATH + "update/" + EXT_ID_USER_DB;
        mockMvc.perform(put(PATH_UPDATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(personInUser)))
                .andDo(print()).andExpect(status().isBadRequest());

    }

    @Test
    void performLogin() throws Exception {
        final String PATH_LOGIN = PATH + "/login";
        // no body
        mockMvc.perform(post(PATH_LOGIN).with(anonymous())).andDo(print()).andExpect(status().isBadRequest());

        PersonLoginDTO loginIn = new PersonLoginDTO() {
            {
                setLogin(UNKNOWN_REF);
                setPassword(PASSWORD_OK);
            }
        };
        // wrong login
        mockMvc.perform(post(PATH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginIn))
                .with(anonymous())).andDo(print()).andExpect(status().isBadRequest());

        // wrong password
        loginIn.setLogin(LOGIN_USER);
        loginIn.setPassword(PASSWORD_WEAK);
        mockMvc.perform(post(PATH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginIn))
                .with(anonymous())).andDo(print()).andExpect(status().isBadRequest());

        // should be ok
        loginIn.setPassword(PASSWORD_OK);
        mockMvc.perform(post(PATH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginIn))
                .with(anonymous())).andDo(print()).andExpect(
                status().isOk());
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}