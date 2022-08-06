package ru.aakhm.inflationrest.dto;

import lombok.Data;

@Data
public class ErrorDTO {
    private long timestamp = System.currentTimeMillis();
    private String message;
}
