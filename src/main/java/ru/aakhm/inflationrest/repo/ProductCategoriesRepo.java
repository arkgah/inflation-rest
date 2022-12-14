package ru.aakhm.inflationrest.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.aakhm.inflationrest.models.ProductCategory;

import java.util.Optional;

@Repository
public interface ProductCategoriesRepo extends JpaRepository<ProductCategory, Integer> {
    Optional<ProductCategory> getByName(String name);

    Optional<ProductCategory> getByExternalId(String externalId);
}
