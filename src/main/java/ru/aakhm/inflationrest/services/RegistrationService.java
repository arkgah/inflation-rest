package ru.aakhm.inflationrest.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aakhm.inflationrest.dto.PersonInDTO;
import ru.aakhm.inflationrest.dto.PersonOutDTO;
import ru.aakhm.inflationrest.models.Person;
import ru.aakhm.inflationrest.models.PersonRole;
import ru.aakhm.inflationrest.models.validation.except.person.PersonRoleNotFoundException;
import ru.aakhm.inflationrest.repo.PeopleRepo;
import ru.aakhm.inflationrest.repo.RolesRepo;
import ru.aakhm.inflationrest.security.Role;
import ru.aakhm.inflationrest.utils.Utils;

import java.util.Optional;

@Service
public class RegistrationService {
    private final PeopleRepo peopleRepo;
    private final RolesRepo rolesRepo;
    private final Utils utils;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RegistrationService(PeopleRepo peopleRepo, RolesRepo rolesRepo, Utils utils, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.peopleRepo = peopleRepo;
        this.rolesRepo = rolesRepo;
        this.utils = utils;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public PersonOutDTO register(PersonInDTO personInDTO) {
        Person person = fromPersonInDtoToSPerson(personInDTO);
        enrichPerson(person);
        person.setPassword(passwordEncoder.encode(person.getPassword()));
        Optional<PersonRole> role = rolesRepo.getByName(Role.ROLE_USER.name());
        person.setRole(role.orElseThrow(() -> new PersonRoleNotFoundException(utils.getMessageFromBundle("personrole.notfound.err"))));
        person = peopleRepo.save(person);
        return fromPersonToPersonOutDto(person);
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
