package ru.aakhm.inflationrest.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "purchase")
@Getter
@Setter
public class Purchase {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "price")
    @Positive(message = "{purchase.price.positive}")
    private BigDecimal price;

    @Column(name = "unit")
    @Positive(message = "{purchase.unit.value.err}")
    private double unit;

    @NotNull(message = "{purchase.date.null.err}")
    private Date purchasedAt;

    @Size(max = 200, message = "{purchase.memo.size}")
    private String memo;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "store_id", referencedColumnName = "id")
    private Store store;

    @ManyToOne
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    private Person person;

    @Column(name = "external_id")
    @NotNull
    private String externalId;

}
