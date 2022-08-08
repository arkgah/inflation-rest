package ru.aakhm.inflationrest.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Person {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "login")
    @NotEmpty(message = "{person.login.empty.err}")
    @Size(min = 3, max = 100, message = "{person.login.size.err}")
    private String login;

    @Column(name = "password")
    private String password;

    @Column(name = "first_name")
    @NotEmpty(message = "{person.firstname.empty.err}")
    @Size(min = 1, max = 50, message = "{person.firstname.size.err}")
    private String firstName;

    @Column(name = "last_name")
    @NotEmpty(message = "{person.lastname.empty.err}")
    @Size(min = 1, max = 50, message = "{person.lastname.size.err}")
    private String lastName;

    @Column(name = "external_id")
    @NotNull
    private String externalId;
}
