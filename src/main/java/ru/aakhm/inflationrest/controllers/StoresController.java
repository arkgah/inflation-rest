package ru.aakhm.inflationrest.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.aakhm.inflationrest.dto.ErrorDTO;
import ru.aakhm.inflationrest.dto.StoreInDTO;
import ru.aakhm.inflationrest.dto.StoreOutDTO;
import ru.aakhm.inflationrest.models.Store;
import ru.aakhm.inflationrest.models.validation.StoreInDTOValidator;
import ru.aakhm.inflationrest.models.validation.except.store.StoreNotCreatedException;
import ru.aakhm.inflationrest.models.validation.except.store.StoreNotFoundException;
import ru.aakhm.inflationrest.services.StoresService;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/stores")
public class StoresController {
    private final ModelMapper modelMapper;
    private final StoresService storesService;
    private final StoreInDTOValidator storeInDTOValidator;
    private final MessageSource messageSource;

    public StoresController(ModelMapper modelMapper, StoresService storesService, StoreInDTOValidator storeInDTOValidator, MessageSource messageSource) {
        this.modelMapper = modelMapper;
        this.storesService = storesService;
        this.storeInDTOValidator = storeInDTOValidator;
        this.messageSource = messageSource;
    }

    @PostMapping
    public ResponseEntity<StoreOutDTO> create(@RequestBody @Valid StoreInDTO storeInDTO, BindingResult bindingResult) {
        storeInDTOValidator.validate(storeInDTO, bindingResult);
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            String errorMsg = errors.stream()
                    .map(e -> new StringBuilder().append(e.getField()).append(" - ").append(e.getDefaultMessage() != null ? e.getDefaultMessage() : e.getCode()))
                    .collect(Collectors.joining(";"));
            throw new StoreNotCreatedException(errorMsg);
        }

        Store store = fromStoreInDtoToStore(storeInDTO);
        store = storesService.save(store);
        StoreOutDTO storeOutDTO = fromStoreToStoreOutDTO(store);
        return new ResponseEntity<>(storeOutDTO, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> delete(@PathVariable("id") String id) {
        Optional<Store> store = storesService.getByExternalId(id);
        store.orElseThrow(() -> new StoreNotFoundException(messageSource.getMessage("store.notfound.err", null, LocaleContextHolder.getLocale())));
        storesService.deleteById(store.get().getId());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Store fromStoreInDtoToStore(StoreInDTO storeInDTO) {
        return modelMapper.map(storeInDTO, Store.class);
    }

    private StoreOutDTO fromStoreToStoreOutDTO(Store store) {
        return modelMapper.map(store, StoreOutDTO.class);
    }

    @ExceptionHandler
    private ResponseEntity<ErrorDTO> handleException(StoreNotCreatedException e) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setMessage(e.getMessage());
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    private ResponseEntity<ErrorDTO> handleException(StoreNotFoundException e) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setMessage(e.getMessage());
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    ResponseEntity<ErrorDTO> handleException(RuntimeException e) {
        ru.aakhm.inflationrest.dto.ErrorDTO errorDTO = new ru.aakhm.inflationrest.dto.ErrorDTO();
        errorDTO.setMessage(e.getMessage());
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }
}
