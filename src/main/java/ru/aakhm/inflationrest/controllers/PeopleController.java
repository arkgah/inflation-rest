package ru.aakhm.inflationrest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.aakhm.inflationrest.dto.out.ErrorDTO;
import ru.aakhm.inflationrest.dto.out.PeopleOutDTO;
import ru.aakhm.inflationrest.dto.out.PersonOutDTO;
import ru.aakhm.inflationrest.models.validation.except.person.PersonNotFoundException;
import ru.aakhm.inflationrest.services.PersonDetailsService;
import ru.aakhm.inflationrest.utils.Utils;

@RestController
@RequestMapping("/people")
public class PeopleController {
    private final PersonDetailsService personDetailsService;
    private final Utils utils;

    @Autowired
    public PeopleController(PersonDetailsService personDetailsService, Utils utils) {
        this.personDetailsService = personDetailsService;
        this.utils = utils;
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<PeopleOutDTO> index() {
        PeopleOutDTO peopleOutDTO = new PeopleOutDTO();
        peopleOutDTO.setPeople(personDetailsService.index());
        return new ResponseEntity<>(peopleOutDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<HttpStatus> delete(@PathVariable("id") String externalId) {
        personDetailsService.delete(externalId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    @PostAuthorize("hasRole('ROLE_ADMIN') or returnObject.body == null ? true : returnObject.body.login == principal.getPerson().getLogin()")
    public ResponseEntity<PersonOutDTO> getByExternalId(@PathVariable("id") String externalId) {
        return new ResponseEntity<>(personDetailsService.getByExternalId(externalId), HttpStatus.OK);
    }

    @GetMapping("/login/{login}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or #login == principal.getPerson().getLogin()")
    public ResponseEntity<PersonOutDTO> getByLogin(@PathVariable("login") String login) {
        return new ResponseEntity<>(
                personDetailsService.getByLogin(login)
                        .orElseThrow(() -> new PersonNotFoundException(utils.getMessageFromBundle("person.notfound.err"))),
                HttpStatus.OK);
    }

    @PatchMapping("/{id}/role/{name}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<HttpStatus> assignRole(@PathVariable("id") String externalId, @PathVariable("name") String roleName) {
        personDetailsService.assignRole(externalId, roleName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ExceptionHandler
    ResponseEntity<ErrorDTO> handleException(PersonNotFoundException e) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setMessage(e.getMessage());
        return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    ResponseEntity<ErrorDTO> handleException(RuntimeException e) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setMessage(e.getMessage());
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }
}
