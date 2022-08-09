package ru.aakhm.inflationrest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.aakhm.inflationrest.dto.in.PersonInDTO;
import ru.aakhm.inflationrest.dto.in.PersonLoginDTO;
import ru.aakhm.inflationrest.dto.out.ErrorDTO;
import ru.aakhm.inflationrest.dto.out.PersonOutDTO;
import ru.aakhm.inflationrest.models.validation.PersonInDTOValidator;
import ru.aakhm.inflationrest.models.validation.except.person.PersonLoginException;
import ru.aakhm.inflationrest.models.validation.except.person.PersonNotCreatedException;
import ru.aakhm.inflationrest.security.JWTUtil;
import ru.aakhm.inflationrest.services.RegistrationService;
import ru.aakhm.inflationrest.utils.Utils;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final RegistrationService registrationService;
    private final PersonInDTOValidator personInDTOValidator;
    private final Utils utils;
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Value("${security.bearer:Bearer }")
    private String BEARER;

    @Value("${header.external_id:USERID}")
    private String HEADER_EXTERNAL_ID;

    @Autowired
    public AuthController(RegistrationService registrationService, PersonInDTOValidator personInDTOValidator, Utils utils, JWTUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.registrationService = registrationService;

        this.personInDTOValidator = personInDTOValidator;
        this.utils = utils;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/registration")
    public ResponseEntity<PersonOutDTO> performRegistration(@RequestBody @Valid PersonInDTO personInDTO, BindingResult bindingResult) {
        personInDTOValidator.validate(personInDTO, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new PersonNotCreatedException(utils.getErrorMsg(bindingResult));
        }

        PersonOutDTO personOutDTO = registrationService.register(personInDTO);
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.set(HttpHeaders.AUTHORIZATION, BEARER + jwtUtil.generateToken(personOutDTO.getLogin()));
        respHeaders.set(HEADER_EXTERNAL_ID, personOutDTO.getExternalId());
        return new ResponseEntity<>(personOutDTO, respHeaders, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> performLogin(@RequestBody @Valid PersonLoginDTO personLoginDTO) {
        UsernamePasswordAuthenticationToken authInputToken = new UsernamePasswordAuthenticationToken(
                personLoginDTO.getLogin(), personLoginDTO.getPassword());
        try {
            authenticationManager.authenticate(authInputToken);
        } catch (BadCredentialsException e) {
            throw new PersonLoginException(utils.getMessageFromBundle("person.login.err"));
        }

        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.set(HttpHeaders.AUTHORIZATION, BEARER + jwtUtil.generateToken(personLoginDTO.getLogin()));
        respHeaders.set(HEADER_EXTERNAL_ID, registrationService.getExternalIdByLogin(personLoginDTO.getLogin()));
        return ResponseEntity.ok().headers(respHeaders).build();
    }

    @ExceptionHandler
    ResponseEntity<ErrorDTO> handleException(RuntimeException e) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setMessage(e.getMessage());
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }


}
