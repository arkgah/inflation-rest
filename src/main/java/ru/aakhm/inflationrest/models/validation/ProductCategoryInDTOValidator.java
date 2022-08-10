package ru.aakhm.inflationrest.models.validation;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.aakhm.inflationrest.dto.in.ProductCategoryInDTO;
import ru.aakhm.inflationrest.dto.out.ProductCategoryOutDTO;
import ru.aakhm.inflationrest.services.ProductCategoriesService;
import ru.aakhm.inflationrest.utils.Utils;

import java.util.Optional;

@Component
public class ProductCategoryInDTOValidator implements Validator {
    private final ProductCategoriesService productCategoriesService;
    private final Utils utils;

    public ProductCategoryInDTOValidator(ProductCategoriesService productCategoriesService, MessageSource messageSource, Utils utils) {
        this.productCategoriesService = productCategoriesService;
        this.utils = utils;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(ProductCategoryInDTO.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ProductCategoryInDTO productCategoryInDTO = (ProductCategoryInDTO) target;
        Optional<ProductCategoryOutDTO> category = productCategoriesService.getByName(productCategoryInDTO.getName());
        if (category.isPresent()) {
            errors.rejectValue("name", utils.getMessageFromBundle("productcategory.name.uniq.err"));
        }
    }
}
