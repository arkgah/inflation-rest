package ru.aakhm.inflationrest.models.validation.except.store;

public class StoreNotCreatedException extends RuntimeException {
    public StoreNotCreatedException(String errorMsg) {
        super(errorMsg);
    }
}
