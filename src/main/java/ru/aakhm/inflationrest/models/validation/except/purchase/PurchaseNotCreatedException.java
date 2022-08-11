package ru.aakhm.inflationrest.models.validation.except.purchase;

public class PurchaseNotCreatedException extends RuntimeException {
    public PurchaseNotCreatedException(String message) {
        super(message);
    }
}
