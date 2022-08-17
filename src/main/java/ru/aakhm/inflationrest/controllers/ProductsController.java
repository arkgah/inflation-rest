package ru.aakhm.inflationrest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.aakhm.inflationrest.dto.in.ProductInDTO;
import ru.aakhm.inflationrest.dto.out.ErrorDTO;
import ru.aakhm.inflationrest.dto.out.ProductOutDTO;
import ru.aakhm.inflationrest.dto.out.ProductsOutDTO;
import ru.aakhm.inflationrest.models.validation.ProductInDTOValidator;
import ru.aakhm.inflationrest.models.validation.except.product.ProductNotCreatedException;
import ru.aakhm.inflationrest.models.validation.except.product.ProductNotFoundException;
import ru.aakhm.inflationrest.models.validation.except.product.ProductNotUpdatedException;
import ru.aakhm.inflationrest.services.ProductsService;
import ru.aakhm.inflationrest.utils.Utils;

import javax.validation.Valid;

@RestController
@RequestMapping("/products")
public class ProductsController {
    private final ProductsService productsService;
    private final ProductInDTOValidator productInDTOValidator;
    private final Utils utils;

    @Autowired
    public ProductsController(ProductsService productsService, ProductInDTOValidator productInDTOValidator, Utils utils) {
        this.productsService = productsService;
        this.productInDTOValidator = productInDTOValidator;
        this.utils = utils;
    }

    @GetMapping
    public ResponseEntity<ProductsOutDTO> index(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "per-page", required = false, defaultValue = "${products.per_page}") Integer perPage,
            @RequestParam(name = "name-like", required = false) String nameLike,
            @RequestParam(name = "cat-name-like", required = false) String categoryNameLike
    ) {
        ProductsOutDTO res = new ProductsOutDTO();
        res.setProducts(productsService.index(page, perPage, nameLike, categoryNameLike));
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductOutDTO> getById(@PathVariable("id") String externalId) {
        return new ResponseEntity<>(productsService.getByExternalId(externalId), HttpStatus.OK);
    }

    @GetMapping("/name/{name}/category/{categoryName}")
    public ResponseEntity<ProductOutDTO> getByNameAndCategoryName(@PathVariable("name") String name,
                                                                  @PathVariable("categoryName") String categoryName) {
        return new ResponseEntity<>(
                productsService.getByNameAndCategoryName(name, categoryName)
                        .orElseThrow(
                                () -> new ProductNotFoundException(
                                        utils.getMessageFromBundle("product.notfound.err"))), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<HttpStatus> delete(@PathVariable("id") String externalId) {
        productsService.deleteByExternalId(externalId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping
    public ResponseEntity<ProductOutDTO> create(@RequestBody @Valid ProductInDTO productInDTO, BindingResult bindingResult) {
        productInDTOValidator.validate(productInDTO, bindingResult);
        String errorMsg = utils.getErrorMsg(bindingResult);
        if (!errorMsg.isEmpty())
            throw new ProductNotCreatedException(errorMsg);

        ProductOutDTO res = productsService.save(productInDTO);
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductOutDTO> update(@PathVariable("id") String externalId,
                                                @RequestBody ProductInDTO productInDTO, BindingResult bindingResult) {
        productInDTOValidator.validate(productInDTO, bindingResult);
        String errorMsg = utils.getErrorMsg(bindingResult);
        if (!errorMsg.isEmpty())
            throw new ProductNotUpdatedException(errorMsg);

        ProductOutDTO res = productsService.update(externalId, productInDTO);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ExceptionHandler
    ResponseEntity<ErrorDTO> handleException(RuntimeException e) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setMessage(e.getMessage());
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

}
