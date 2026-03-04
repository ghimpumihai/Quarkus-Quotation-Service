package org.stef.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "proposal")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Proposal {
    @Id
    @GeneratedValue
    private Long id;

    private String customer;

    @Column(name="price_tonne")
    private BigDecimal priceTonne;

    private Integer tonnes;

    private String country;

    @Column(name="proposal_validity_days")
    private Integer proposalValidityDays;

    private Date created;

}
