package ru.aakhm.inflationrest.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.aakhm.inflationrest.models.Person;

import java.util.Optional;

@Repository
public interface PeopleRepo extends JpaRepository<Person, Integer> {
    Optional<Person> findByLogin(String login);
}
