package ru.aakhm.inflationrest.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aakhm.inflationrest.dto.PersonInDTO;
import ru.aakhm.inflationrest.dto.PersonOutDTO;
import ru.aakhm.inflationrest.models.Person;
import ru.aakhm.inflationrest.repo.PeopleRepo;
import ru.aakhm.inflationrest.utils.Utils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PeopleService {
    private final PeopleRepo peopleRepo;
    private final Utils utils;
    private final ModelMapper modelMapper;

    @Autowired
    public PeopleService(PeopleRepo peopleRepo, Utils utils, ModelMapper modelMapper) {
        this.peopleRepo = peopleRepo;
        this.utils = utils;
        this.modelMapper = modelMapper;
    }

    // readOnly = true methods
    public Optional<Person> getByLogin(String login) {
        return peopleRepo.findByLogin(login);
    }

    public List<PersonOutDTO> index() {
        return peopleRepo.findAll().stream().map(this::fromPersonToPersonOutDto).collect(Collectors.toList());
    }

    private Person fromPersonInDtoToSPerson(PersonInDTO personInDTO) {
        return modelMapper.map(personInDTO, Person.class);
    }

    private PersonOutDTO fromPersonToPersonOutDto(Person person) {
        return modelMapper.map(person, PersonOutDTO.class);
    }
}
