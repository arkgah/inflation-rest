package ru.aakhm.inflationrest.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name = "store")
@Getter
@Setter
public class Store {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name")
    @NotEmpty(message = "{store.name.empty.err}")
    @Size(min = 2, max = 100, message = "{store.name.size.err}")
    private String name;

    @Column(name = "external_id")
    @NotNull
    private String externalId;

    @OneToMany(mappedBy = "store")
    private List<Purchase> purchases;
}
