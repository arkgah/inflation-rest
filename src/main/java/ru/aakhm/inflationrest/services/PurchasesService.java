package ru.aakhm.inflationrest.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aakhm.inflationrest.dto.in.PurchaseInDTO;
import ru.aakhm.inflationrest.dto.out.PurchaseOutDTO;
import ru.aakhm.inflationrest.models.*;
import ru.aakhm.inflationrest.models.validation.except.person.PersonNotFoundException;
import ru.aakhm.inflationrest.models.validation.except.product.ProductNotFoundException;
import ru.aakhm.inflationrest.models.validation.except.productcategory.ProductCategoryNotFoundException;
import ru.aakhm.inflationrest.models.validation.except.purchase.PurchaseDeleteNotAllowedException;
import ru.aakhm.inflationrest.models.validation.except.purchase.PurchaseNotFoundException;
import ru.aakhm.inflationrest.models.validation.except.purchase.PurchaseUpdateNotAllowedException;
import ru.aakhm.inflationrest.models.validation.except.store.StoreNotFoundException;
import ru.aakhm.inflationrest.repo.*;
import ru.aakhm.inflationrest.security.Role;
import ru.aakhm.inflationrest.utils.Utils;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PurchasesService implements ExternalIdService<PurchaseInDTO, PurchaseOutDTO> {
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
    @Override
    @Transactional
    public PurchaseOutDTO save(PurchaseInDTO purchaseInDTO) {
        Purchase purchase = fromPurchaseInDTOtoPurchase(purchaseInDTO);
        enrichPurchase(purchase);
        return fromPurchaseToPurchaseOutDTO(purchasesRepo.save(purchase));
    }

    // Вспомогательный метод, необходим для внутреннего создания Purchase
    @Transactional
    public PurchaseOutDTO saveForPerson(PurchaseInDTO purchaseInDTO, String login) {
        Purchase purchase = fromPurchaseInDTOtoPurchase(purchaseInDTO);
        enrichPurchase(purchase);
        purchase.setPerson(peopleRepo.getByLogin(login)
                .orElseThrow(() -> new PersonNotFoundException(
                        utils.getMessageFromBundle("person.notfound.err"))));
        return fromPurchaseToPurchaseOutDTO(purchasesRepo.save(purchase));
    }

    @Override
    @Transactional
    public PurchaseOutDTO update(String externalId, PurchaseInDTO purchaseInDTO) {
        Purchase purchaseOld = purchasesRepo.getByExternalId(externalId)
                .orElseThrow(() -> new PurchaseNotFoundException(utils.getMessageFromBundle("purchase.notfound.err")));

        if (!userCanModifyPurchase(purchaseOld)) {
            throw new PurchaseUpdateNotAllowedException(utils.getMessageFromBundle("purchase.edit.notallowed.err"));
        }

        Purchase purchaseNew = fromPurchaseInDTOtoPurchase(purchaseInDTO);
        purchaseNew.setId(purchaseOld.getId());
        purchaseNew.setExternalId(purchaseOld.getExternalId());
        purchaseOld = mapPurchase(purchaseNew);

        return fromPurchaseToPurchaseOutDTO(purchasesRepo.save(purchaseOld));
    }

    @Override
    @Transactional
    public void deleteByExternalId(String externalId) {
        Purchase purchaseOld = purchasesRepo.getByExternalId(externalId)
                .orElseThrow(() -> new PurchaseNotFoundException(utils.getMessageFromBundle("purchase.notfound.err")));

        if (!userCanModifyPurchase(purchaseOld)) {
            throw new PurchaseDeleteNotAllowedException(utils.getMessageFromBundle("purchase.delete.notallowed.err"));
        }

        purchasesRepo.delete(purchaseOld);
    }

    // ========
    // readOnly = true methods
    @Override
    public List<PurchaseOutDTO> index(Integer page, Integer perPage) {
        return purchasesRepo.findAll(PageRequest.of(page != null ? page : 0, perPage, Sort.by("purchasedAt"))).getContent()
                .stream().map(this::fromPurchaseToPurchaseOutDTO).collect(Collectors.toList());
    }

    @Override
    public PurchaseOutDTO getByExternalId(String externalId) {
        return purchasesRepo.getByExternalId(externalId).map(this::fromPurchaseToPurchaseOutDTO)
                .orElseThrow(() -> new PurchaseNotFoundException(utils.getMessageFromBundle("purchase.notfound.err")));
    }

    public boolean isPresentByPurchasedAtAndProductAndStoreAndPerson(Date date, String productName, String productCatName, String storeName) {
        Optional<ProductCategory> pc = productCategoriesRepo.getByName(productCatName);
        Optional<Product> product = productsRepo.getProductByNameAndCategory(productName, pc.orElseThrow(() -> new ProductCategoryNotFoundException(
                utils.getMessageFromBundle("productcategory.notfound.err"))));
        Optional<Store> store = storesRepo.getByName(storeName);
        Person person = null;

        Principal principal = SecurityContextHolder.getContext().getAuthentication();

        if (principal != null) {
            person =
                    peopleRepo.getByLogin(principal.getName())
                            .orElseThrow(() -> new PersonNotFoundException(
                                    utils.getMessageFromBundle("person.notfound.err")));
        }

        Optional<Purchase> presentedPurchase = purchasesRepo.getByPurchasedAtAndProductAndStoreAndPerson(
                date,
                product.orElseThrow(() -> new ProductNotFoundException(utils.getMessageFromBundle("product.notfound.err"))),
                store.orElseThrow(() -> new StoreNotFoundException(utils.getMessageFromBundle("store.notfound.err"))),
                person);

        return presentedPurchase.isPresent();
    }

    // ========
    // utility methods
    private Purchase fromPurchaseInDTOtoPurchase(PurchaseInDTO purchaseInDTO) {
        Optional<ProductCategory> productCategory = productCategoriesRepo.getByName(
                purchaseInDTO.getProduct().getCategory().getName());

        Optional<Product> product = productsRepo.getProductByNameAndCategory(
                purchaseInDTO.getProduct().getName(),
                productCategory
                        .orElseThrow(
                                () -> new ProductCategoryNotFoundException(
                                        utils.getMessageFromBundle("productcategory.notfound.err"))));
        Optional<Store> store = storesRepo.getByName(purchaseInDTO.getStore().getName());

        Purchase purchase = modelMapper.map(purchaseInDTO, Purchase.class);

        purchase.setUnit(purchaseInDTO.getProduct().getUnit());
        purchase.setProduct(product.orElseThrow(
                () -> new ProductNotFoundException(utils.getMessageFromBundle("product.notfound.err"))));
        purchase.setStore(store.orElseThrow(
                () -> new StoreNotFoundException(utils.getMessageFromBundle("store.notfound.err"))));

        Principal principal = SecurityContextHolder.getContext().getAuthentication();
        // При инициализации DB principal == null
        if (principal != null) {
            purchase.setPerson(
                    peopleRepo.getByLogin(principal.getName())
                            .orElseThrow(() -> new PersonNotFoundException(
                                    utils.getMessageFromBundle("person.notfound.err"))));
        }
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

    // Разрешаем модификацию (delete, update) только админами, или если покупка сделана данным пользователем
    private boolean userCanModifyPurchase(Purchase purchase) {
        Principal principal = SecurityContextHolder.getContext().getAuthentication();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean hasAdminRole = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals(Role.ROLE_ADMIN.name()));

        return hasAdminRole || principal.getName().equals(purchase.getPerson().getLogin());
    }
}
