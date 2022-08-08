package ru.aakhm.inflationrest.models.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.aakhm.inflationrest.dto.PersonInDTO;
import ru.aakhm.inflationrest.models.Person;
import ru.aakhm.inflationrest.services.PeopleService;
import ru.aakhm.inflationrest.utils.Utils;

import java.util.Optional;

@Component
public class PersonInDTOValidator implements Validator {
    private final PeopleService peopleService;
    private final Utils utils;

    @Autowired
    public PersonInDTOValidator(PeopleService peopleService, Utils utils) {
        this.peopleService = peopleService;
        this.utils = utils;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(PersonInDTO.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PersonInDTO personInDTO = (PersonInDTO) target;
        Optional<Person> person = peopleService.getByLogin(personInDTO.getLogin());
        if (person.isPresent()) {
            errors.rejectValue("login", utils.getMessageFromBundle("person.login.uniq.err"));
        }
    }
}
