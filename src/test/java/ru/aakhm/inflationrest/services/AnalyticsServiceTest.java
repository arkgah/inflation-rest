package ru.aakhm.inflationrest.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.aakhm.inflationrest.dto.out.CPIOutDTO;
import ru.aakhm.inflationrest.models.Product;
import ru.aakhm.inflationrest.models.ProductCategory;
import ru.aakhm.inflationrest.models.Purchase;
import ru.aakhm.inflationrest.models.validation.except.analytics.CPICannotCalculateException;
import ru.aakhm.inflationrest.repo.PurchasesRepo;
import ru.aakhm.inflationrest.utils.Utils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AnalyticsServiceTest {
    @InjectMocks
    private AnalyticsService analyticsService;

    @Mock
    private PurchasesRepo purchasesRepo;

    @Mock
    private Utils utils;

    private final Date date1 = new Date();
    private final Date date2 = new Date();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCPI() {
        when(purchasesRepo.getAllByPurchasedAtBetween(any(Date.class), any(Date.class))).thenReturn(listOfPurchases());
        CPIOutDTO res = assertDoesNotThrow(() -> analyticsService.getCPI(date1, date2));
        assertEquals(0.0, res.getCpi());

        when(purchasesRepo.getAllByPurchasedAtBetween(any(Date.class), any(Date.class))).thenReturn(List.of());
        assertThrows(CPICannotCalculateException.class, () -> analyticsService.getCPI(date1, date2));
    }

    private List<Purchase> listOfPurchases() {
        return List.of(
                new Purchase() {
                    {
                        setProduct(new Product() {
                            {
                                setCategory(
                                        new ProductCategory() {
                                            {
                                                setId(1);
                                            }
                                        }
                                );
                                setUnit(1.0);
                            }
                        });

                        setPrice(BigDecimal.valueOf(1.23));

                        setUnit(1.0);

                        setPurchasedAt(date1);
                    }
                }
        );
    }

}