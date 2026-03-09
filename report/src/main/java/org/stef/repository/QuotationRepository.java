package org.stef.repository;


import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.stef.entity.Quotation;

@ApplicationScoped
public class QuotationRepository implements PanacheRepository<Quotation> {
}
