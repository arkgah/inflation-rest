package ru.aakhm.inflationrest.models.validation;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.aakhm.inflationrest.dto.StoreInDTO;
import ru.aakhm.inflationrest.models.Store;
import ru.aakhm.inflationrest.services.StoresService;

import java.util.Optional;

@Component
public class StoreInDTOValidator implements Validator {
    private final StoresService storesService;
    private final MessageSource messageSource;

    public StoreInDTOValidator(StoresService storesService, MessageSource messageSource) {
        this.storesService = storesService;
        this.messageSource = messageSource;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(StoreInDTO.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        StoreInDTO storeInDTO = (StoreInDTO) target;
        Optional<Store> storeDB = storesService.getByName(storeInDTO.getName());
        if (storeDB.isPresent()) {
            errors.rejectValue("name", messageSource.getMessage("store.name.uniq.err", null, LocaleContextHolder.getLocale()));
        }
    }
}
