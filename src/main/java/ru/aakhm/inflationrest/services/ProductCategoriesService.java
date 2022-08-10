package ru.aakhm.inflationrest.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aakhm.inflationrest.dto.in.ProductCategoryInDTO;
import ru.aakhm.inflationrest.dto.out.ProductCategoryOutDTO;
import ru.aakhm.inflationrest.models.ProductCategory;
import ru.aakhm.inflationrest.models.validation.except.productcategory.ProductCategoryNotFoundException;
import ru.aakhm.inflationrest.repo.ProductCategoriesRepo;
import ru.aakhm.inflationrest.utils.Utils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProductCategoriesService {
    private final ProductCategoriesRepo productCategoriesRepo;
    private final Utils utils;
    private final ModelMapper modelMapper;

    @Autowired
    public ProductCategoriesService(ProductCategoriesRepo productCategoriesRepo, Utils utils, ModelMapper modelMapper) {
        this.productCategoriesRepo = productCategoriesRepo;
        this.utils = utils;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public ProductCategoryOutDTO save(ProductCategoryInDTO productCategoryInDTO) {
        ProductCategory productCategory = fromProductCategoryInDtoToProductCategory(productCategoryInDTO);
        enrichProductCategory(productCategory);
        ProductCategory savedProductCategory = productCategoriesRepo.save(productCategory);
        return fromProductCategoryToProductCategoryOutDto(savedProductCategory);
    }

    @Transactional
    public void deleteByExternalId(String externalId) {
        ProductCategory productCategory = findByExternalId(externalId)
                .orElseThrow(
                        () -> new ProductCategoryNotFoundException(utils.getMessageFromBundle("productcategory.notfound.err")));

        productCategoriesRepo.deleteById(productCategory.getId());
    }

    @Transactional
    public ProductCategoryOutDTO update(String externalId, ProductCategoryInDTO updatedProductCategory) {
        ProductCategory productCategoryEntity = findByExternalId(externalId)
                .orElseThrow(
                        () -> new ProductCategoryNotFoundException(utils.getMessageFromBundle("productcategory.notfound.err")));
        productCategoryEntity.setName(updatedProductCategory.getName());
        productCategoryEntity = productCategoriesRepo.save(productCategoryEntity);
        return fromProductCategoryToProductCategoryOutDto(productCategoryEntity);
    }

    // ========
    // readOnly = true methods
    public List<ProductCategoryOutDTO> index() {
        return productCategoriesRepo.findAll().stream().map(this::fromProductCategoryToProductCategoryOutDto).collect(Collectors.toList());
    }

    public Optional<ProductCategoryOutDTO> getByName(String name) {
        return productCategoriesRepo.findByName(name).map(this::fromProductCategoryToProductCategoryOutDto);
    }

    public ProductCategoryOutDTO getByExternalId(String externalId) {
        return productCategoriesRepo.findByExternalId(externalId)
                .map(this::fromProductCategoryToProductCategoryOutDto)
                .orElseThrow(() -> new ProductCategoryNotFoundException(utils.getMessageFromBundle("productcategory.notfound.err")));
    }

    private Optional<ProductCategory> findByExternalId(String externalId) {
        return productCategoriesRepo.findByExternalId(externalId);
    }


    private void enrichProductCategory(ProductCategory productCategory) {
        productCategory.setExternalId(utils.generateExternalId());
    }

    private ProductCategory fromProductCategoryInDtoToProductCategory(ProductCategoryInDTO productCategoryInDto) {
        return modelMapper.map(productCategoryInDto, ProductCategory.class);
    }

    private ProductCategoryOutDTO fromProductCategoryToProductCategoryOutDto(ProductCategory productCategory) {
        return modelMapper.map(productCategory, ProductCategoryOutDTO.class);
    }
}
