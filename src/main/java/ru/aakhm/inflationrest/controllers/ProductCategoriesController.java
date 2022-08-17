package ru.aakhm.inflationrest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.aakhm.inflationrest.dto.in.ProductCategoryInDTO;
import ru.aakhm.inflationrest.dto.out.ErrorDTO;
import ru.aakhm.inflationrest.dto.out.ProductCategoriesOutDTO;
import ru.aakhm.inflationrest.dto.out.ProductCategoryOutDTO;
import ru.aakhm.inflationrest.models.validation.ProductCategoryInDTOValidator;
import ru.aakhm.inflationrest.models.validation.except.productcategory.ProductCategoryNotCreatedException;
import ru.aakhm.inflationrest.models.validation.except.productcategory.ProductCategoryNotFoundException;
import ru.aakhm.inflationrest.models.validation.except.productcategory.ProductCategoryNotUpdatedException;
import ru.aakhm.inflationrest.services.ProductCategoriesService;
import ru.aakhm.inflationrest.utils.Utils;

import javax.validation.Valid;

@RestController
@RequestMapping("/products/categories")
public class ProductCategoriesController {
    private final ProductCategoriesService productCategoriesService;
    private final ProductCategoryInDTOValidator productCategoryInDTOValidator;
    private final Utils utils;

    @Autowired
    public ProductCategoriesController(ProductCategoriesService productCategoriesService, ProductCategoryInDTOValidator productCategoryInDTOValidator, Utils utils) {
        this.productCategoriesService = productCategoriesService;
        this.productCategoryInDTOValidator = productCategoryInDTOValidator;
        this.utils = utils;
    }

    @GetMapping
    public ResponseEntity<ProductCategoriesOutDTO> index(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "per-page", required = false, defaultValue = "${productcategories.per_page}") Integer perPage) {
        ProductCategoriesOutDTO res = new ProductCategoriesOutDTO();
        res.setCategories(productCategoriesService.index(page, perPage));
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductCategoryOutDTO> getById(@PathVariable("id") String externalId) {
        return new ResponseEntity<>(productCategoriesService.getByExternalId(externalId), HttpStatus.OK);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ProductCategoryOutDTO> getByName(@PathVariable("name") String name) {
        return new ResponseEntity<>(
                productCategoriesService.getByName(name).orElseThrow(() -> new ProductCategoryNotFoundException(utils.getMessageFromBundle("productcategory.notfound.err"))),
                HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ProductCategoryOutDTO> create(@RequestBody @Valid ProductCategoryInDTO productCategoryInDTO, BindingResult bindingResult) {
        productCategoryInDTOValidator.validate(productCategoryInDTO, bindingResult);
        String errorMsg = utils.getErrorMsg(bindingResult);
        if (!errorMsg.isEmpty())
            throw new ProductCategoryNotCreatedException(errorMsg);

        ProductCategoryOutDTO res = productCategoriesService.save(productCategoryInDTO);
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<HttpStatus> delete(@PathVariable("id") String id) {
        productCategoriesService.deleteByExternalId(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductCategoryOutDTO> update(@PathVariable("id") String id,
                                                        @RequestBody @Valid ProductCategoryInDTO productCategoryInDTO,
                                                        BindingResult bindingResult) {
        productCategoryInDTOValidator.validate(productCategoryInDTO, bindingResult);
        String errorMsg = utils.getErrorMsg(bindingResult);
        if (!errorMsg.isEmpty())
            throw new ProductCategoryNotUpdatedException(errorMsg);

        ProductCategoryOutDTO res = productCategoriesService.update(id, productCategoryInDTO);

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ExceptionHandler
    ResponseEntity<ErrorDTO> handleException(RuntimeException e) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setMessage(e.getMessage());
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    ResponseEntity<ErrorDTO> handleException(ProductCategoryNotFoundException e) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setMessage(e.getMessage());
        return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
    }

}
