package ru.aakhm.inflationrest.models.validation.except.store;

public class StoreNotUpdatedException extends RuntimeException {
    public StoreNotUpdatedException(String errorMsg) {
        super(errorMsg);
    }
}
