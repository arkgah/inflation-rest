package ru.aakhm.inflationrest.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
public class ProductsService implements ExternalIdAndNameWithCategoryService<ProductInDTO, ProductOutDTO> {
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
    @Override
    @Transactional
    public ProductOutDTO save(ProductInDTO productInDTO) {
        Product product = fromProductInDtoToProduct(productInDTO);
        enrichProduct(product);
        return fromProductToProductOutDto(productsRepo.save(product));
    }

    @Override
    @Transactional
    public ProductOutDTO update(String externalId, ProductInDTO productInDTO) {
        Product product = findByExternalId(externalId)
                .orElseThrow(() -> new ProductNotFoundException(utils.getMessageFromBundle("product.notfound.err")));
        ProductCategory newCategory = productCategoriesRepo.getByName(
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

    @Override
    @Transactional
    public void deleteByExternalId(String externalId) {
        Product product = findByExternalId(externalId)
                .orElseThrow(() -> new ProductNotFoundException(utils.getMessageFromBundle("product.notfound.err")));
        productsRepo.delete(product);
    }

    // =======
    // readOnly = true methods
    @Override
    public Optional<ProductOutDTO> getByNameAndCategoryName(String name, String categoryName) {
        Optional<ProductCategory> productCategory = productCategoriesRepo.getByName(categoryName);
        Optional<Product> product = productsRepo.getProductByNameAndCategory(
                name,
                productCategory.orElseThrow(
                        () -> new ProductCategoryNotFoundException(
                                utils.getMessageFromBundle("productcategory.notfound.err"))));
        if (product.isEmpty())
            return Optional.empty();
        return Optional.of(fromProductToProductOutDto(product.get()));
    }

    @Override
    public List<ProductOutDTO> index(Integer page, Integer perPage) {
        return index(page, perPage, null, null);
    }

    @Override
    public List<ProductOutDTO> index(Integer page, Integer perPage, String nameLike, String categoryNameLike) {
        if (nameLike == null) {
            return productsRepo.findAll(PageRequest.of(page != null ? page : 0, perPage, Sort.by("category", "name"))).getContent()
                    .stream().map(this::fromProductToProductOutDto).collect(Collectors.toList());
        }
        String catNameLike = categoryNameLike != null ? categoryNameLike : "";
        return productsRepo.getAllByNameContainingIgnoreCaseAndCategory_NameContainingIgnoreCase(
                        PageRequest.of(page != null ? page : 0, perPage, Sort.by("category", "name")), nameLike, catNameLike).getContent()
                .stream().map(this::fromProductToProductOutDto).collect(Collectors.toList());
    }

    @Override
    public ProductOutDTO getByExternalId(String externalId) {
        return productsRepo.getByExternalId(externalId)
                .map(this::fromProductToProductOutDto)
                .orElseThrow(() -> new ProductNotFoundException(utils.getMessageFromBundle("product.notfound.err")));
    }

    // aux methods
    private Product fromProductInDtoToProduct(ProductInDTO productInDTO) {
        Optional<ProductCategory> productCategory = productCategoriesRepo.getByName(productInDTO.getCategory().getName());
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
        return productsRepo.getByExternalId(externalId);
    }

}
