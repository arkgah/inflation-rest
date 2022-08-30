package ru.aakhm.inflationrest.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.aakhm.inflationrest.dto.in.ProductCategoryInDTO;
import ru.aakhm.inflationrest.dto.in.ProductInDTO;
import ru.aakhm.inflationrest.dto.in.PurchaseInDTO;
import ru.aakhm.inflationrest.dto.in.StoreInDTO;
import ru.aakhm.inflationrest.dto.out.PersonOutDTO;
import ru.aakhm.inflationrest.dto.out.ProductOutDTO;
import ru.aakhm.inflationrest.dto.out.PurchaseOutDTO;
import ru.aakhm.inflationrest.dto.out.StoreOutDTO;
import ru.aakhm.inflationrest.models.*;
import ru.aakhm.inflationrest.models.validation.except.person.PersonNotFoundException;
import ru.aakhm.inflationrest.models.validation.except.purchase.PurchaseDeleteNotAllowedException;
import ru.aakhm.inflationrest.models.validation.except.purchase.PurchaseNotFoundException;
import ru.aakhm.inflationrest.models.validation.except.purchase.PurchaseUpdateNotAllowedException;
import ru.aakhm.inflationrest.repo.*;
import ru.aakhm.inflationrest.security.Role;
import ru.aakhm.inflationrest.utils.Utils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PurchasesServiceTest {
    @InjectMocks
    private PurchasesService purchasesService;

    @Mock
    private PurchasesRepo purchasesRepo;

    @Mock
    private ProductCategoriesRepo productCategoriesRepo;

    @Mock
    private ProductsRepo productsRepo;

    @Mock
    private StoresRepo storesRepo;

    @Mock
    private PeopleRepo peopleRepo;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private Utils utils;

    private static final String PURCHASE_EXTERNAL_ID = "prch987";
    private static final BigDecimal PURCHASE_PRICE = BigDecimal.valueOf(123.45);
    private static final Date PURCHASE_DATE = new Date();
    private static final String PURCHASE_MEMO = "Test memo";
    private static final Double PURCHASE_PRODUCT_UNIT = 0.5;

    private static final String PRODUCT_EXTERNAL_ID = "123abc";
    private static final String PRODUCT_NAME = "Test Product";
    private static final Double PRODUCT_UNIT = 1.5;

    private static final String PRODUCT_CAT_NAME = "Test Category";
    private static final String PRODUCT_CAT_EXTERNAL_ID = "456def";

    private static final String STORE_NAME = "Test store";

    private static final String PURCHASE_ADMIN_LOGIN = "admin";
    private static final String PURCHASE_USER_LOGIN = "user";

    private PurchaseInDTO purchaseInDTO;
    private ProductInDTO productInDTO;
    private ProductCategoryInDTO pcInDTO;
    private StoreInDTO storeInDTO;

    private Product product;
    private ProductCategory productCategory;
    private Store store;

    private Purchase purchase;
    private Person personAdmin;
    private Person personUser;


    private PurchaseOutDTO purchaseOutDTO;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        initData();
    }

    private void initData() {
        pcInDTO = new ProductCategoryInDTO() {
            {
                setName(PRODUCT_CAT_NAME);
            }
        };

        productInDTO = new ProductInDTO() {
            {
                setName(PRODUCT_NAME);
                setUnit(PURCHASE_PRODUCT_UNIT);
                setCategory(pcInDTO);
            }
        };

        storeInDTO = new StoreInDTO() {
            {
                setName(STORE_NAME);
            }
        };

        purchaseInDTO = new PurchaseInDTO() {
            {
                setPrice(PURCHASE_PRICE);
                setPurchasedAt(PURCHASE_DATE);
                setMemo(PURCHASE_MEMO);
                setProduct(productInDTO);
                setStore(storeInDTO);
            }
        };

        purchaseOutDTO = new PurchaseOutDTO() {
            {
                setExternalId(PURCHASE_EXTERNAL_ID);
                setPrice(PURCHASE_PRICE);
                setPurchasedAt(PURCHASE_DATE);
                setMemo(PURCHASE_MEMO);

                setProduct(new ProductOutDTO());
                setStore(new StoreOutDTO());
                setPerson(new PersonOutDTO());
            }
        };

        personAdmin = new Person() {
            {
                setLogin(PURCHASE_ADMIN_LOGIN);
            }
        };

        personUser = new Person() {
            {
                setLogin(PURCHASE_USER_LOGIN);
            }
        };

        productCategory = new ProductCategory() {
            {
                setName(PRODUCT_CAT_NAME);
            }
        };

        product = new Product() {
            {
                setName(PRODUCT_NAME);
                setCategory(productCategory);
                setUnit(1.);
            }
        };

        store = new Store() {
            {
                setName(STORE_NAME);
            }
        };

        purchase = new Purchase() {
            {
                setProduct(product);
                setStore(store);
                setPerson(new Person() {
                    {
                        setLogin(PURCHASE_ADMIN_LOGIN);
                    }
                });
            }
        };


    }

    @Test
    void save() {
        prepareMocksForSave();

        PurchaseOutDTO resPurchase = purchasesService.save(purchaseInDTO);
        verify(purchasesRepo, times(1)).save(purchase);

        checkSaveResult(resPurchase);
    }

    @Test
    void saveForPerson() {
        prepareMocksForSave();

        PurchaseOutDTO resPurchase = purchasesService.saveForPerson(purchaseInDTO, PURCHASE_ADMIN_LOGIN);
        verify(purchasesRepo, times(1)).save(purchase);
        checkSaveResult(resPurchase);

        reset(purchasesRepo);
        when(peopleRepo.getByLogin(any(String.class))).thenReturn(Optional.empty());
        assertThrows(PersonNotFoundException.class, () -> purchasesService.saveForPerson(purchaseInDTO, null));
        verify(purchasesRepo, times(0)).save(purchase);
    }

    private void checkSaveResult(PurchaseOutDTO resPurchase) {
        assertNotNull(resPurchase);
        assertNotNull(resPurchase.getProduct());
        assertNotNull(resPurchase.getStore());
        assertNotNull(resPurchase.getPerson());
        assertNotNull(resPurchase.getExternalId());
        assertNotNull(resPurchase.getPurchasedAt());
        assertNotNull(resPurchase.getPrice());
        assertEquals(PURCHASE_EXTERNAL_ID, resPurchase.getExternalId());
        assertEquals(PURCHASE_DATE, resPurchase.getPurchasedAt());
        assertEquals(PURCHASE_PRICE, resPurchase.getPrice());
        assertEquals(PURCHASE_MEMO, resPurchase.getMemo());
    }

    private void prepareMocksForSave() {
        when(purchasesRepo.save(any(Purchase.class))).thenReturn(purchase);
        when(productCategoriesRepo.getByName(PRODUCT_CAT_NAME)).thenReturn(Optional.of(productCategory));
        when(productsRepo.getProductByNameAndCategory(any(String.class), any(ProductCategory.class))).thenReturn(Optional.of(product));
        when(peopleRepo.getByLogin(PURCHASE_ADMIN_LOGIN)).thenReturn(Optional.of(personAdmin));
        when(peopleRepo.getByLogin(PURCHASE_USER_LOGIN)).thenReturn(Optional.of(personUser));


        when(storesRepo.getByName(STORE_NAME)).thenReturn(Optional.of(store));

        when(modelMapper.map(any(PurchaseInDTO.class), any())).thenReturn(purchase);
        when(modelMapper.map(any(Purchase.class), any())).thenReturn(purchaseOutDTO);

        when(utils.generateExternalId()).thenReturn(PURCHASE_EXTERNAL_ID);
    }

    @Test
    void update() {
        // purchase существует
        prepareMocksForSave();
        when(modelMapper.map(any(Purchase.class), eq(Purchase.class))).thenReturn(purchase);
        when(purchasesRepo.getByExternalId(PURCHASE_EXTERNAL_ID)).thenReturn(Optional.of(purchase));

        // https://bjoernkw.com/2022/02/20/minimal-spring-security-context-setup-for-unit-testing/
        SecurityContextHolder.setContext(
                SecurityContextHolder.createEmptyContext()
        );
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(PURCHASE_ADMIN_LOGIN, "password", Role.ROLE_ADMIN.name())
        );

        PurchaseOutDTO resPurchase = purchasesService.update(PURCHASE_EXTERNAL_ID, purchaseInDTO);
        verify(purchasesRepo, times(1)).save(purchase);
        checkSaveResult(resPurchase);

        // purchase не существует
        reset(purchasesRepo);
        when(purchasesRepo.getByExternalId(PURCHASE_EXTERNAL_ID)).thenReturn(Optional.empty());
        assertThrows(PurchaseNotFoundException.class, () -> purchasesService.update(PURCHASE_EXTERNAL_ID, purchaseInDTO));
        verify(purchasesRepo, times(0)).save(purchase);

        // update не разрешено
        reset(purchasesRepo);
        when(purchasesRepo.getByExternalId(PURCHASE_EXTERNAL_ID)).thenReturn(Optional.of(purchase));
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(PURCHASE_USER_LOGIN, "password", Role.ROLE_USER.name())
        );
        assertThrows(PurchaseUpdateNotAllowedException.class, () -> purchasesService.update(PURCHASE_EXTERNAL_ID, purchaseInDTO));
        verify(purchasesRepo, times(0)).save(purchase);

    }

    @Test
    void isPresentByPurchasedAtAndProductAndStoreAndPerson() {
        // purchase не существует
        when(purchasesRepo.getByPurchasedAtAndProductAndStoreAndPerson(any(), any(), any(), any())).thenReturn(Optional.empty());
        when(productCategoriesRepo.getByName(any())).thenReturn(Optional.of(productCategory));
        when(productsRepo.getProductByNameAndCategory(any(), any())).thenReturn(Optional.of(product));
        when(storesRepo.getByName(any())).thenReturn(Optional.of(store));
        when(peopleRepo.getByLogin(any())).thenReturn(Optional.of(personUser));

        assertFalse(purchasesService.isPresentByPurchasedAtAndProductAndStoreAndPerson(PURCHASE_DATE, PRODUCT_NAME, PRODUCT_CAT_NAME, STORE_NAME));
        verify(purchasesRepo, times(1)).getByPurchasedAtAndProductAndStoreAndPerson(any(), any(), any(), any());

        // purchase существует
        reset(purchasesRepo);
        when(purchasesRepo.getByPurchasedAtAndProductAndStoreAndPerson(any(), any(), any(), any())).thenReturn(Optional.of(purchase));
        assertTrue(purchasesService.isPresentByPurchasedAtAndProductAndStoreAndPerson(PURCHASE_DATE, PRODUCT_NAME, PRODUCT_CAT_NAME, STORE_NAME));
        verify(purchasesRepo, times(1)).getByPurchasedAtAndProductAndStoreAndPerson(any(), any(), any(), any());
    }

    @Test
    void deleteByExternalId() {
        // purchase существует
        when(purchasesRepo.getByExternalId(PURCHASE_EXTERNAL_ID)).thenReturn(Optional.of(purchase));

        //  as admin
        SecurityContextHolder.setContext(
                SecurityContextHolder.createEmptyContext()
        );
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(PURCHASE_ADMIN_LOGIN, "password", Role.ROLE_ADMIN.name())
        );
        assertDoesNotThrow(() -> purchasesService.deleteByExternalId(PURCHASE_EXTERNAL_ID));
        verify(purchasesRepo, times(1)).delete(purchase);
        //  as user
        reset(purchasesRepo);
        when(purchasesRepo.getByExternalId(PURCHASE_EXTERNAL_ID)).thenReturn(Optional.of(purchase));
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(PURCHASE_USER_LOGIN, "password", Role.ROLE_USER.name())
        );
        assertThrows(PurchaseDeleteNotAllowedException.class, () -> purchasesService.deleteByExternalId(PURCHASE_EXTERNAL_ID));
        verify(purchasesRepo, times(0)).delete(purchase);

        // purchase не существует
        reset(purchasesRepo);
        when(purchasesRepo.getByExternalId(PURCHASE_EXTERNAL_ID)).thenReturn(Optional.empty());
        assertThrows(PurchaseNotFoundException.class, () -> purchasesService.deleteByExternalId(PURCHASE_EXTERNAL_ID));
        verify(purchasesRepo, times(0)).delete(purchase);
    }

    @Test
    void index() {
        when(purchasesRepo.findAll(any(Pageable.class))).thenReturn(repoIndex());
        when(modelMapper.map(any(Purchase.class), any())).thenReturn(purchaseOutDTO);

        List<PurchaseOutDTO> resPurchases = purchasesService.index(0, 10);
        assertNotNull(resPurchases);
        assertEquals(repoIndex().getContent().size(), resPurchases.size());
        verify(purchasesRepo, times(1)).findAll(any(Pageable.class));
        verify(purchasesRepo, times(0)).findAll();
    }

    @Test
    void getByExternalId() {
        // purchase существует
        when(purchasesRepo.getByExternalId(PURCHASE_EXTERNAL_ID)).thenReturn(Optional.of(purchase));
        when(modelMapper.map(any(Purchase.class), any())).thenReturn(purchaseOutDTO);

        PurchaseOutDTO resPurchase = purchasesService.getByExternalId(PURCHASE_EXTERNAL_ID);
        verify(purchasesRepo, times(1)).getByExternalId(any(String.class));
        assertNotNull(resPurchase);
        assertEquals(PURCHASE_EXTERNAL_ID, resPurchase.getExternalId());

        // purchase не существует
        reset(purchasesRepo);
        when(purchasesRepo.getByExternalId(PURCHASE_EXTERNAL_ID)).thenReturn(Optional.empty());
        assertThrows(PurchaseNotFoundException.class, () -> purchasesService.getByExternalId(PURCHASE_EXTERNAL_ID));
    }

    private Page<Purchase> repoIndex() {
        return new PageImpl<>(List.of(
                purchase
        ));
    }
}