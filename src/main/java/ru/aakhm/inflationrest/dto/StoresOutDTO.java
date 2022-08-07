package ru.aakhm.inflationrest.dto;

import lombok.Data;

import java.util.List;

@Data
public class StoresOutDTO {
    private List<StoreOutDTO> stores;
}
