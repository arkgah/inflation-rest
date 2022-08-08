package ru.aakhm.inflationrest.models.validation.except.person;

public class PersonLoginException extends RuntimeException {
    public PersonLoginException(String message) {
        super(message);
    }
}
