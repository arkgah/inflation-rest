package ru.aakhm.inflationrest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.aakhm.inflationrest.dto.in.*;
import ru.aakhm.inflationrest.dto.out.PersonOutDTO;
import ru.aakhm.inflationrest.security.Role;
import ru.aakhm.inflationrest.services.PersonDetailsService;
import ru.aakhm.inflationrest.services.PurchasesService;
import ru.aakhm.inflationrest.services.RegistrationService;
import ru.aakhm.inflationrest.utils.DateTimeUtil;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

// Класс создаёт 2 пользователя с ролями (ROLE_USER, ROLE_ADMIN) для возможности работать с приложением
@Component
public class InitialPeopleAndPurchasesDemoSetup {
    @Value("${people.create.predefined:false}")
    private boolean doCreate;

    private final RegistrationService registrationService;
    private final PersonDetailsService personDetailsService;
    private final PurchasesService purchasesService;

    @Autowired
    public InitialPeopleAndPurchasesDemoSetup(RegistrationService registrationService, PersonDetailsService personDetailsService, PurchasesService purchasesService) {
        this.registrationService = registrationService;
        this.personDetailsService = personDetailsService;
        this.purchasesService = purchasesService;
    }

    // Вызовется автоматически, после загрузки приложения
    @EventListener
    @Transactional
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!doCreate)
            return;
        PersonInDTO personInDTO = new PersonInDTO();
        personInDTO.setLogin("admin");
        personInDTO.setPassword("12345678");
        personInDTO.setFirstName("Admin");
        personInDTO.setLastName("Admin");
        PersonOutDTO registeredUser = registrationService.register(personInDTO);

        personDetailsService.assignRole(registeredUser.getExternalId(), Role.ROLE_ADMIN.name());

        personInDTO.setLogin("user");
        personInDTO.setFirstName("User");
        personInDTO.setLastName("User");
        registeredUser = registrationService.register(personInDTO);

        personDetailsService.assignRole(registeredUser.getExternalId(), Role.ROLE_USER.name());

        savePurchase("admin", 2022, 1, 15, 51.23, "Кефир", 1., "Продукты", "Лента");
        savePurchase("user", 2022, 3, 23, 61.23, "Кефир", 1., "Продукты", "Окей");
    }

    private void savePurchase(String login,
                              int year, int month, int dayOfMonth,
                              double price, String productName, double unit, String categoryName, String storeName) {
        PurchaseInDTO purchase = new PurchaseInDTO() {
            {
                setPurchasedAt(DateTimeUtil.dateFromLocalDate(LocalDate.of(year, month, dayOfMonth)));
                setPrice(BigDecimal.valueOf(price));
                setProduct(new ProductInDTO() {
                    {
                        setName(productName);
                        setUnit(unit);
                        setCategory(new ProductCategoryInDTO() {
                            {
                                setName(categoryName);
                            }
                        });
                    }
                });
                setStore(new StoreInDTO() {
                    {
                        setName(storeName);
                    }
                });
            }
        };

        purchasesService.saveForPerson(purchase, login);
    }
}
