package org.stef.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.stef.entity.Opportunity;

@ApplicationScoped
public class OpportunitiesRepository implements PanacheRepository<Opportunity> {
}
