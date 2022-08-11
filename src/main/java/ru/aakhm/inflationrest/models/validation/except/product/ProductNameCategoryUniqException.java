package ru.aakhm.inflationrest.models.validation.except.product;

public class ProductNameCategoryUniqException extends RuntimeException {
    public ProductNameCategoryUniqException(String message) {
        super(message);
    }
}
