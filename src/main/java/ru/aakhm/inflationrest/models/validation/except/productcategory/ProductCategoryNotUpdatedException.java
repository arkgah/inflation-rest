package ru.aakhm.inflationrest.models.validation.except.productcategory;

public class ProductCategoryNotUpdatedException extends RuntimeException {
    public ProductCategoryNotUpdatedException(String message) {
        super(message);
    }
}
