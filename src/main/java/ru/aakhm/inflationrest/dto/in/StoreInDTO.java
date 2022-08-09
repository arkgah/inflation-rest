package ru.aakhm.inflationrest.dto.in;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class StoreInDTO {
    @NotEmpty(message = "{store.name.empty.err}")
    @Size(min = 2, max = 100, message = "{store.name.size.err}")
    private String name;
}
