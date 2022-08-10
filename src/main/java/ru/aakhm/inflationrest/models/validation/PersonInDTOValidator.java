package ru.aakhm.inflationrest.models.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.HandlerMapping;
import ru.aakhm.inflationrest.dto.in.PersonInDTO;
import ru.aakhm.inflationrest.dto.out.PersonOutDTO;
import ru.aakhm.inflationrest.services.PersonDetailsService;
import ru.aakhm.inflationrest.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

@Component
public class PersonInDTOValidator implements Validator {
    private final PersonDetailsService personDetailsService;
    private final Utils utils;
    private final HttpServletRequest request;

    @Value("${password.length:8}")
    int PASSWORD_LENGTH;

    @Autowired
    public PersonInDTOValidator(PersonDetailsService personDetailsService, Utils utils, HttpServletRequest request) {
        this.personDetailsService = personDetailsService;
        this.utils = utils;
        this.request = request;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(PersonInDTO.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PersonInDTO personInDTO = (PersonInDTO) target;
        Optional<PersonOutDTO> person = personDetailsService.getByLogin(personInDTO.getLogin());

        Map<String, String> attributes = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        System.out.println(attributes);

        if ((person.isPresent() && !attributes.containsKey("id")) ||
                (person.isPresent() && attributes.containsKey("id") && !attributes.get("id").equals(person.get().getExternalId()))) {
            errors.rejectValue("login", utils.getMessageFromBundle("person.login.uniq.err"));
        }

        if (!isStrongPassword(personInDTO.getPassword())) {
            errors.rejectValue("password", utils.getMessageFromBundle("person.password.strong.err", PASSWORD_LENGTH));
        }

    }

    private boolean isStrongPassword(String password) {
        final String SPEC_CHARS = "!@#$%^&*()-+";

        boolean lowerCase = false;
        boolean upperCase = false;
        boolean digit = false;
        boolean specChar = false;

        int len = password.length();
        if (len < PASSWORD_LENGTH)
            return false;

        for (int i = 0; i < len; i++) {
            char c = password.charAt(i);
            if (Character.isLowerCase(c))
                lowerCase = true;
            if (Character.isUpperCase(c))
                upperCase = true;
            if (Character.isDigit(c))
                digit = true;
            if (SPEC_CHARS.indexOf(c) >= 0)
                specChar = true;
            if (i < len - 1) {
                if (c == password.charAt(i + 1))
                    return false;
            }
        }

        return lowerCase && upperCase && digit && specChar;
    }
}
