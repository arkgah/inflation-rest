package ru.aakhm.inflationrest.models.validation;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.aakhm.inflationrest.dto.StoreInDTO;
import ru.aakhm.inflationrest.dto.StoreOutDTO;
import ru.aakhm.inflationrest.services.StoresService;
import ru.aakhm.inflationrest.utils.Utils;

import java.util.Optional;

@Component
public class StoreInDTOValidator implements Validator {
    private final StoresService storesService;
    private final Utils utils;

    public StoreInDTOValidator(StoresService storesService, MessageSource messageSource, Utils utils) {
        this.storesService = storesService;
        this.utils = utils;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(StoreInDTO.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        StoreInDTO storeInDTO = (StoreInDTO) target;
        Optional<StoreOutDTO> store = storesService.getByName(storeInDTO.getName());
        if (store.isPresent()) {
            errors.rejectValue("name", utils.getMessageFromBundle("store.name.uniq.err"));
        }
    }
}
