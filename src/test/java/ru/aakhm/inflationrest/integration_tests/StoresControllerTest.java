package ru.aakhm.inflationrest.integration_tests;

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
import ru.aakhm.inflationrest.dto.in.StoreInDTO;

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
    @Sql(value = {"/StoresControllerTest_before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getById() throws Exception {
        mockMvc.perform(get("/stores/s1").with(anonymous())).andDo(print()).andExpect(status().isForbidden());

        mockMvc.perform(get("/stores/s2").with(user("admin").roles("ADMIN"))).andDo(print()).andExpect(status().isOk());

        mockMvc.perform(get("/stores/s3").with(user("user").roles("USER"))).andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.name").value("Лента"),
                        jsonPath("$.externalId").value("s3")
                );

    }

    @Test
    @Sql(value = {"/StoresControllerTest_before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getByName() throws Exception {
        mockMvc.perform(get("/stores/s1").with(anonymous())).andDo(print()).andExpect(status().isForbidden());

        mockMvc.perform(get("/stores/s2").with(user("admin").roles("ADMIN"))).andDo(print()).andExpect(status().isOk());

        mockMvc.perform(get("/stores/name/Лента").with(user("user").roles("USER"))).andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.name").value("Лента"),
                        jsonPath("$.externalId").value("s3")
                );
    }

    @Test
    @Sql(value = {"/StoresControllerTest_before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void create() throws Exception {
        StoreInDTO storeInDTO = new StoreInDTO();
        storeInDTO.setName("Test");

        mockMvc.perform(post("/stores").contentType(MediaType.APPLICATION_JSON)
                .with(anonymous())).andDo(print()).andExpect(status().isForbidden());


        mockMvc.perform(post("/stores").contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(storeInDTO))
                        .with(user("user").roles("USER"))).andDo(print())
                .andExpectAll(
                        status().isCreated(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.name").value("Test"),
                        jsonPath("$.externalId").isNotEmpty()
                );
    }

    @Test
    @Sql(value = {"/StoresControllerTest_before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void delete() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/stores/s1")
                .with(anonymous())).andDo(print()).andExpect(status().isForbidden());

        mockMvc.perform(MockMvcRequestBuilders.delete("/stores/s1")
                .with(user("user").roles("USER"))).andDo(print()).andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.delete("/stores/s1")
                .with(user("admin").roles("ADMIN"))).andDo(print()).andExpect(status().isNoContent());
    }

    @Test
    @Sql(value = {"/StoresControllerTest_before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void update() throws Exception {
        StoreInDTO storeInDTO = new StoreInDTO();
        storeInDTO.setName("Test");

        mockMvc.perform(put("/stores/s1").contentType(MediaType.APPLICATION_JSON)
                .with(anonymous())).andDo(print()).andExpect(status().isForbidden());

        mockMvc.perform(put("/stores/s1").contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(storeInDTO))
                        .with(user("user").roles("USER"))).andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.name").value("Test"),
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