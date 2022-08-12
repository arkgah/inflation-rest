package ru.aakhm.inflationrest.models.validation.except.analytics;

public class CPICannotCalculateException extends RuntimeException {
    public CPICannotCalculateException(String message) {
        super(message);
    }
}
