package ru.aakhm.inflationrest.models.validation.except.analytics;

public class CPIIncorrectDateInterval extends RuntimeException {
    public CPIIncorrectDateInterval(String message) {
        super(message);
    }
}
