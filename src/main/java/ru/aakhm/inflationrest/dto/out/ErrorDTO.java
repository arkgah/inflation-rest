package ru.aakhm.inflationrest.dto.out;

import lombok.Data;

@Data
public class ErrorDTO {
    private long timestamp = System.currentTimeMillis();
    private String message;
}
