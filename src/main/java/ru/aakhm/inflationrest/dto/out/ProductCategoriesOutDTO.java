package ru.aakhm.inflationrest.dto.out;

import lombok.Data;

import java.util.List;

@Data
public class ProductCategoriesOutDTO {
    private List<ProductCategoryOutDTO> categories;
}