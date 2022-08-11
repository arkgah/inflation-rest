package ru.aakhm.inflationrest.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.aakhm.inflationrest.models.Purchase;

public interface PurchasesRepo extends JpaRepository<Purchase, Integer> {
}
