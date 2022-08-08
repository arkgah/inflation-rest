package ru.aakhm.inflationrest.dto;

import lombok.Data;

import java.util.List;

@Data
public class PeopleOutDTO {
    private List<PersonOutDTO> people;
}
