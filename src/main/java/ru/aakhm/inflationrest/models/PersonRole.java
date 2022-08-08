package ru.aakhm.inflationrest.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Entity
@Table(name = "person_role")
@Getter
@Setter
@NoArgsConstructor
public class PersonRole {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotEmpty(message = "personrole.name.empty.err")
    private String name;

    @OneToMany(mappedBy = "role")
    private List<Person> people;
}
