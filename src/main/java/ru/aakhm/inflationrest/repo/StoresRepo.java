package ru.aakhm.inflationrest.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.aakhm.inflationrest.models.Store;

import java.util.Optional;

@Repository
public interface StoresRepo extends JpaRepository<Store, Integer> {
    Optional<Store> getByName(String name);

    Optional<Store> getByExternalId(String externalId);
}
