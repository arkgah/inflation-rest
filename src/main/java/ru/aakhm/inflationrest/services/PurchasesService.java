package ru.aakhm.inflationrest.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aakhm.inflationrest.dto.in.PurchaseInDTO;
import ru.aakhm.inflationrest.dto.out.PurchaseOutDTO;
import ru.aakhm.inflationrest.models.Product;
import ru.aakhm.inflationrest.models.ProductCategory;
import ru.aakhm.inflationrest.models.Purchase;
import ru.aakhm.inflationrest.models.Store;
import ru.aakhm.inflationrest.models.validation.except.person.PersonNotFoundException;
import ru.aakhm.inflationrest.models.validation.except.product.ProductNotFoundException;
import ru.aakhm.inflationrest.models.validation.except.productcategory.ProductCategoryNotFoundException;
import ru.aakhm.inflationrest.models.validation.except.purchase.PurchaseNotFoundException;
import ru.aakhm.inflationrest.models.validation.except.purchase.PurchaseUpdateNotAllowedException;
import ru.aakhm.inflationrest.models.validation.except.store.StoreNotFoundException;
import ru.aakhm.inflationrest.repo.*;
import ru.aakhm.inflationrest.security.Role;
import ru.aakhm.inflationrest.utils.Utils;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PurchasesService {
    private final PurchasesRepo purchasesRepo;
    private final ProductsRepo productsRepo;
    private final ProductCategoriesRepo productCategoriesRepo;
    private final PeopleRepo peopleRepo;
    private final StoresRepo storesRepo;
    private final Utils utils;
    private final ModelMapper modelMapper;

    @Autowired
    public PurchasesService(PurchasesRepo purchasesRepo, ProductsRepo productsRepo, ProductCategoriesRepo productCategoriesRepo, PeopleRepo peopleRepo, StoresRepo storesRepo, Utils utils, ModelMapper modelMapper) {
        this.purchasesRepo = purchasesRepo;
        this.productsRepo = productsRepo;
        this.productCategoriesRepo = productCategoriesRepo;
        this.peopleRepo = peopleRepo;
        this.storesRepo = storesRepo;
        this.utils = utils;
        this.modelMapper = modelMapper;
    }

    // ========
    // readOnly = false methods
    @Transactional
    public PurchaseOutDTO save(PurchaseInDTO purchaseInDTO) {
        Purchase purchase = fromPurchaseInDTOtoPurchase(purchaseInDTO);
        enrichPurchase(purchase);
        return fromPurchaseToPurchaseOutDTO(purchasesRepo.save(purchase));
    }

    @Transactional
    public PurchaseOutDTO update(String externalId, PurchaseInDTO purchaseInDTO) {
        Purchase purchaseOld = purchasesRepo.findByExternalId(externalId)
                .orElseThrow(() -> new PurchaseNotFoundException(utils.getMessageFromBundle("purchase.notfound.err")));

        Principal principal = SecurityContextHolder.getContext().getAuthentication();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean hasAdminRole = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals(Role.ROLE_ADMIN.name()));

        if (!hasAdminRole && !principal.getName().equals(purchaseOld.getPerson().getLogin())) {
            throw new PurchaseUpdateNotAllowedException(utils.getMessageFromBundle("purchase.edit.notallowed.err"));
        }

        Purchase purchaseNew = fromPurchaseInDTOtoPurchase(purchaseInDTO);
        purchaseNew.setId(purchaseOld.getId());
        purchaseNew.setExternalId(purchaseOld.getExternalId());
        purchaseOld = mapPurchase(purchaseNew);

        return fromPurchaseToPurchaseOutDTO(purchasesRepo.save(purchaseOld));
    }

    // ========
    // readOnly = true methods
    public List<PurchaseOutDTO> index() {
        return purchasesRepo.findAll().stream().map(this::fromPurchaseToPurchaseOutDTO).collect(Collectors.toList());
    }

    // ========
    // utility methods
    private Purchase fromPurchaseInDTOtoPurchase(PurchaseInDTO purchaseInDTO) {
        if (purchaseInDTO.getProduct().getName() == null)
            throw new ProductNotFoundException(utils.getMessageFromBundle("product.name.null.err"));
        if (purchaseInDTO.getProduct().getCategory() == null)
            throw new ProductCategoryNotFoundException(utils.getMessageFromBundle("product.category.null.err"));
        if (purchaseInDTO.getProduct().getCategory().getName() == null)
            throw new ProductCategoryNotFoundException(utils.getMessageFromBundle("productcategory.name.null"));

        Optional<ProductCategory> productCategory = productCategoriesRepo.findByName(
                purchaseInDTO.getProduct().getCategory().getName());

        Optional<Product> product = productsRepo.getProductByNameAndCategory(
                purchaseInDTO.getProduct().getName(),
                productCategory
                        .orElseThrow(
                                () -> new ProductCategoryNotFoundException(
                                        utils.getMessageFromBundle("productcategory.notfound.err"))));
        Optional<Store> store = storesRepo.findByName(purchaseInDTO.getStore().getName());

        Purchase purchase = modelMapper.map(purchaseInDTO, Purchase.class);

        purchase.setUnit(purchaseInDTO.getProduct().getUnit());
        purchase.setProduct(product.orElseThrow(
                () -> new ProductNotFoundException(utils.getMessageFromBundle("product.notfound.err"))));
        purchase.setStore(store.orElseThrow(
                () -> new StoreNotFoundException(utils.getMessageFromBundle("store.notfound.err"))));

        Principal principal = SecurityContextHolder.getContext().getAuthentication();
        purchase.setPerson(
                peopleRepo.findByLogin(principal.getName())
                        .orElseThrow(() -> new PersonNotFoundException(
                                utils.getMessageFromBundle("person.notfound.err"))));

        return purchase;
    }

    private PurchaseOutDTO fromPurchaseToPurchaseOutDTO(Purchase purchase) {
        PurchaseOutDTO res = modelMapper.map(purchase, PurchaseOutDTO.class);
        res.getProduct().setUnit(purchase.getUnit());
        return res;
    }

    private Purchase mapPurchase(Purchase purchase) {
        return modelMapper.map(purchase, Purchase.class);
    }

    private void enrichPurchase(Purchase purchase) {
        purchase.setExternalId(utils.generateExternalId());
    }

}
