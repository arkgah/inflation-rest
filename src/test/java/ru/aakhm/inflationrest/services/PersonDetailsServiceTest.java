package ru.aakhm.inflationrest.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.aakhm.inflationrest.dto.out.PersonOutDTO;
import ru.aakhm.inflationrest.models.Person;
import ru.aakhm.inflationrest.models.PersonRole;
import ru.aakhm.inflationrest.models.validation.except.person.PersonNotFoundException;
import ru.aakhm.inflationrest.models.validation.except.person.PersonRoleNotFoundException;
import ru.aakhm.inflationrest.repo.PeopleRepo;
import ru.aakhm.inflationrest.repo.RolesRepo;
import ru.aakhm.inflationrest.security.Role;
import ru.aakhm.inflationrest.utils.Utils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PersonDetailsServiceTest {
    @InjectMocks
    PersonDetailsService personDetailsService;

    @Mock
    private PeopleRepo peopleRepo;

    @Mock
    private RolesRepo rolesRepo;

    @Mock
    private Utils utils;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private Person person;

    @Mock
    private PersonOutDTO personOutDTO;

    private static final String EXTERNAL_ID = "123abc";
    private static final String LOGIN = "login";

    private final PersonRole personRole = new PersonRole() {
        {
            setId(1);
            setName(Role.ROLE_USER.name());
        }
    };

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void assignRole() {
        when(rolesRepo.getByName(any(String.class))).thenReturn(Optional.of(personRole));
        when(peopleRepo.getByExternalId(any(String.class))).thenReturn(Optional.of(person));

        assertDoesNotThrow(() -> personDetailsService.assignRole(EXTERNAL_ID, Role.ROLE_USER.name()));
        verify(rolesRepo, times(1)).getByName(any(String.class));
        verify(peopleRepo, times(1)).getByExternalId(any(String.class));
        verify(person, times(1)).setRole(any(PersonRole.class));

        reset(person);
        when(peopleRepo.getByExternalId(any(String.class))).thenReturn(Optional.empty());
        assertThrows(PersonNotFoundException.class, () -> personDetailsService.assignRole(EXTERNAL_ID, Role.ROLE_USER.name()));
        verify(person, times(0)).setRole(any(PersonRole.class));

        reset(person);
        when(peopleRepo.getByExternalId(any(String.class))).thenReturn(Optional.of(person));
        when(rolesRepo.getByName(any(String.class))).thenReturn(Optional.empty());
        assertThrows(PersonRoleNotFoundException.class, () -> personDetailsService.assignRole(EXTERNAL_ID, Role.ROLE_USER.name()));
        verify(person, times(0)).setRole(any(PersonRole.class));
    }

    @Test
    void delete() {
        when(peopleRepo.getByExternalId(any(String.class))).thenReturn(Optional.of(person));
        assertDoesNotThrow(() -> personDetailsService.delete(EXTERNAL_ID));
        verify(peopleRepo, times(1)).delete(any(Person.class));

        reset(peopleRepo);
        when(peopleRepo.getByExternalId(any(String.class))).thenReturn(Optional.empty());
        assertThrows(PersonNotFoundException.class, () -> personDetailsService.delete(EXTERNAL_ID));
        verify(peopleRepo, times(0)).delete(any(Person.class));
    }

    @Test
    void loadUserByUsername() {
        when(peopleRepo.getByLogin(any(String.class))).thenReturn(Optional.of(person));
        UserDetails res = assertDoesNotThrow(() -> personDetailsService.loadUserByUsername(LOGIN));
        assertNotNull(res);
        verify(peopleRepo, times(1)).getByLogin(any(String.class));

        when(peopleRepo.getByLogin(any(String.class))).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> personDetailsService.loadUserByUsername(LOGIN));
    }

    @Test
    void getByLogin() {
        when(modelMapper.map(any(Person.class), any())).thenReturn(personOutDTO);
        when(peopleRepo.getByLogin(any(String.class))).thenReturn(Optional.of(person));
        Optional<PersonOutDTO> res = assertDoesNotThrow(() -> personDetailsService.getByLogin(LOGIN));
        assertEquals(Optional.of(personOutDTO), res);

        when(peopleRepo.getByLogin(any(String.class))).thenReturn(Optional.empty());
        res = assertDoesNotThrow(() -> personDetailsService.getByLogin(LOGIN));
        assertEquals(Optional.empty(), res);
    }

    @Test
    void getByExternalId() {
        when(modelMapper.map(any(Person.class), any())).thenReturn(personOutDTO);
        when(peopleRepo.getByExternalId(any(String.class))).thenReturn(Optional.of(person));
        PersonOutDTO res = assertDoesNotThrow(() -> personDetailsService.getByExternalId(EXTERNAL_ID));
        assertEquals(personOutDTO, res);

        when(peopleRepo.getByExternalId(any(String.class))).thenReturn(Optional.empty());
        assertThrows(PersonNotFoundException.class, () -> personDetailsService.getByExternalId(EXTERNAL_ID));
    }

    @Test
    void index() {
        when(peopleRepo.findAll()).thenReturn(repoIndex());
        when(modelMapper.map(any(Person.class), any())).thenReturn(personOutDTO);
        List<PersonOutDTO> res = assertDoesNotThrow(() -> personDetailsService.index());
        assertNotNull(res);
        assertEquals(repoIndex().size(), res.size());
        verify(peopleRepo, times(1)).findAll();
    }

    private List<Person> repoIndex() {
        return List.of(person);
    }
}