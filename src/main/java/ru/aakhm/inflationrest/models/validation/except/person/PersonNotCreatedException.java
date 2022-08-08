package ru.aakhm.inflationrest.models.validation.except.person;

public class PersonNotCreatedException extends RuntimeException {
    public PersonNotCreatedException(String message) {
        super(message);
    }
}
