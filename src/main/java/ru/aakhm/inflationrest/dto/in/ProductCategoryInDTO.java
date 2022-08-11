package ru.aakhm.inflationrest.dto.in;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class ProductCategoryInDTO {
    @NotNull(message = "{productcategory.name.null}")
    @NotEmpty(message = "{productcategory.name.empty.err}")
    @Size(min = 2, max = 100, message = "{productcategory.name.size.err}")
    private String name;
}
