package ru.aakhm.inflationrest.integration_tests.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.aakhm.inflationrest.dto.in.ProductCategoryInDTO;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@Sql(value = {"/sql/ProductCategoriesTest_before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ProductCategoriesControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private static final String PATH = "/products/categories/";

    private static final String NAME1 = "Продукты";
    private static final String EXT_ID1 = "pc1";
    private static final String UNKNOWN_REF = "UNKNOWN";
    private static final String NEW_NAME = "NEW_NAME";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void index() throws Exception {
        mockMvc.perform(get(PATH).with(anonymous())).andDo(print()).andExpect(status().isForbidden());
        mockMvc.perform(get(PATH).with(user("admin").roles("ADMIN"))).andDo(print()).andExpect(status().isOk());
        mockMvc.perform(get(PATH).with(user("user").roles("USER"))).andDo(print()).andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.categories").isArray(),
                jsonPath("$.categories", hasSize(3)),
                jsonPath("$.categories[0:].name", hasItem("Продукты")),
                jsonPath("$.categories[0:].name", hasItem("Хозтовары")),
                jsonPath("$.categories[0:].name", hasItem("Коммунальные услуги"))
        );
    }

    @Test
    void getById() throws Exception {
        mockMvc.perform(get(PATH + EXT_ID1).with(anonymous())).andDo(print()).andExpect(status().isForbidden());
        mockMvc.perform(get(PATH + EXT_ID1).with(user("admin").roles("ADMIN"))).andDo(print())
                .andExpect(status().isOk());
        mockMvc.perform(get(PATH + EXT_ID1).with(user("user").roles("USER"))).andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.externalId").value(EXT_ID1)
                );
    }

    @Test
    void getByName() throws Exception {
        mockMvc.perform(get(PATH + "/name/" + NAME1).with(anonymous())).andDo(print()).andExpect(status().isForbidden());
        mockMvc.perform(get(PATH + "/name/" + NAME1).with(user("admin").roles("ADMIN"))).andDo(print())
                .andExpect(status().isOk());
        mockMvc.perform(get(PATH + "/name/" + NAME1).with(user("user").roles("USER"))).andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.name").value(NAME1)
                );
    }

    @Test
    void create() throws Exception {
        ProductCategoryInDTO pcIn = new ProductCategoryInDTO();
        pcIn.setName(NEW_NAME);

        mockMvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON)
                .with(anonymous())).andDo(print()).andExpect(status().isForbidden());

        mockMvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(pcIn))
                        .with(user("user").roles("USER"))).andDo(print())
                .andExpectAll(
                        status().isCreated(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.name").value(NEW_NAME),
                        jsonPath("$.externalId").isNotEmpty()
                );
    }

    @Test
    void delete() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(PATH)
                .with(anonymous())).andDo(print()).andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.delete(PATH)
                .with(user("user").roles("USER"))).andDo(print()).andExpect(status().isBadRequest());
        mockMvc.perform(MockMvcRequestBuilders.delete(PATH + EXT_ID1)
                .with(user("admin").roles("ADMIN"))).andDo(print()).andExpect(status().isNoContent());

        mockMvc.perform(MockMvcRequestBuilders.delete(PATH + UNKNOWN_REF)
                .with(user("admin").roles("ADMIN"))).andDo(print()).andExpect(status().isNotFound());
    }

    @Test
    void update() throws Exception {
        ProductCategoryInDTO pcIn = new ProductCategoryInDTO();
        pcIn.setName(NEW_NAME);

        mockMvc.perform(put(PATH + EXT_ID1).contentType(MediaType.APPLICATION_JSON)
                .with(anonymous())).andDo(print()).andExpect(status().isForbidden());

        mockMvc.perform(put(PATH + UNKNOWN_REF).contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(pcIn))
                        .with(user("user").roles("USER"))).andDo(print())
                .andExpect(status().isNotFound());

        mockMvc.perform(put(PATH + EXT_ID1).contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(pcIn))
                        .with(user("user").roles("USER"))).andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.name").value(NEW_NAME),
                        jsonPath("$.externalId").isNotEmpty()
                );
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}