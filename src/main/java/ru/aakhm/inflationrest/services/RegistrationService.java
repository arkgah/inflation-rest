package ru.aakhm.inflationrest.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aakhm.inflationrest.dto.in.PersonInDTO;
import ru.aakhm.inflationrest.dto.out.PersonOutDTO;
import ru.aakhm.inflationrest.models.Person;
import ru.aakhm.inflationrest.models.PersonRole;
import ru.aakhm.inflationrest.models.validation.except.person.PersonNotFoundException;
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
        Person person = fromPersonInDtoToPerson(personInDTO);
        enrichPerson(person);
        person.setPassword(passwordEncoder.encode(person.getPassword()));
        Optional<PersonRole> role = rolesRepo.getByName(Role.ROLE_USER.name());
        person.setRole(role.orElseThrow(() -> new PersonRoleNotFoundException(utils.getMessageFromBundle("personrole.notfound.err"))));
        person = peopleRepo.save(person);
        return fromPersonToPersonOutDto(person);
    }

    @Transactional
    public PersonOutDTO update(String externalId, PersonInDTO personInDTO) {
        Person oldPersonEntity = findByExternalId(externalId);
        Person newPersonEntity = fromPersonInDtoToPerson(personInDTO);

        newPersonEntity.setPassword(passwordEncoder.encode(newPersonEntity.getPassword()));

        oldPersonEntity.setPassword(newPersonEntity.getPassword());
        oldPersonEntity.setLastName(newPersonEntity.getFirstName());
        oldPersonEntity.setFirstName(newPersonEntity.getLastName());

        newPersonEntity = peopleRepo.save(oldPersonEntity);
        return fromPersonToPersonOutDto(newPersonEntity);
    }

    public String getExternalIdByLogin(String login) {
        Optional<Person> person = peopleRepo.getByLogin(login);
        return person.map(Person::getExternalId).orElseThrow(() -> new PersonNotFoundException(utils.getMessageFromBundle("person.notfound.err")));
    }

    private Person findByExternalId(String externalId) {
        return peopleRepo.getByExternalId(externalId).orElseThrow(() -> new PersonNotFoundException(utils.getMessageFromBundle("person.notfound.err")));
    }

    private Person fromPersonInDtoToPerson(PersonInDTO personInDTO) {
        return modelMapper.map(personInDTO, Person.class);
    }

    private PersonOutDTO fromPersonToPersonOutDto(Person person) {
        return modelMapper.map(person, PersonOutDTO.class);
    }

    private void enrichPerson(Person person) {
        person.setExternalId(utils.generateExternalId());
    }
}
