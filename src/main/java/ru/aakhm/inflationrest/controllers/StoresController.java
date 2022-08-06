package ru.aakhm.inflationrest.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.aakhm.inflationrest.dto.ErrorDTO;
import ru.aakhm.inflationrest.dto.StoreInDTO;
import ru.aakhm.inflationrest.dto.StoreOutDTO;
import ru.aakhm.inflationrest.models.Store;
import ru.aakhm.inflationrest.models.validation.StoreInDTOValidator;
import ru.aakhm.inflationrest.models.validation.except.store.StoreNotCreatedException;
import ru.aakhm.inflationrest.services.StoresService;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/stores")
public class StoresController {
    private final ModelMapper modelMapper;
    private final StoresService storesService;
    private final StoreInDTOValidator storeInDTOValidator;

    public StoresController(ModelMapper modelMapper, StoresService storesService, StoreInDTOValidator storeInDTOValidator) {
        this.modelMapper = modelMapper;
        this.storesService = storesService;
        this.storeInDTOValidator = storeInDTOValidator;
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
}
