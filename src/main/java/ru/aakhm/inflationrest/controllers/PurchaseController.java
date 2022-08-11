package ru.aakhm.inflationrest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.aakhm.inflationrest.dto.in.PurchaseInDTO;
import ru.aakhm.inflationrest.dto.out.ErrorDTO;
import ru.aakhm.inflationrest.dto.out.PurchaseOutDTO;
import ru.aakhm.inflationrest.models.validation.PurchaseInDTOValidator;
import ru.aakhm.inflationrest.models.validation.except.purchase.PurchaseNotCreatedException;
import ru.aakhm.inflationrest.services.PurchasesService;
import ru.aakhm.inflationrest.utils.Utils;

import javax.validation.Valid;

@RestController
@RequestMapping("/purchases")
public class PurchaseController {
    private final PurchasesService purchasesService;
    private final PurchaseInDTOValidator purchaseInDTOValidator;
    private final Utils utils;

    @Autowired
    public PurchaseController(PurchasesService purchasesService, PurchaseInDTOValidator purchaseInDTOValidator, Utils utils) {
        this.purchasesService = purchasesService;
        this.purchaseInDTOValidator = purchaseInDTOValidator;
        this.utils = utils;
    }

    @PostMapping
    public ResponseEntity<PurchaseOutDTO> create(@RequestBody @Valid PurchaseInDTO purchaseInDTO, BindingResult bindingResult) {
        purchaseInDTOValidator.validate(purchaseInDTO, bindingResult);
        String errorMsg = utils.getErrorMsg(bindingResult);
        if (!errorMsg.isEmpty())
            throw new PurchaseNotCreatedException(errorMsg);

        return new ResponseEntity<>(purchasesService.save(purchaseInDTO), HttpStatus.CREATED);
    }

    @ExceptionHandler
    private ResponseEntity<ErrorDTO> handleException(RuntimeException e) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setMessage(e.getMessage());
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }
}
