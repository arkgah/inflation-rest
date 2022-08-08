package ru.aakhm.inflationrest.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.aakhm.inflationrest.models.PersonRole;

import java.util.Optional;

@Repository
public interface RolesRepo extends JpaRepository<PersonRole, Integer> {
    Optional<PersonRole> getByName(String name);
}
