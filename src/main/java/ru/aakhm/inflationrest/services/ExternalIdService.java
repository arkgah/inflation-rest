package ru.aakhm.inflationrest.services;

import java.util.List;

public interface ExternalIdService<InDTO, OutDTO> {
    // ========
    // readOnly = false methods
    // Реализация следующих методов должна быть помечена @Transactional
    OutDTO save(InDTO inDTOToSave);

    void deleteByExternalId(String externalId);

    OutDTO update(String externalId, InDTO inDTOToBeUpdated);

    // ========
    // readOnly = true methods
    // Реализацию следующих методов не нужно помечать @Transactional
    List<OutDTO> index();

    OutDTO getByExternalId(String externalId);
}
