package ru.aakhm.inflationrest.dto.out;

import lombok.Data;

import java.util.List;

@Data
public class PurchasesOutDTO {
    private List<PurchaseOutDTO> purchases;
}
