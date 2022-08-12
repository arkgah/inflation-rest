package ru.aakhm.inflationrest.models.validation.except.purchase;

public class PurchaseUpdateNotAllowedException extends RuntimeException {
    public PurchaseUpdateNotAllowedException(String message) {
        super(message);
    }
}
