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
    List<OutDTO> index(Integer page, Integer itemsPerPage);

    default List<OutDTO> index(Integer page, Integer itemsPerPage, Boolean sortAsc) {
        return index(page, itemsPerPage);
    }

    OutDTO getByExternalId(String externalId);
}
