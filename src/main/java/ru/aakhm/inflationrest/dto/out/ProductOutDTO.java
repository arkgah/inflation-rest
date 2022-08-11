package ru.aakhm.inflationrest.dto.out;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Data
public class ProductOutDTO {
    @NotEmpty(message = "{product.name.empty.err}")
    @Size(min = 2, max = 100, message = "{product.name.size.err}")
    private String name;

    @Positive(message = "{product.unit.value.err}")
    @NotNull(message = "{product.unit.null.err}")
    private Double unit;

    @NotEmpty
    private String externalId;

    @NotNull(message = "{product.category.null.err}")
    private ProductCategoryOutDTO category;
}
