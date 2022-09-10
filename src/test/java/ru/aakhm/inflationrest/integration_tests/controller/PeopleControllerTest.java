package ru.aakhm.inflationrest.integration_tests.controller;

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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.aakhm.inflationrest.security.Role;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
class PeopleControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private static final String SQL_BEFORE_TEST = "/sql/PeopleRepoTest_before.sql";

    private static final int PEOPLE_COUNT = 2;
    private static final String LOGIN_USER = "user";
    private static final String ROLE_USER = "USER";
    private static final String EXT_ID_USER = "person1";
    private static final String UNKNOWN_REF = "UNKNOWN";

    private static final String LOGIN_ADMIN = "admin";
    private static final String ROLE_ADMIN = "ADMIN";
    public static final String EXT_ID_ADMIN = "person2";


    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @Sql(value = {SQL_BEFORE_TEST}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void index() throws Exception {
        mockMvc.perform(get("/people").with(anonymous())).andDo(print()).andExpect(status().isForbidden());
        mockMvc.perform(get("/people").with(user(LOGIN_USER).roles(ROLE_USER))).andDo(print()).andExpect(status().isBadRequest());

        mockMvc.perform(get("/people").with(user(LOGIN_ADMIN).roles(ROLE_ADMIN))).andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.people").isArray(),
                        jsonPath("$.people", hasSize(PEOPLE_COUNT)),
                        jsonPath("$.people[0:].login", hasItem(LOGIN_USER)),
                        jsonPath("$.people[0:].login", hasItem(LOGIN_ADMIN))
                );
    }

    @Test
    @Sql(value = {SQL_BEFORE_TEST}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void delete() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/people/" + EXT_ID_USER)
                .with(anonymous())).andDo(print()).andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.delete("/people/" + EXT_ID_USER)
                .with(user(LOGIN_USER).roles(ROLE_USER))).andDo(print()).andExpect(status().isBadRequest());
        mockMvc.perform(MockMvcRequestBuilders.delete("/people/" + EXT_ID_USER)
                .with(user(LOGIN_ADMIN).roles(ROLE_ADMIN))).andDo(print()).andExpect(status().isNoContent());

        mockMvc.perform(MockMvcRequestBuilders.delete("/people/" + UNKNOWN_REF)
                .with(user(LOGIN_ADMIN).roles(ROLE_ADMIN))).andDo(print()).andExpect(status().isNotFound());

    }

    @Test
    @Sql(value = {SQL_BEFORE_TEST}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @WithUserDetails(LOGIN_USER)
        // https://docs.spring.io/spring-security/site/docs/4.0.x/reference/htmlsingle/#test-method-withuserdetails
    void getByExternalId_forUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/people/" + EXT_ID_USER)
                .with(anonymous())).andDo(print()).andExpect(status().isForbidden());

        mockMvc.perform(MockMvcRequestBuilders.get("/people/" + EXT_ID_USER))
                .andDo(print()).andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.externalId").isNotEmpty(),
                        jsonPath("$.externalId", is(EXT_ID_USER))
                );

        mockMvc.perform(MockMvcRequestBuilders.get("/people/" + EXT_ID_ADMIN))
                .andDo(print()).andExpect(status().isBadRequest());
    }

    @Test
    @Sql(value = {SQL_BEFORE_TEST}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @WithUserDetails(LOGIN_ADMIN)
        // https://docs.spring.io/spring-security/site/docs/4.0.x/reference/htmlsingle/#test-method-withuserdetails
    void getByExternalId_forAdmin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/people/" + EXT_ID_USER)
                .with(anonymous())).andDo(print()).andExpect(status().isForbidden());

        mockMvc.perform(MockMvcRequestBuilders.get("/people/" + EXT_ID_USER))
                .andDo(print()).andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.externalId").isNotEmpty(),
                        jsonPath("$.externalId", is(EXT_ID_USER))
                );

        mockMvc.perform(MockMvcRequestBuilders.get("/people/" + EXT_ID_ADMIN))
                .andDo(print()).andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.externalId").isNotEmpty(),
                        jsonPath("$.externalId", is(EXT_ID_ADMIN))
                );
    }


    @Test
    @Sql(value = {SQL_BEFORE_TEST}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @WithUserDetails(LOGIN_USER)
    void getByLogin_forUser() throws Exception {
        final String PATH = "/people/login/";
        mockMvc.perform(MockMvcRequestBuilders.get(PATH + LOGIN_USER)
                .with(anonymous())).andDo(print()).andExpect(status().isForbidden());

        mockMvc.perform(MockMvcRequestBuilders.get(PATH + LOGIN_USER))
                .andDo(print()).andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.login").isNotEmpty(),
                        jsonPath("$.login", is(LOGIN_USER))
                );

        mockMvc.perform(MockMvcRequestBuilders.get(PATH + LOGIN_ADMIN))
                .andDo(print()).andExpect(status().isBadRequest());
    }

    @Test
    @Sql(value = {SQL_BEFORE_TEST}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @WithUserDetails(LOGIN_ADMIN)
    void getByLogin_forAdmin() throws Exception {
        final String PATH = "/people/login/";
        mockMvc.perform(MockMvcRequestBuilders.get(PATH + LOGIN_ADMIN)
                .with(anonymous())).andDo(print()).andExpect(status().isForbidden());

        mockMvc.perform(MockMvcRequestBuilders.get(PATH + LOGIN_USER))
                .andDo(print()).andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.login").isNotEmpty(),
                        jsonPath("$.login", is(LOGIN_USER))
                );

        mockMvc.perform(MockMvcRequestBuilders.get(PATH + LOGIN_ADMIN))
                .andDo(print()).andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.login").isNotEmpty(),
                        jsonPath("$.login", is(LOGIN_ADMIN))
                );
    }

    @Test
    @Sql(value = {SQL_BEFORE_TEST}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void assignRole() throws Exception {
        final String PATH_FORMAT = "/people/%s/role/%s";
        mockMvc.perform(MockMvcRequestBuilders.patch(String.format(PATH_FORMAT, EXT_ID_USER, ROLE_ADMIN))
                .with(anonymous())).andDo(print()).andExpect(status().isForbidden());

        mockMvc.perform(MockMvcRequestBuilders.patch(String.format(PATH_FORMAT, EXT_ID_USER, ROLE_ADMIN))
                        .with(user(LOGIN_USER).roles(ROLE_USER)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.patch(String.format(PATH_FORMAT, EXT_ID_USER, Role.ROLE_ADMIN.name()))
                        .with(user(LOGIN_ADMIN).roles(ROLE_ADMIN)))
                .andDo(print())
                .andExpect(status().isOk());
    }
}