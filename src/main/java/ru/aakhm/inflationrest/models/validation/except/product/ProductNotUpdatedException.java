package ru.aakhm.inflationrest.models.validation.except.product;

public class ProductNotUpdatedException extends RuntimeException {
    public ProductNotUpdatedException(String message) {
        super(message);
    }
}
