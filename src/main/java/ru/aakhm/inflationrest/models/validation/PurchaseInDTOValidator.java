package ru.aakhm.inflationrest.models.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.aakhm.inflationrest.dto.in.PurchaseInDTO;
import ru.aakhm.inflationrest.utils.Utils;

@Component
public class PurchaseInDTOValidator implements Validator {
    private final Utils utils;

    @Autowired
    public PurchaseInDTOValidator(Utils utils) {
        this.utils = utils;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(PurchaseInDTO.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PurchaseInDTO purchaseInDTO = (PurchaseInDTO) target;
        if (purchaseInDTO.getProduct().getName() == null) {
            errors.rejectValue("product", utils.getMessageFromBundle("product.name.null.err"));
            return;
        }
        if (purchaseInDTO.getProduct().getCategory() == null) {
            errors.rejectValue("product.category", utils.getMessageFromBundle("product.category.null.err"));
            return;
        }
        if (purchaseInDTO.getProduct().getCategory().getName() == null)
            errors.rejectValue("product.category.name", utils.getMessageFromBundle("productcategory.name.null"));

    }
}
