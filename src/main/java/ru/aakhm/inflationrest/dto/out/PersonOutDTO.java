package ru.aakhm.inflationrest.dto.out;

import lombok.Data;

@Data
public class PersonOutDTO {
    private String login;

    private String firstName;

    private String lastName;

    private String externalId;
}
