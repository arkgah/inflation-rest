package ru.aakhm.inflationrest.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aakhm.inflationrest.dto.out.CPIOutDTO;
import ru.aakhm.inflationrest.models.Purchase;
import ru.aakhm.inflationrest.models.validation.except.analytics.CPICannotCalculateException;
import ru.aakhm.inflationrest.repo.PurchasesRepo;
import ru.aakhm.inflationrest.utils.DateTimeUtil;
import ru.aakhm.inflationrest.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
public class AnalyticsService {
    private final PurchasesRepo purchasesRepo;
    private final Utils utils;

    @Autowired
    public AnalyticsService(PurchasesRepo purchasesRepo, Utils utils) {
        this.purchasesRepo = purchasesRepo;
        this.utils = utils;
    }

    // ========
    // readOnly = true methods
    public CPIOutDTO getCPI(Date beginDate, Date endDate) {
        DateTimeUtil.TwoDateTimeEvents events = DateTimeUtil.TwoDateTimeEvents.of(
                LocalDate.ofInstant(beginDate.toInstant(), ZoneId.systemDefault()),
                LocalDate.ofInstant(endDate.toInstant(), ZoneId.systemDefault()));

        List<Purchase> purchaseList1 =
                purchasesRepo.getAllByPurchasedAtBetween(
                        Date.from(events.getLocalDateTime11().atZone(ZoneId.systemDefault()).toInstant()),
                        Date.from(events.getLocalDateTime12().atZone(ZoneId.systemDefault()).toInstant()));

        List<Purchase> purchaseList2 =
                purchasesRepo.getAllByPurchasedAtBetween(
                        Date.from(events.getLocalDateTime21().atZone(ZoneId.systemDefault()).toInstant()),
                        Date.from(events.getLocalDateTime22().atZone(ZoneId.systemDefault()).toInstant()));

        OptionalDouble cpi = getCpi(purchaseList1, purchaseList2, false);

        CPIOutDTO res = new CPIOutDTO();
        res.setBeginDate(beginDate);
        res.setEndDate(endDate);
        res.setCpi(cpi.orElseThrow(() -> new CPICannotCalculateException(utils.getMessageFromBundle("cpi.cannotcalculate.err"))));

        return res;
    }

    // ========
    // utility methods
    private OptionalDouble getCpi(List<Purchase> purchaseList1, List<Purchase> purchaseList2, boolean isExactCarts) {
        Map<LocalDate, Map<Integer, Double>> byMonthMap1 = getMonthlyPurchasesMap(purchaseList1);
        Map<LocalDate, Map<Integer, Double>> byMonthMap2 = getMonthlyPurchasesMap(purchaseList2);

        Set<Integer> products1 = byMonthMap1.values().stream().map(Map::keySet).flatMap(Set::stream).collect(Collectors.toSet());
        Set<Integer> products2 = byMonthMap2.values().stream().map(Map::keySet).flatMap(Set::stream).collect(Collectors.toSet());
        Set<Integer> productsIntersect = products1.stream().filter(products2::contains).collect(Collectors.toSet());

        log.debug("products1={}", products1);
        log.debug("products2={}", products2);
        log.debug("productsIntersect={}", productsIntersect);

        OptionalInt countProductsCart1 = byMonthMap1.values().stream().mapToInt(m -> m.keySet().size()).findFirst();
        OptionalInt countProductsCart2 = byMonthMap2.values().stream().mapToInt(m -> m.keySet().size()).findFirst();
        if (countProductsCart1.isEmpty() || countProductsCart2.isEmpty()) {
            return OptionalDouble.empty();
        }

        if (isExactCarts) {
            // проверяем, что корзины полные для двух списков
            if (countProductsCart1.getAsInt() != countProductsCart2.getAsInt())
                return OptionalDouble.empty();
        }

        byMonthMap1.values().stream().findFirst()
                .orElseThrow(() -> new CPICannotCalculateException(utils.getMessageFromBundle("cpi.cannotcalculate.err")))
                .keySet().retainAll(productsIntersect);
        byMonthMap2.values().stream().findFirst()
                .orElseThrow(() -> new CPICannotCalculateException(utils.getMessageFromBundle("cpi.cannotcalculate.err")))
                .keySet().retainAll(productsIntersect);


        double cartPrice1 = byMonthMap1.values().stream().findFirst()
                .orElseThrow(() -> new CPICannotCalculateException(utils.getMessageFromBundle("cpi.cannotcalculate.err")))
                .values().stream().mapToDouble(Double::doubleValue).sum();
        double cartPrice2 = byMonthMap2.values().stream().findFirst()
                .orElseThrow(() -> new CPICannotCalculateException(utils.getMessageFromBundle("cpi.cannotcalculate.err")))
                .values().stream().mapToDouble(Double::doubleValue).sum();

        double cpi = 100. * (cartPrice2 - cartPrice1) / cartPrice2;
        // округляем до одной цифры после запятой
        return OptionalDouble.of(BigDecimal.valueOf(cpi).setScale(1, RoundingMode.HALF_UP).doubleValue());
    }

    private Map<LocalDate, Map<Integer, Double>> getMonthlyPurchasesMap(List<Purchase> purchaseList) {
        return purchaseList.stream()
                .peek(p -> {
                    BigDecimal normalizedPrice = p.getPrice().multiply(BigDecimal.valueOf(p.getProduct().getUnit() / p.getUnit()));
                    p.setPrice(normalizedPrice);
                })
                .collect(Collectors.groupingBy(p -> LocalDate.ofInstant(p.getPurchasedAt().toInstant(), ZoneId.systemDefault()).with(TemporalAdjusters.firstDayOfMonth()),
                        Collectors.groupingBy(p -> p.getProduct().getId(), Collectors.averagingDouble(p -> p.getPrice().doubleValue()))));
    }
}
