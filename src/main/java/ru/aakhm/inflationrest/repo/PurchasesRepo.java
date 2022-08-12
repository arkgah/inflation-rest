package ru.aakhm.inflationrest.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.aakhm.inflationrest.models.Purchase;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchasesRepo extends JpaRepository<Purchase, Integer> {
    Optional<Purchase> findByExternalId(String externalId);

    List<Purchase> findAllByPurchasedAtBetween(Date beginDate, Date endDate);
}
