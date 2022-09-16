package ru.aakhm.inflationrest.integration_tests.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.aakhm.inflationrest.dto.in.PurchaseInDTO;
import ru.aakhm.inflationrest.dto.in.StoreInDTO;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    private static final String PATH = "/purchases/";
    private static final String PROD_NAME1 = "Хлеб ржаной";
    private static final String EXT_ID = "prc1";
    private static final String UNKNOWN_REF = "UNKNOWN";

    private static final String PURCH_AT_MON_MAX = "2022-11";
    private static final String PURCH_AT_MON_MID = "2022-10";
    private static final String PURCH_AT_MON_MIN = "2022-09";

    private static final String NEW_MEMO = "memo";
    private static final BigDecimal NEW_PRICE = BigDecimal.valueOf(123.45);
    private static final String NEW_PURCH_AT_DUP_STRING = "2022-09-01 09:00"; // для проверки на уникальность
    private static final Date NEW_PURCH_AT_DUP;
    private static final String NEW_PURCH_AT_STRING = "2022-09-15 09:00"; // для проверки на уникальность
    private static final Date NEW_PURCH_AT;

    static {
        try {
            NEW_PURCH_AT = new SimpleDateFormat("yyyy-M-dd hh:mm").parse(NEW_PURCH_AT_STRING);
            NEW_PURCH_AT_DUP = new SimpleDateFormat("yyyy-M-dd hh:mm").parse(NEW_PURCH_AT_DUP_STRING);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String NEW_PROD_NAME = "Хлеб ржаной";
    private static final Double NEW_PROD_UNIT = 1.23;

    private static final String NEW_PROD_CAT_NAME = "Продукты";
    private static final String NEW_PROD_STORE_NAME = "Пятёрочка";

    private PurchaseInDTO purchInDTO;

    @BeforeEach
    void setUp() {
        purchInDTO = new PurchaseInDTO() {
            {
                setPurchasedAt(NEW_PURCH_AT);
                setPrice(NEW_PRICE);
                setMemo(NEW_MEMO);
                setProduct(
                        new ProductInDTO() {
                            {
                                setName(NEW_PROD_NAME);
                                setUnit(NEW_PROD_UNIT);
                                setCategory(
                                        new ProductCategoryInDTO() {
                                            {
                                                setName(NEW_PROD_CAT_NAME);
                                            }
                                        }
                                );
                            }
                        }
                );
                setStore(
                        new StoreInDTO() {
                            {
                                setName(NEW_PROD_STORE_NAME);
                            }
                        }
                );
            }
        };
    }

    @Test
    void create() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(PATH).with(anonymous())).andDo(print())
                .andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.post(PATH).with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(purchInDTO))).andDo(print())
                .andExpectAll(
                        status().isCreated(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.externalId").isNotEmpty(),
                        jsonPath("$.purchasedAt").value(startsWith(NEW_PURCH_AT_STRING.substring(0, 9))),
                        jsonPath("$.memo").value(NEW_MEMO),
                        jsonPath("$.price").value(NEW_PRICE),
                        jsonPath("$.product.name").value(NEW_PROD_NAME),
                        jsonPath("$.product.unit").value(NEW_PROD_UNIT),
                        jsonPath("$.product.category.name").value(NEW_PROD_CAT_NAME),
                        jsonPath("$.store.name").value(NEW_PROD_STORE_NAME),
                        jsonPath("$.person.login").value("user")
                );

        purchInDTO.getProduct().setName(UNKNOWN_REF);
        mockMvc.perform(MockMvcRequestBuilders.post(PATH).with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(purchInDTO))).andDo(print())
                .andExpect(status().isBadRequest());

        purchInDTO.getProduct().setName(NEW_PROD_NAME);
        purchInDTO.setPurchasedAt(NEW_PURCH_AT_DUP);
        mockMvc.perform(MockMvcRequestBuilders.post(PATH).with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(purchInDTO))).andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_Ok() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put(PATH + EXT_ID).with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(purchInDTO))).andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.externalId").value(EXT_ID),
                        jsonPath("$.purchasedAt").value(startsWith(NEW_PURCH_AT_STRING.substring(0, 9))),
                        jsonPath("$.memo").value(NEW_MEMO),
                        jsonPath("$.price").value(NEW_PRICE),
                        jsonPath("$.product.name").value(NEW_PROD_NAME),
                        jsonPath("$.product.unit").value(NEW_PROD_UNIT),
                        jsonPath("$.product.category.name").value(NEW_PROD_CAT_NAME),
                        jsonPath("$.store.name").value(NEW_PROD_STORE_NAME)
                );
    }

    @Test
    void update_Error() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put(PATH + EXT_ID).with(anonymous())).andDo(print())
                .andExpect(status().isForbidden());

        mockMvc.perform(MockMvcRequestBuilders.put(PATH + UNKNOWN_REF).with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(purchInDTO))).andDo(print())
                .andExpect(status().isNotFound());

        purchInDTO.setPurchasedAt(NEW_PURCH_AT_DUP);
        mockMvc.perform(MockMvcRequestBuilders.put(PATH + UNKNOWN_REF).with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(purchInDTO))).andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(PATH + EXT_ID).with(anonymous())).andDo(print())
                .andExpect(status().isForbidden());

        mockMvc.perform(MockMvcRequestBuilders.delete(PATH + UNKNOWN_REF).with(
                        user("user").roles("USER"))).andDo(print())
                .andExpect(status().isNotFound());

        // It's not allowed to delete not own purchases
        mockMvc.perform(MockMvcRequestBuilders.delete(PATH + EXT_ID).with(
                        user("other_user").roles("USER"))).andDo(print())
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.delete(PATH + EXT_ID).with(
                        user("user").roles("USER"))).andDo(print())
                .andExpect(status().isNoContent());
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
                jsonPath("$.purchases[0:].product.name", hasItem(PROD_NAME1))
        );

        int page = 0;
        int per_page = 2;
        boolean sort_asc = false;
        final String PATH_FORMAT = PATH + "?page=%d&per-page=%d&sort-asc=%b";
        mockMvc.perform(get(String.format(PATH_FORMAT, page, per_page, sort_asc))
                .with(user("user").roles("USER"))).andDo(print()).andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.purchases").isNotEmpty(),
                jsonPath("$.purchases").isArray(),
                jsonPath("$.purchases", hasSize(2)),
                jsonPath("$.purchases[0:].purchasedAt",
                        containsInRelativeOrder(startsWith(PURCH_AT_MON_MAX), startsWith(PURCH_AT_MON_MID)))
        );

        sort_asc = true;
        mockMvc.perform(get(String.format(PATH_FORMAT, page, per_page, sort_asc))
                .with(user("user").roles("USER"))).andDo(print()).andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.purchases").isNotEmpty(),
                jsonPath("$.purchases").isArray(),
                jsonPath("$.purchases", hasSize(2)),
                jsonPath("$.purchases[0:].purchasedAt",
                        containsInRelativeOrder(startsWith(PURCH_AT_MON_MIN), startsWith(PURCH_AT_MON_MID)))
        );

    }

    @Test
    void getByExternalId() throws Exception {
        mockMvc.perform(get(PATH + EXT_ID).with(anonymous())).andDo(print()).andExpect(status().isForbidden());
        mockMvc.perform(get(PATH + EXT_ID).with(user("admin").roles("ADMIN")))
                .andDo(print()).andExpect(status().isOk());
        mockMvc.perform(get(PATH + EXT_ID).with(user("user").roles("USER")))
                .andDo(print()).andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.externalId").isNotEmpty(),
                        jsonPath("$.externalId").value(EXT_ID)
                );

        mockMvc.perform(get(PATH + UNKNOWN_REF).with(user("user").roles("USER")))
                .andDo(print()).andExpect(status().isNotFound());

    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}