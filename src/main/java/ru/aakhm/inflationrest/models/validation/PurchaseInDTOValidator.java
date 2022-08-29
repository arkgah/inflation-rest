package ru.aakhm.inflationrest.models.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.aakhm.inflationrest.dto.in.PurchaseInDTO;
import ru.aakhm.inflationrest.services.PurchasesService;
import ru.aakhm.inflationrest.utils.Utils;

@Component
public class PurchaseInDTOValidator implements Validator {
    private final Utils utils;
    private final PurchasesService purchasesService;

    @Autowired
    public PurchaseInDTOValidator(Utils utils, PurchasesService purchasesService) {
        this.utils = utils;
        this.purchasesService = purchasesService;
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
        if (purchaseInDTO.getProduct().getCategory().getName() == null) {
            errors.rejectValue("product.category.name", utils.getMessageFromBundle("productcategory.name.null.err"));
            return;
        }
        if (purchaseInDTO.getStore().getName() == null) {
            errors.rejectValue("store.name", utils.getMessageFromBundle("store.name.null.err"));
            return;
        }
        if (purchasesService.isPresentByPurchasedAtAndProductAndStoreAndPerson(
                purchaseInDTO.getPurchasedAt(),
                purchaseInDTO.getProduct().getName(),
                purchaseInDTO.getProduct().getCategory().getName(),
                purchaseInDTO.getStore().getName()
        )) {
            errors.rejectValue("purchasedAt",
                    utils.getMessageFromBundle("purchase.uniq.err"));
            return;
        }

    }
}
