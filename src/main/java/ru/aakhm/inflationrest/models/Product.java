package ru.aakhm.inflationrest.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@Setter
public class Product {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name")
    @NotEmpty(message = "{product.name.empty.err}")
    @Size(min = 2, max = 100, message = "{product.name.size.err}")
    private String name;

    @Column(name = "unit")
    @Positive(message = "{product.unit.value.err}")
    private double unit;

    @Column(name = "external_id")
    @NotNull
    private String externalId;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private ProductCategory category;

    @OneToMany(mappedBy = "product")
    private List<Purchase> purchases;
}
