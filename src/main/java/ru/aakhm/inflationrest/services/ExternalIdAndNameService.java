package ru.aakhm.inflationrest.services;

import java.util.Optional;

public interface ExternalIdAndNameService<InDTO, OutDTO> extends ExternalIdService<InDTO, OutDTO> {
    // ========
    // readOnly = true methods
    Optional<OutDTO> getByName(String name);
}
