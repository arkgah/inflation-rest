package ru.aakhm.inflationrest.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aakhm.inflationrest.models.Store;
import ru.aakhm.inflationrest.repo.StoresRepo;
import ru.aakhm.inflationrest.utils.Utils;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class StoresService {
    private final StoresRepo storesRepo;
    private final Utils utils;

    @Autowired
    public StoresService(StoresRepo storesRepo, Utils utils) {
        this.storesRepo = storesRepo;
        this.utils = utils;
    }

    @Transactional
    public Store save(Store store) {
        enrichStore(store);
        storesRepo.save(store);
        return store;
    }

    public Optional<Store> getByName(String name) {
        return storesRepo.findByName(name);
    }

    private void enrichStore(Store store) {
        store.setExternalId(utils.generateStoreExternalId());
    }
}
