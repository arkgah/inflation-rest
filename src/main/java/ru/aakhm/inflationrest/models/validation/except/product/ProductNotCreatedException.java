package ru.aakhm.inflationrest.models.validation.except.product;

public class ProductNotCreatedException extends RuntimeException {
    public ProductNotCreatedException(String message) {
        super(message);
    }
}
