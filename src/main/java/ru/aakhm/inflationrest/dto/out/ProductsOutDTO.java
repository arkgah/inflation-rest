package ru.aakhm.inflationrest.dto.out;

import lombok.Data;

import java.util.List;

@Data
public class ProductsOutDTO {
   private List<ProductOutDTO> products;
}
