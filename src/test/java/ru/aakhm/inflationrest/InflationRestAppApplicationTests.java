package ru.aakhm.inflationrest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import ru.aakhm.inflationrest.controllers.StoresController;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class InflationRestAppApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StoresController storesController;

    @Test
    void contextLoads() {
        assertThat(storesController).isNotNull();
    }

}
