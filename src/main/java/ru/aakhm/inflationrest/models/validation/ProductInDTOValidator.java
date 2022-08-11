package ru.aakhm.inflationrest.models.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.aakhm.inflationrest.dto.in.ProductInDTO;
import ru.aakhm.inflationrest.dto.out.ProductOutDTO;
import ru.aakhm.inflationrest.services.ProductsService;
import ru.aakhm.inflationrest.utils.Utils;

import java.util.Optional;

@Component
public class ProductInDTOValidator implements Validator {
    private final ProductsService productsService;
    private final Utils utils;

    @Autowired
    public ProductInDTOValidator(ProductsService productsService, Utils utils) {
        this.productsService = productsService;
        this.utils = utils;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(ProductInDTO.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ProductInDTO productInDTO = (ProductInDTO) target;
        if (productInDTO.getCategory() == null) {
            errors.rejectValue("category", utils.getMessageFromBundle("product.category.null.err"));
            return;
        }
        Optional<ProductOutDTO> product = productsService.getByNameAndCategoryName(productInDTO.getName(), productInDTO.getCategory().getName());
        if (product.isPresent()) {
            errors.rejectValue("name", utils.getMessageFromBundle("product.name.category.uniq.err"));
        }
    }
}
