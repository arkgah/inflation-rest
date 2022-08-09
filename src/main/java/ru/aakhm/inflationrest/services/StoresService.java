package ru.aakhm.inflationrest.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aakhm.inflationrest.dto.in.StoreInDTO;
import ru.aakhm.inflationrest.dto.out.StoreOutDTO;
import ru.aakhm.inflationrest.models.Store;
import ru.aakhm.inflationrest.models.validation.except.store.StoreNotFoundException;
import ru.aakhm.inflationrest.models.validation.except.store.StoreNotUpdatedException;
import ru.aakhm.inflationrest.repo.StoresRepo;
import ru.aakhm.inflationrest.utils.Utils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StoresService {
    private final StoresRepo storesRepo;
    private final Utils utils;
    private final ModelMapper modelMapper;

    @Autowired
    public StoresService(StoresRepo storesRepo, Utils utils, ModelMapper modelMapper) {
        this.storesRepo = storesRepo;
        this.utils = utils;
        this.modelMapper = modelMapper;
    }

    // ========
    // readOnly = false methods
    @Transactional
    public StoreOutDTO save(StoreInDTO storeInDTO) {
        Store store = fromStoreInDtoToStore(storeInDTO);
        enrichStore(store);
        storesRepo.save(store);
        return fromStoreToStoreOutDTO(store);
    }

    @Transactional
    public void deleteByExternalId(String externalId) {
        Store store = findByExternalId(externalId)
                .orElseThrow(
                        () -> new StoreNotFoundException(utils.getMessageFromBundle("store.notfound.err")));

        storesRepo.deleteById(store.getId());
    }

    @Transactional
    public StoreOutDTO update(String externalId, StoreInDTO updatedStore) {
        Store storeEntity = findByExternalId(externalId)
                .orElseThrow(
                        () -> new StoreNotUpdatedException(utils.getMessageFromBundle("store.notfound.err")));
        storeEntity.setName(updatedStore.getName());
        storeEntity = storesRepo.save(storeEntity);
        return fromStoreToStoreOutDTO(storeEntity);
    }

    // readOnly = true methods
    public List<StoreOutDTO> index() {
        return storesRepo.findAll().stream().map(this::fromStoreToStoreOutDTO).collect(Collectors.toList());
    }

    public Optional<StoreOutDTO> getByName(String name) {
        return storesRepo.findByName(name).map(this::fromStoreToStoreOutDTO);
    }

    public StoreOutDTO getByExternalId(String externalId) {
        return storesRepo.findByExternalId(externalId)
                .map(this::fromStoreToStoreOutDTO)
                .orElseThrow(() -> new StoreNotUpdatedException(utils.getMessageFromBundle("store.notfound.err")));
    }

    private Optional<Store> findByExternalId(String externalId) {
        return storesRepo.findByExternalId(externalId);
    }


    private void enrichStore(Store store) {
        store.setExternalId(utils.generateStoreExternalId());
    }

    private Store fromStoreInDtoToStore(StoreInDTO storeInDTO) {
        return modelMapper.map(storeInDTO, Store.class);
    }

    private StoreOutDTO fromStoreToStoreOutDTO(Store store) {
        return modelMapper.map(store, StoreOutDTO.class);
    }

}
