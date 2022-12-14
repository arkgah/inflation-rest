package ru.aakhm.inflationrest.services;

import ru.aakhm.inflationrest.dto.out.ProductOutDTO;

import java.util.List;
import java.util.Optional;

public interface ExternalIdAndNameWithCategoryService<InDTO, OutDTO> extends ExternalIdService<InDTO, OutDTO> {
    // =======
    // readOnly = true methods
    Optional<ProductOutDTO> getByNameAndCategoryName(String name, String categoryName);

    List<OutDTO> index(Integer page, Integer itemsPerPage, String nameLike, String categoryNameLike);
}
