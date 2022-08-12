package ru.aakhm.inflationrest.models.validation.except.purchase;

public class PurchaseNotUpdatedException extends RuntimeException {
    public PurchaseNotUpdatedException(String message) {
        super(message);
    }
}
