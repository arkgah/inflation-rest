package ru.aakhm.inflationrest.models.validation.except.person;

public class PersonRoleNotFoundException extends RuntimeException {
    public PersonRoleNotFoundException(String message) {
        super(message);
    }
}
