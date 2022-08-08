package ru.aakhm.inflationrest.models.validation.except.person;

public class PersonNotFoundException extends RuntimeException {
    public PersonNotFoundException(String message) {
        super(message);
    }
}
