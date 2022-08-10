package ru.aakhm.inflationrest.dto.in;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class ProductCategoryInDTO {
    @NotEmpty(message = "{productcategory.name.empty.err}")
    @Size(min = 2, max = 100, message = "{productcategory.name.size.err}")
    private String name;
}
