package ru.aakhm.inflationrest.dto.out;

import lombok.Data;

import java.util.List;

@Data
public class PeopleOutDTO {
    private List<PersonOutDTO> people;
}
