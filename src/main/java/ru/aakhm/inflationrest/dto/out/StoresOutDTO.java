package ru.aakhm.inflationrest.dto.out;

import lombok.Data;

import java.util.List;

@Data
public class StoresOutDTO {
    private List<StoreOutDTO> stores;
}
