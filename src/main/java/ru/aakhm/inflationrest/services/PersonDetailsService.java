package ru.aakhm.inflationrest.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    @Transactional
    public void assignRole(String externalId, String roleName) {
        Optional<PersonRole> role = rolesRepo.getByName(roleName);
        Optional<Person> person = peopleRepo.findByExternalId(externalId);

        person.ifPresentOrElse(p -> p.setRole(
                        role.orElseThrow(() -> new PersonRoleNotFoundException(utils.getMessageFromBundle("personrole.notfound.err")))
                ),
                () -> {
                    throw new PersonNotFoundException(utils.getMessageFromBundle("person.notfound.err"));
                });
    }

    @Transactional
    public void delete(String externalId) {
        Optional<Person> person = peopleRepo.findByExternalId(externalId);
        peopleRepo.delete(person.orElseThrow(() -> new PersonNotFoundException(utils.getMessageFromBundle("person.notfound.err"))));
    }

    // readOnly = true methods
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Person> person = peopleRepo.findByLogin(username);

        return person.map(PersonDetails::new).orElseThrow(() -> new UsernameNotFoundException(utils.getMessageFromBundle("person.login.notfound.err")));
    }

    public Optional<PersonOutDTO> getByLogin(String login) {
        return peopleRepo.findByLogin(login).map(this::fromPersonToPersonOutDto);
    }

    public PersonOutDTO getByExternalId(String externalId) {
        return peopleRepo.findByExternalId(externalId)
                .map(this::fromPersonToPersonOutDto)
                .orElseThrow(() -> new PersonNotFoundException(utils.getMessageFromBundle("person.notfound.err")));
    }

    public List<PersonOutDTO> index() {
        return peopleRepo.findAll().stream().map(this::fromPersonToPersonOutDto).collect(Collectors.toList());
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
