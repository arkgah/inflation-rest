package ru.aakhm.inflationrest.integration_tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
class StoresControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }


    @Test
    @Sql(value = {"/StoresControllerTest_before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void index() throws Exception {
        mockMvc.perform(get("/stores").with(anonymous())).andDo(print()).andExpect(status().isForbidden());

        mockMvc.perform(get("/stores").with(user("admin").roles("ADMIN"))).andDo(print()).andExpect(status().isOk());

        mockMvc.perform(get("/stores").with(user("user").roles("USER"))).andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.stores").isArray(),
                        jsonPath("$.stores", hasSize(3)),
                        jsonPath("$.stores[0:].name", hasItem("Пятёрочка")),
                        jsonPath("$.stores[0:].name", hasItem("Окей")),
                        jsonPath("$.stores[0:].name", hasItem("Лента")));
    }

    @Test
    void getById() {
    }

    @Test
    void getByName() {
    }

    @Test
    void create() {
    }

    @Test
    void delete() {
    }

    @Test
    void update() {
    }
}