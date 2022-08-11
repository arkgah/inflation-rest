package ru.aakhm.inflationrest.dto.in;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
public class PurchaseInDTO {
    @Positive(message = "{purchase.price.positive}")
    @NotNull
    private Double price;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date purchasedAt;

    @Size(max = 200, message = "{purchase.memo.size}")
    private String memo;

    @NotNull
    private String productName;

    @NotNull
    private String storeName;
}
