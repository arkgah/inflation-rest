package ru.aakhm.inflationrest.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.aakhm.inflationrest.models.Store;

import java.util.Optional;

@Repository
public interface StoresRepo extends CrudRepository<Store, Integer> {
    Optional<Store> findByName(String name);
}
