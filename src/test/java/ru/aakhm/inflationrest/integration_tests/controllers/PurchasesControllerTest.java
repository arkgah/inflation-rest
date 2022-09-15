package ru.aakhm.inflationrest.integration_tests.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@Sql(value = {"/sql/PurchasesTest_before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PurchasesControllerTest {
    @Autowired
    MockMvc mockMvc;

    public static final String PATH = "/purchases/";

    @Test
    void create() {
    }

    @Test
    void update() {
    }

    @Test
    void delete() {
    }

    @Test
    void index() throws Exception {
        mockMvc.perform(get(PATH).with(anonymous())).andDo(print()).andExpect(status().isForbidden());
        mockMvc.perform(get(PATH).with(user("admin").roles("ADMIN"))).andDo(print()).andExpect(status().isOk());
        mockMvc.perform(get(PATH).with(user("user").roles("USER"))).andDo(print()).andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.purchases").isNotEmpty(),
                jsonPath("$.purchases").isArray(),
                jsonPath("$.purchases", hasSize(3)),
                jsonPath("$.purchases[0:].product.name", hasItem("Хлеб ржаной"))
        );

        int page = 0;
        int per_page = 2;
        boolean sorted_asc = false;
        final String PATH_FORMAT = PATH + "?page=%d&per-page=%d&sort-asc=%b";
        mockMvc.perform(get(String.format(PATH_FORMAT, page, per_page, sorted_asc))
                .with(user("user").roles("USER"))).andDo(print()).andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.purchases").isNotEmpty(),
                jsonPath("$.purchases").isArray(),
                jsonPath("$.purchases", hasSize(2)),
                jsonPath("$.purchases[0:].purchasedAt",
                        containsInRelativeOrder(startsWith("2022-11"), startsWith("2022-10")))
        );

        sorted_asc = true;
        mockMvc.perform(get(String.format(PATH_FORMAT, page, per_page, sorted_asc))
                .with(user("user").roles("USER"))).andDo(print()).andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.purchases").isNotEmpty(),
                jsonPath("$.purchases").isArray(),
                jsonPath("$.purchases", hasSize(2)),
                jsonPath("$.purchases[0:].purchasedAt",
                        containsInRelativeOrder(startsWith("2022-09"), startsWith("2022-10")))
        );

    }

    @Test
    void getByExternalId() {
    }
}