package ru.aakhm.inflationrest.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Component
public class Utils {
    private final Random RANDOM = new SecureRandom();

    private final MessageSource messageSource;

    @Value("${store.external_id.length}")
    private int STORE_EXTERNAL_ID_LENGTH;

    public Utils(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String generateStoreExternalId() {
        return generateRandomString(STORE_EXTERNAL_ID_LENGTH);
    }

    public String generateRandomString(int length) {
        final String ALPHABET = "0123456789ABCDEFGHUIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder returnValue = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }

        return returnValue.toString();
    }

    // For validation
    public String getErrorMsg(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            return errors.stream()
                    .map(e -> new StringBuilder().append(e.getField()).append(" - ").append(e.getDefaultMessage() != null ? e.getDefaultMessage() : e.getCode()))
                    .collect(Collectors.joining(";"));
        }
        return "";
    }

    public String getMessageFromBundle(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
