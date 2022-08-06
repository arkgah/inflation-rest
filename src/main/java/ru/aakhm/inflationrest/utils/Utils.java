package ru.aakhm.inflationrest.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Random;

@Component
public class Utils {
    private final Random RANDOM = new SecureRandom();
    private final String ALPHABET = "0123456789ABCDEFGHUIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    @Value("${store.external_id.length}")
    private int STORE_EXTERNAL_ID_LENGTH;

    public String generateStoreExternalId() {
        return generateRandomString(STORE_EXTERNAL_ID_LENGTH);
    }

    public String generateRandomString(int length) {
        StringBuilder returnValue = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }

        return returnValue.toString();
    }
}
