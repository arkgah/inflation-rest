package ru.aakhm.inflationrest.models.validation.except.productcategory;

public class ProductCategoryNotFoundException extends RuntimeException {
    public ProductCategoryNotFoundException(String message) {
        super(message);
    }
}
