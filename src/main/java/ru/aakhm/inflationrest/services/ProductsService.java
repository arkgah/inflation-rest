package ru.aakhm.inflationrest.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aakhm.inflationrest.dto.in.ProductInDTO;
import ru.aakhm.inflationrest.dto.out.ProductOutDTO;
import ru.aakhm.inflationrest.models.Product;
import ru.aakhm.inflationrest.models.ProductCategory;
import ru.aakhm.inflationrest.models.validation.except.product.ProductNameCategoryUniqException;
import ru.aakhm.inflationrest.models.validation.except.product.ProductNotFoundException;
import ru.aakhm.inflationrest.models.validation.except.productcategory.ProductCategoryNotFoundException;
import ru.aakhm.inflationrest.repo.ProductCategoriesRepo;
import ru.aakhm.inflationrest.repo.ProductsRepo;
import ru.aakhm.inflationrest.utils.Utils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProductsService {
    private final ProductsRepo productsRepo;
    private final ProductCategoriesRepo productCategoriesRepo;
    private final Utils utils;
    private final ModelMapper modelMapper;

    @Autowired
    public ProductsService(ProductsRepo productsRepo, ProductCategoriesRepo productCategoriesRepo, Utils utils, ModelMapper modelMapper) {
        this.productsRepo = productsRepo;
        this.productCategoriesRepo = productCategoriesRepo;
        this.utils = utils;
        this.modelMapper = modelMapper;
    }

    // ========
    // readOnly = false methods
    @Transactional
    public ProductOutDTO save(ProductInDTO productInDTO) {
        Product product = fromProductInDtoToProduct(productInDTO);
        enrichProduct(product);
        return fromProductToProductOutDto(productsRepo.save(product));
    }

    @Transactional
    public ProductOutDTO update(String externalId, ProductInDTO productInDTO) {
        Product product = findByExternalId(externalId)
                .orElseThrow(() -> new ProductNotFoundException(utils.getMessageFromBundle("product.notfound.err")));
        ProductCategory newCategory = productCategoriesRepo.findByName(
                        productInDTO.getCategory().getName())
                .orElseThrow(() -> new ProductCategoryNotFoundException(
                        utils.getMessageFromBundle("productcategory.notfound.err")));
        productsRepo.getProductByNameAndCategory(productInDTO.getName(), newCategory)
                .ifPresent(p -> {
                    throw new ProductNameCategoryUniqException("product.name.category.uniq.err");
                });

        product.setName(productInDTO.getName());
        product.setCategory(newCategory);
        product.setUnit(productInDTO.getUnit());

        return fromProductToProductOutDto(productsRepo.save(product));
    }

    @Transactional
    public void deleteByExternalId(String externalId) {
        Product product = findByExternalId(externalId)
                .orElseThrow(() -> new ProductNotFoundException(utils.getMessageFromBundle("product.notfound.err")));
        productsRepo.delete(product);
    }

    // =======
    // readOnly = true methods
    public Optional<ProductOutDTO> getByNameAndCategoryName(String name, String categoryName) {
        Optional<ProductCategory> productCategory = productCategoriesRepo.findByName(categoryName);
        Optional<Product> product = productsRepo.getProductByNameAndCategory(
                name,
                productCategory.orElseThrow(
                        () -> new ProductCategoryNotFoundException(
                                utils.getMessageFromBundle("productcategory.notfound.err"))));
        if (product.isEmpty())
            return Optional.empty();
        return Optional.of(fromProductToProductOutDto(product.get()));
    }

    public List<ProductOutDTO> index() {
        return productsRepo.findAll().stream().map(this::fromProductToProductOutDto).collect(Collectors.toList());
    }

    public ProductOutDTO getByExternalId(String externalId) {
        return productsRepo.findByExternalId(externalId)
                .map(this::fromProductToProductOutDto)
                .orElseThrow(() -> new ProductNotFoundException(utils.getMessageFromBundle("product.notfound.err")));
    }


    private Product fromProductInDtoToProduct(ProductInDTO productInDTO) {
        Optional<ProductCategory> productCategory = productCategoriesRepo.findByName(productInDTO.getCategory().getName());
        Product product = modelMapper.map(productInDTO, Product.class);
        product.setCategory(productCategory.orElseThrow(() -> new ProductCategoryNotFoundException(utils.getMessageFromBundle("productcategory.notfound.err"))));
        return product;
    }

    private ProductOutDTO fromProductToProductOutDto(Product product) {
        return modelMapper.map(product, ProductOutDTO.class);
    }

    private void enrichProduct(Product product) {
        product.setExternalId(utils.generateExternalId());
    }

    private Optional<Product> findByExternalId(String externalId) {
        return productsRepo.findByExternalId(externalId);
    }

}
