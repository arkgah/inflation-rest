package ru.aakhm.inflationrest.models.validation.except.store;

public class StoreNotFoundException extends RuntimeException {
    public StoreNotFoundException(String errorMsg) {
        super(errorMsg);
    }
}
