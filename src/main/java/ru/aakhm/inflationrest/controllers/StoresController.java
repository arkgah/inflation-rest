package ru.aakhm.inflationrest.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.aakhm.inflationrest.dto.ErrorDTO;
import ru.aakhm.inflationrest.dto.StoreInDTO;
import ru.aakhm.inflationrest.dto.StoreOutDTO;
import ru.aakhm.inflationrest.dto.StoresOutDTO;
import ru.aakhm.inflationrest.models.validation.StoreInDTOValidator;
import ru.aakhm.inflationrest.models.validation.except.store.StoreNotCreatedException;
import ru.aakhm.inflationrest.models.validation.except.store.StoreNotFoundException;
import ru.aakhm.inflationrest.models.validation.except.store.StoreNotUpdatedException;
import ru.aakhm.inflationrest.services.StoresService;
import ru.aakhm.inflationrest.utils.Utils;

import javax.validation.Valid;

@RestController
@RequestMapping("/stores")
public class StoresController {
    private final StoresService storesService;
    private final StoreInDTOValidator storeInDTOValidator;
    private final Utils utils;

    public StoresController(StoresService storesService, StoreInDTOValidator storeInDTOValidator, Utils utils) {
        this.storesService = storesService;
        this.storeInDTOValidator = storeInDTOValidator;
        this.utils = utils;
    }

    @GetMapping
    public ResponseEntity<StoresOutDTO> index() {
        StoresOutDTO storesOutDTO = new StoresOutDTO();
        storesOutDTO.setStores(storesService.index());
        return new ResponseEntity<>(storesOutDTO, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreOutDTO> getById(@PathVariable("id") String externalId) {
        return new ResponseEntity<>(storesService.getByExternalId(externalId), HttpStatus.OK);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<StoreOutDTO> getByName(@PathVariable("name") String name) {
        return new ResponseEntity<>(
                storesService.getByName(name).orElseThrow(() -> new StoreNotFoundException(utils.getMessageFromBundle("store.notfound.err"))),
                HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<StoreOutDTO> create(@RequestBody @Valid StoreInDTO storeInDTO, BindingResult bindingResult) {
        storeInDTOValidator.validate(storeInDTO, bindingResult);
        String errorMsg = utils.getErrorMsg(bindingResult);
        if (!errorMsg.isEmpty())
            throw new StoreNotCreatedException(errorMsg);

        StoreOutDTO storeOutDTO = storesService.save(storeInDTO);

        return new ResponseEntity<>(storeOutDTO, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<HttpStatus> delete(@PathVariable("id") String id) {
        storesService.deleteByExternalId(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreOutDTO> update(@PathVariable("id") String id, @RequestBody @Valid StoreInDTO storeInDTO, BindingResult bindingResult) {
        storeInDTOValidator.validate(storeInDTO, bindingResult);
        String errorMsg = utils.getErrorMsg(bindingResult);
        if (!errorMsg.isEmpty())
            throw new StoreNotUpdatedException(errorMsg);

        StoreOutDTO storeOutDTO = storesService.update(id, storeInDTO);

        return new ResponseEntity<>(storeOutDTO, HttpStatus.OK);
    }

    @ExceptionHandler
    ResponseEntity<ErrorDTO> handleException(RuntimeException e) {
        ErrorDTO errorDTO = new ru.aakhm.inflationrest.dto.ErrorDTO();
        errorDTO.setMessage(e.getMessage());
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    ResponseEntity<ErrorDTO> handleException(StoreNotFoundException e) {
        ErrorDTO errorDTO = new ru.aakhm.inflationrest.dto.ErrorDTO();
        errorDTO.setMessage(e.getMessage());
        return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
    }

}
