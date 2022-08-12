package ru.aakhm.inflationrest.models.validation.except.purchase;

public class PurchaseDeleteNotAllowedException extends RuntimeException {
    public PurchaseDeleteNotAllowedException(String message) {
        super(message);
    }
}
