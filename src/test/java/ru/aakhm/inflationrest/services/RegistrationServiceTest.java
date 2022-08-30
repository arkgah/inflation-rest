package ru.aakhm.inflationrest.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.aakhm.inflationrest.dto.in.PersonInDTO;
import ru.aakhm.inflationrest.dto.out.PersonOutDTO;
import ru.aakhm.inflationrest.models.Person;
import ru.aakhm.inflationrest.models.PersonRole;
import ru.aakhm.inflationrest.models.validation.except.person.PersonNotFoundException;
import ru.aakhm.inflationrest.repo.PeopleRepo;
import ru.aakhm.inflationrest.repo.RolesRepo;
import ru.aakhm.inflationrest.utils.Utils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegistrationServiceTest {
    @InjectMocks
    private RegistrationService registrationService;

    @Mock
    private PeopleRepo peopleRepo;

    @Mock
    private RolesRepo rolesRepo;

    @Mock
    private Utils utils;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private static final String LOGIN = "login";
    private static final String PASSWORD = "password";
    private static final String FIRST_NAME = "First";
    private static final String LAST_NAME = "Last";
    private static final String EXTERNAL_ID = "123abc";

    private final PersonInDTO personInDTO = new PersonInDTO() {
        {
            setLogin(LOGIN);
            setPassword(PASSWORD);
            setFirstName(FIRST_NAME);
            setLastName(LAST_NAME);
        }
    };

    private final PersonOutDTO personOutDTO = new PersonOutDTO() {
        {
            setLogin(LOGIN);
            setFirstName(FIRST_NAME);
            setLastName(LAST_NAME);
            setExternalId(EXTERNAL_ID);
        }
    };

    private final PersonRole personRole = new PersonRole() {
        {
            setName("USER");
            setId(1);
        }
    };

    private final Person person = new Person() {
        {
            setLogin(LOGIN);
            setFirstName(FIRST_NAME);
            setLastName(LAST_NAME);
            setRole(personRole);
            setExternalId(EXTERNAL_ID);
        }
    };

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register() {
        when(modelMapper.map(any(PersonInDTO.class), any())).thenReturn(person);
        when(passwordEncoder.encode(any(String.class))).thenReturn(PASSWORD);
        when(rolesRepo.getByName(any(String.class))).thenReturn(Optional.of(personRole));
        when(modelMapper.map(any(Person.class), any())).thenReturn(personOutDTO);
        when(peopleRepo.save(any(Person.class))).thenReturn(person);

        PersonOutDTO res = registrationService.register(personInDTO);
        verify(peopleRepo, times(1)).save(any(Person.class));
        verify(modelMapper, times(1)).map(any(PersonInDTO.class), any());
        verify(modelMapper, times(1)).map(any(Person.class), any());

        assertNotNull(res);
        assertEquals(EXTERNAL_ID, res.getExternalId());
        assertEquals(LOGIN, res.getLogin());
    }

    @Test
    void update() {
        // person существует
        when(peopleRepo.getByExternalId(any(String.class))).thenReturn(Optional.of(person));
        when(modelMapper.map(any(PersonInDTO.class), any())).thenReturn(person);
        when(passwordEncoder.encode(any(String.class))).thenReturn(PASSWORD);
        when(modelMapper.map(any(Person.class), any())).thenReturn(personOutDTO);
        when(peopleRepo.save(any(Person.class))).thenReturn(person);

        PersonOutDTO res = registrationService.update(EXTERNAL_ID, personInDTO);
        verify(peopleRepo, times(1)).save(any(Person.class));
        verify(modelMapper, times(1)).map(any(PersonInDTO.class), any());
        verify(modelMapper, times(1)).map(any(Person.class), any());

        assertNotNull(res);
        assertEquals(EXTERNAL_ID, res.getExternalId());
        assertEquals(LOGIN, res.getLogin());

        // person не существует
        reset(peopleRepo);
        when(peopleRepo.getByExternalId(any(String.class))).thenReturn(Optional.empty());
        assertThrows(PersonNotFoundException.class, () -> registrationService.update(EXTERNAL_ID, personInDTO));
        verify(peopleRepo, times(0)).save(any(Person.class));
    }

    @Test
    void getExternalIdByLogin() {
        // login существует
        when(peopleRepo.getByLogin(any(String.class))).thenReturn(Optional.of(person));
        String res = registrationService.getExternalIdByLogin(LOGIN);
        verify(peopleRepo, times(1)).getByLogin(any(String.class));
        assertEquals(EXTERNAL_ID, res);

        // login не существует
        reset(peopleRepo);
        when(peopleRepo.getByLogin(any(String.class))).thenReturn(Optional.empty());
        assertThrows(PersonNotFoundException.class, () -> registrationService.getExternalIdByLogin(LOGIN));
        verify(peopleRepo, times(1)).getByLogin(any(String.class));
    }
}