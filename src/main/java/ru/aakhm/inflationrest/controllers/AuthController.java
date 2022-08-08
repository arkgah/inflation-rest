package ru.aakhm.inflationrest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.aakhm.inflationrest.dto.PeopleOutDTO;
import ru.aakhm.inflationrest.models.validation.PersonInDTOValidator;
import ru.aakhm.inflationrest.services.PeopleService;
import ru.aakhm.inflationrest.utils.Utils;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final PeopleService peopleService;
    private final PersonInDTOValidator personInDTOValidator;
    private final Utils utils;

    @Autowired
    public AuthController(PeopleService peopleService, PersonInDTOValidator personInDTOValidator, Utils utils) {
        this.peopleService = peopleService;
        this.personInDTOValidator = personInDTOValidator;
        this.utils = utils;
    }

    @GetMapping
    public ResponseEntity<PeopleOutDTO> index() {
        PeopleOutDTO peopleOutDTO = new PeopleOutDTO();
        peopleOutDTO.setPeople(peopleService.index());
        return new ResponseEntity<>(peopleOutDTO, HttpStatus.OK);
    }


}
