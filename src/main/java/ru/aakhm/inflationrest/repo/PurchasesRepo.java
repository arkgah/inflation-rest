package ru.aakhm.inflationrest.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.aakhm.inflationrest.models.Purchase;

@Repository
public interface PurchasesRepo extends JpaRepository<Purchase, Integer> {
}
