package ru.aakhm.inflationrest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.aakhm.inflationrest.dto.PeopleOutDTO;
import ru.aakhm.inflationrest.services.PersonDetailsService;

@RestController
@RequestMapping("/people")
public class PeopleController {
    private final PersonDetailsService personDetailsService;

    @Autowired
    public PeopleController(PersonDetailsService personDetailsService) {
        this.personDetailsService = personDetailsService;
    }

    @GetMapping
    public ResponseEntity<PeopleOutDTO> index() {
        PeopleOutDTO peopleOutDTO = new PeopleOutDTO();
        peopleOutDTO.setPeople(personDetailsService.index());
        return new ResponseEntity<>(peopleOutDTO, HttpStatus.OK);
    }
}
