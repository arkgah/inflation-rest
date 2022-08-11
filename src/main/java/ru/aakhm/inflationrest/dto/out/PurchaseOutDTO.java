package ru.aakhm.inflationrest.dto.out;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PurchaseOutDTO {
    private BigDecimal price;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date purchasedAt;

    private String memo;

    private ProductOutDTO product;

    private StoreOutDTO store;

    @JsonIgnoreProperties({"firstName", "lastName", "externalId"})
    private PersonOutDTO person;

    private String externalId;
}
