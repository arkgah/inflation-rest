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
import ru.aakhm.inflationrest.dto.in.ProductInDTO;

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
@Sql(value = {"/sql/ProductsTest_before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ProductsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private static final String PATH = "/products/";

    private static final String EXT_ID = "p1";
    private static final String NAME = "Хлеб ржаной";
    private static final String CAT_NAME = "Продукты";
    private static final String UNKNOWN_REF = "UNKNOWN";

    private static final String NEW_NAME = "NEW NAME";


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
                jsonPath("$.products").isArray(),
                jsonPath("$.products", hasSize(4)),
                jsonPath("$.products[0:].name", hasItem("Хлеб ржаной")),
                jsonPath("$.products[0:].name", hasItem("Кефир")),
                jsonPath("$.products[0:].name", hasItem("Мыло"))
        );

        int page = 0;
        int per_page = 2;
        String formatPath = PATH + "?page=%d&per-page=%d";
        mockMvc.perform(get(String.format(formatPath, page, per_page)).with(user("user").roles("USER"))).andDo(print()).andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.products").isArray(),
                jsonPath("$.products", hasSize(2))
        );

        page = 1;
        per_page = 10;
        mockMvc.perform(get(String.format(formatPath, page, per_page)).with(user("user").roles("USER"))).andDo(print()).andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.products").isArray(),
                jsonPath("$.products", hasSize(0))
        );

        String name_like = "хлеб";
        String cat_name_like = "прод";
        formatPath = PATH + "?name-like=%s&cat-name-like=%s";
        mockMvc.perform(get(String.format(formatPath, name_like, cat_name_like)).with(user("user").roles("USER"))).andDo(print()).andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.products").isArray(),
                jsonPath("$.products", hasSize(2)),
                jsonPath("$.products[0:].name", hasItem("Хлеб ржаной")),
                jsonPath("$.products[0:].name", hasItem("Хлеб белый"))
        );
    }

    @Test
    void getById() throws Exception {
        String PATH_ID = PATH + EXT_ID;
        mockMvc.perform(get(PATH_ID).with(anonymous())).andDo(print()).andExpect(status().isForbidden());
        mockMvc.perform(get(PATH_ID).with(user("admin").roles("ADMIN"))).andDo(print()).andExpect(status().isOk());
        mockMvc.perform(get(PATH_ID).with(user("user").roles("USER"))).andDo(print()).andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.externalId").exists(),
                jsonPath("$.externalId").value(EXT_ID)
        );

        PATH_ID = PATH + UNKNOWN_REF;
        mockMvc.perform(get(PATH_ID).with(user("user").roles("USER"))).andDo(print()).andExpect(status().isNotFound());
    }

    @Test
    void getByNameAndCategoryName() throws Exception {
        final String FORMAT_PATH_NAME_CAT = PATH + "/name/%s/category/%s";
        mockMvc.perform(get(String.format(FORMAT_PATH_NAME_CAT, NAME, CAT_NAME)).with(anonymous())).andDo(print())
                .andExpect(status().isForbidden());
        mockMvc.perform(get(String.format(FORMAT_PATH_NAME_CAT, NAME, CAT_NAME))
                .with(user("admin").roles("ADMIN"))).andDo(print()).andExpect(status().isOk());
        // exists
        mockMvc.perform(get(String.format(FORMAT_PATH_NAME_CAT, NAME, CAT_NAME))
                .with(user("user").roles("USER"))).andDo(print()).andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.name").exists(),
                jsonPath("$.name").value(NAME),
                jsonPath("$.category.name").exists(),
                jsonPath("$.category.name").value(CAT_NAME)
        );

        // doesn't exist
        mockMvc.perform(get(String.format(FORMAT_PATH_NAME_CAT, UNKNOWN_REF, CAT_NAME))
                .with(user("user").roles("USER"))).andDo(print()).andExpect(status().isNotFound());

        mockMvc.perform(get(String.format(FORMAT_PATH_NAME_CAT, NAME, UNKNOWN_REF))
                .with(user("user").roles("USER"))).andDo(print()).andExpect(status().isNotFound());
    }

    @Test
    void delete() throws Exception {
        String PATH_ID = PATH + EXT_ID;
        mockMvc.perform(MockMvcRequestBuilders.delete(PATH_ID).with(anonymous())).andDo(print())
                .andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.delete(PATH_ID).with(user("user").roles("USER"))).andDo(print())
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.delete(PATH_ID).with(user("admin").roles("ADMIN"))).andDo(print())
                .andExpect(status().isNoContent());

        PATH_ID = PATH + UNKNOWN_REF;
        mockMvc.perform(MockMvcRequestBuilders.delete(PATH_ID).with(user("admin").roles("ADMIN"))).andDo(print())
                .andExpect(status().isNotFound());

    }

    @Test
    void create() throws Exception {
        ProductInDTO productIn = new ProductInDTO() {
            {
                setName(NEW_NAME);
                setCategory(
                        new ProductCategoryInDTO() {
                            {
                                setName(CAT_NAME);
                            }
                        }
                );
                setUnit(1.);
            }
        };
        mockMvc.perform(MockMvcRequestBuilders.post(PATH).with(anonymous())).andDo(print())
                .andExpect(status().isForbidden());

        mockMvc.perform(MockMvcRequestBuilders.post(PATH).with(user("user").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON).content(asJsonString(productIn))).andDo(print()).andExpectAll(
                status().isCreated(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.name").value(NEW_NAME),
                jsonPath("$.category.name").value(CAT_NAME),
                jsonPath("$.externalId").isNotEmpty()
        );

        // existing product
        productIn.setName(NAME);
        mockMvc.perform(MockMvcRequestBuilders.post(PATH).with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON).content(asJsonString(productIn))).andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void update() throws Exception {
        String PATH_ID = PATH + EXT_ID;
        ProductInDTO productIn = new ProductInDTO() {
            {
                setName(NEW_NAME);
                setCategory(
                        new ProductCategoryInDTO() {
                            {
                                setName(CAT_NAME);
                            }
                        }
                );
                setUnit(1.);
            }
        };
        mockMvc.perform(MockMvcRequestBuilders.post(PATH_ID).with(anonymous())).andDo(print())
                .andExpect(status().isForbidden());

        mockMvc.perform(MockMvcRequestBuilders.put(PATH_ID).with(user("user").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON).content(asJsonString(productIn))).andDo(print()).andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.name").value(NEW_NAME),
                jsonPath("$.category.name").value(CAT_NAME),
                jsonPath("$.externalId").value(EXT_ID)
        );

        PATH_ID = PATH + UNKNOWN_REF;
        mockMvc.perform(MockMvcRequestBuilders.put(PATH_ID).with(user("user").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON).content(asJsonString(productIn))).andDo(print()).andExpect(
                status().isBadRequest()
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