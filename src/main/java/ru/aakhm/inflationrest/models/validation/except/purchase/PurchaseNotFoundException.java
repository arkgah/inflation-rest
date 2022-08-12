package ru.aakhm.inflationrest.models.validation.except.purchase;

public class PurchaseNotFoundException extends RuntimeException {
    public PurchaseNotFoundException(String message) {
        super(message);
    }
}
