package ru.aakhm.inflationrest.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.aakhm.inflationrest.models.Product;
import ru.aakhm.inflationrest.models.ProductCategory;

import java.util.Optional;

@Repository
public interface ProductsRepo extends JpaRepository<Product, Integer> {
    Optional<Product> getProductByNameAndCategory(String name, ProductCategory productCategory);

    Optional<Product> getByExternalId(String externalId);

    Page<Product> getAllByNameContainingIgnoreCaseAndCategory_NameContainingIgnoreCase(Pageable pageable, String nameLike, String categoryNameLike);
}
