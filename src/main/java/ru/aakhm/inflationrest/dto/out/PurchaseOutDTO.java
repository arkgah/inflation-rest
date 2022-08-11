package ru.aakhm.inflationrest.dto.out;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class PurchaseOutDTO {
    private double price;

    private double unit;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date purchasedAt;

    private String memo;

    private ProductOutDTO product;

    private StoreOutDTO store;

    private PersonOutDTO person;

    private String externalId;
}
