package ru.aakhm.inflationrest.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class PersonOutDTO {
    @NotEmpty(message = "{person.login.empty.err}")
    @Size(min = 3, max = 100, message = "{person.login.size.err}")
    private String login;

    @NotEmpty(message = "{person.firstname.empty.err}")
    @Size(min = 1, max = 50, message = "{person.firstname.size.err}")
    private String firstName;

    @NotEmpty(message = "{person.lastname.empty.err}")
    @Size(min = 1, max = 50, message = "{person.lastname.size.err}")
    private String lastName;

    @NotEmpty
    private String externalId;
}
