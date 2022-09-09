package ru.aakhm.inflationrest.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.aakhm.inflationrest.models.Person;
import ru.aakhm.inflationrest.models.Product;
import ru.aakhm.inflationrest.models.Purchase;
import ru.aakhm.inflationrest.models.Store;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchasesRepo extends JpaRepository<Purchase, Integer> {
    Page<Purchase> findAll(Pageable pageable);

    Optional<Purchase> getByExternalId(String externalId);

    List<Purchase> getAllByPurchasedAtBetween(Date beginDate, Date endDate);

    Optional<Purchase> getByPurchasedAtAndProductAndStoreAndPerson(Date date, Product p, Store store, Person person);
}
