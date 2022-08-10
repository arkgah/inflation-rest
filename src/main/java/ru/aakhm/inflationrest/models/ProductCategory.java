package ru.aakhm.inflationrest.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "product_category")
@Getter
@Setter
public class ProductCategory {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name")
    @NotEmpty(message = "{productcategory.name.empty.err}")
    @Size(min = 2, max = 100, message = "{productcategory.name.size.err}")
    private String name;

    @Column(name = "external_id")
    @NotNull
    private String externalId;
}
