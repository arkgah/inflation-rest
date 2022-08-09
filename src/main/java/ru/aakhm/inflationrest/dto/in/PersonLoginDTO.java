package ru.aakhm.inflationrest.dto.in;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class PersonLoginDTO {
    @NotEmpty(message = "{person.login.empty.err}")
    @Size(min = 3, max = 100, message = "{person.login.size.err}")
    private String login;

    @NotEmpty(message = "{person.password.empty.err}")
    @Size(min = 8, max = 30, message = "person.password.size.err")
    private String password;
}
