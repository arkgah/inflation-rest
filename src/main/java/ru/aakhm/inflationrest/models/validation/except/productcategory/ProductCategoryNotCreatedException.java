package ru.aakhm.inflationrest.models.validation.except.productcategory;

public class ProductCategoryNotCreatedException extends RuntimeException {
    public ProductCategoryNotCreatedException(String message) {
        super(message);
    }
}
