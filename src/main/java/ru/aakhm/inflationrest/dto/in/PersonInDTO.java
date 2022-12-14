package ru.aakhm.inflationrest.dto.in;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class PersonInDTO {
    @NotEmpty(message = "{person.login.empty.err}")
    @Size(min = 3, max = 100, message = "{person.login.size.err}")
    private String login;

    @NotEmpty(message = "{person.password.empty.err}")
    @Size(min = 8, max = 30, message = "{person.password.size.err}")
    private String password;

    @NotEmpty(message = "{person.firstname.empty.err}")
    @Size(min = 1, max = 50, message = "{person.firstname.size.err}")
    private String firstName;

    @NotEmpty(message = "{person.lastname.empty.err}")
    @Size(min = 1, max = 50, message = "{person.lastname.size.err}")
    private String lastName;
}
