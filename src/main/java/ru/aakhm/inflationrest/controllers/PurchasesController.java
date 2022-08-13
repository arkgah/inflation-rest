package ru.aakhm.inflationrest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.aakhm.inflationrest.dto.in.PurchaseInDTO;
import ru.aakhm.inflationrest.dto.out.ErrorDTO;
import ru.aakhm.inflationrest.dto.out.PurchaseOutDTO;
import ru.aakhm.inflationrest.dto.out.PurchasesOutDTO;
import ru.aakhm.inflationrest.models.validation.PurchaseInDTOValidator;
import ru.aakhm.inflationrest.models.validation.except.purchase.PurchaseNotCreatedException;
import ru.aakhm.inflationrest.models.validation.except.purchase.PurchaseNotUpdatedException;
import ru.aakhm.inflationrest.services.PurchasesService;
import ru.aakhm.inflationrest.utils.Utils;

import javax.validation.Valid;

@RestController
@RequestMapping("/purchases")
public class PurchasesController {
    private final PurchasesService purchasesService;
    private final PurchaseInDTOValidator purchaseInDTOValidator;
    private final Utils utils;

    @Autowired
    public PurchasesController(PurchasesService purchasesService, PurchaseInDTOValidator purchaseInDTOValidator, Utils utils) {
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

    @PutMapping("/{id}")
    // @PostAuthorize не будет работать (т.к. выходной объект уже содержит заменённый login, оставлено для примера других случаев.
    // @PostAuthorize("returnObject != null ? returnObject.body.person.login == principal.getPerson().getLogin() : true")
    public ResponseEntity<PurchaseOutDTO> update(@PathVariable("id") String externalId,
                                                 @RequestBody @Valid PurchaseInDTO purchaseInDTO,
                                                 BindingResult bindingResult) {
        purchaseInDTOValidator.validate(purchaseInDTO, bindingResult);
        String errorMsg = utils.getErrorMsg(bindingResult);
        if (!errorMsg.isEmpty())
            throw new PurchaseNotUpdatedException(errorMsg);

        return new ResponseEntity<>(purchasesService.update(externalId, purchaseInDTO), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public HttpStatus delete(@PathVariable("id") String externalId) {
        purchasesService.deleteByExternalId(externalId);
        return HttpStatus.NO_CONTENT;
    }

    @GetMapping
    public ResponseEntity<PurchasesOutDTO> index(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "per_page", required = false, defaultValue = "${purchases.per_page}") Integer perPage) {
        PurchasesOutDTO purchasesOutDTO = new PurchasesOutDTO();
        purchasesOutDTO.setPurchases(purchasesService.index(page, perPage));
        return new ResponseEntity<>(purchasesOutDTO, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOutDTO> getByExternalId(@PathVariable("id") String externalId) {
        return new ResponseEntity<>(purchasesService.getByExternalId(externalId), HttpStatus.OK);
    }


    @ExceptionHandler
    private ResponseEntity<ErrorDTO> handleException(RuntimeException e) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setMessage(e.getMessage());
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }
}
