package ru.aakhm.inflationrest.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aakhm.inflationrest.dto.PersonInDTO;
import ru.aakhm.inflationrest.dto.PersonOutDTO;
import ru.aakhm.inflationrest.models.Person;
import ru.aakhm.inflationrest.repo.PeopleRepo;
import ru.aakhm.inflationrest.repo.RolesRepo;
import ru.aakhm.inflationrest.security.PersonDetails;
import ru.aakhm.inflationrest.utils.Utils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PersonDetailsService implements UserDetailsService {
    private final PeopleRepo peopleRepo;
    private final RolesRepo rolesRepo;
    private final Utils utils;
    private final ModelMapper modelMapper;

    @Autowired
    public PersonDetailsService(PeopleRepo peopleRepo, RolesRepo rolesRepo, Utils utils, ModelMapper modelMapper) {
        this.peopleRepo = peopleRepo;
        this.rolesRepo = rolesRepo;
        this.utils = utils;
        this.modelMapper = modelMapper;
    }

    // readOnly = false methods


    // readOnly = true methods
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Person> person = peopleRepo.findByLogin(username);

        return person.map(PersonDetails::new).orElseThrow(() -> new UsernameNotFoundException(utils.getMessageFromBundle("person.login.notfound.err")));
    }

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

    private void enrichPerson(Person person) {
        person.setExternalId(utils.generatePersonSecureId());
    }

}
