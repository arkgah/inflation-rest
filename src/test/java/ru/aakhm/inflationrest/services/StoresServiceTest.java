package ru.aakhm.inflationrest.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.aakhm.inflationrest.dto.in.StoreInDTO;
import ru.aakhm.inflationrest.dto.out.StoreOutDTO;
import ru.aakhm.inflationrest.models.Store;
import ru.aakhm.inflationrest.models.validation.except.store.StoreNotFoundException;
import ru.aakhm.inflationrest.repo.StoresRepo;
import ru.aakhm.inflationrest.utils.Utils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoresServiceTest {
    @InjectMocks
    private StoresService storesService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private Utils utils;

    @Mock
    private StoresRepo storesRepo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save() {
        final String EXTERNAL_ID = "123abc";
        StoreInDTO storeInDTO = new StoreInDTO();
        storeInDTO.setName("Test");

        Store store = new Store();
        store.setName(storeInDTO.getName());

        StoreOutDTO storeOutDTO = new StoreOutDTO();
        storeOutDTO.setName(storeInDTO.getName());
        storeOutDTO.setExternalId(EXTERNAL_ID);

        when(storesRepo.getByName(storeInDTO.getName())).thenReturn(Optional.empty());
        when(storesRepo.save(any(Store.class))).thenReturn(store);
        when(utils.generateExternalId()).thenReturn(EXTERNAL_ID);


        when(modelMapper.map(any(StoreInDTO.class), any())).thenReturn(store);
        when(modelMapper.map(any(Store.class), any())).thenReturn(storeOutDTO);

        StoreOutDTO resStore = storesService.save(storeInDTO);
        verify(storesRepo, times(1)).save(any(Store.class));
        verify(modelMapper, times(1)).map(any(StoreInDTO.class), any());
        verify(modelMapper, times(1)).map(any(Store.class), any());

        assertNotNull(resStore);
        assertEquals(store.getName(), resStore.getName());
        assertEquals(EXTERNAL_ID, resStore.getExternalId());

    }

    @Test
    void deleteByExternalId() {
        final String EXTERNAL_ID = "123abc";
        Store store = new Store();
        store.setExternalId(EXTERNAL_ID);
        store.setName("Test");

        // store ????????????????????
        when(storesRepo.getByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(store));

        storesService.deleteByExternalId(EXTERNAL_ID);
        verify(storesRepo, times(1)).deleteById(anyInt());

        // store ???? ????????????????????
        reset(storesRepo);
        when(storesRepo.getByExternalId(EXTERNAL_ID)).thenReturn(Optional.empty());

        assertThrows(StoreNotFoundException.class, () -> storesService.deleteByExternalId(EXTERNAL_ID));
        verify(storesRepo, times(0)).deleteById(anyInt());
    }

    @Test
    void update() {
        final String EXTERNAL_ID = "123abc";
        final String NAME = "Test";

        StoreInDTO storeInDTO = new StoreInDTO();
        storeInDTO.setName(NAME);

        Store store = new Store();
        store.setExternalId(EXTERNAL_ID);
        store.setName(NAME);

        StoreOutDTO storeOutDTO = new StoreOutDTO();
        storeOutDTO.setName(NAME);
        storeOutDTO.setExternalId(EXTERNAL_ID);

        // store ????????????????????
        when(storesRepo.getByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(store));
        when(storesRepo.save(any(Store.class))).thenReturn(store);
        when(modelMapper.map(any(Store.class), any())).thenReturn(storeOutDTO);

        StoreOutDTO resStore = storesService.update(EXTERNAL_ID, storeInDTO);
        verify(storesRepo, times(1)).save(any(Store.class));

        assertNotNull(resStore);
        assertEquals(EXTERNAL_ID, resStore.getExternalId());
        assertEquals(NAME, resStore.getName());

        // store ???? ????????????????????
        reset(storesRepo);
        when(storesRepo.getByExternalId(EXTERNAL_ID)).thenReturn(Optional.empty());
        assertThrows(StoreNotFoundException.class, () -> storesService.update(EXTERNAL_ID, storeInDTO));
        verify(storesRepo, times(0)).save(any());
    }

    @Test
    void index() {
        when(storesRepo.findAll(any(Pageable.class))).thenReturn(repoIndex());
        when(modelMapper.map(any(Store.class), any())).thenReturn(new StoreOutDTO());

        List<StoreOutDTO> resList = storesService.index(0, 10);
        assertNotNull(resList);
        assertEquals(repoIndex().getContent().size(), resList.size());
        verify(storesRepo, times(1)).findAll(any(Pageable.class));
        verify(storesRepo, times(0)).findAll();
    }

    @Test
    void getByName() {
        final String NAME = "Test";
        Store store = new Store();
        store.setName(NAME);

        StoreOutDTO storeOutDTO = new StoreOutDTO();
        storeOutDTO.setName(NAME);

        // store ????????????????????
        when(storesRepo.getByName(NAME)).thenReturn(Optional.of(store));
        when(modelMapper.map(any(Store.class), any())).thenReturn(storeOutDTO);

        Optional<StoreOutDTO> resStore = storesService.getByName(NAME);
        assertNotNull(resStore);
        assertTrue(resStore.isPresent());
        assertEquals(NAME, resStore.get().getName());
        verify(storesRepo, times(1)).getByName(anyString());


        // store ???? ????????????????????
        reset(storesRepo);
        when(storesRepo.getByName(NAME)).thenReturn(Optional.empty());

        resStore = storesService.getByName(NAME);
        assertFalse(resStore.isPresent());
        verify(storesRepo, times(1)).getByName(anyString());
    }

    @Test
    void getByExternalId() {
        final String EXTERNAL_ID = "123abc";
        Store store = new Store();
        store.setExternalId(EXTERNAL_ID);

        StoreOutDTO storeOutDTO = new StoreOutDTO();
        storeOutDTO.setExternalId(EXTERNAL_ID);

        // store ????????????????????
        when(storesRepo.getByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(store));
        when(modelMapper.map(any(Store.class), any())).thenReturn(storeOutDTO);

        StoreOutDTO resStore = storesService.getByExternalId(EXTERNAL_ID);
        assertNotNull(resStore);

        assertEquals(EXTERNAL_ID, resStore.getExternalId());
        verify(storesRepo, times(1)).getByExternalId(anyString());

        // store ???? ????????????????????
        reset(storesRepo);
        when(storesRepo.getByExternalId(EXTERNAL_ID)).thenReturn(Optional.empty());

        assertThrows(StoreNotFoundException.class, () -> storesService.getByExternalId(EXTERNAL_ID));
        verify(storesRepo, times(1)).getByExternalId(anyString());
    }

    private Page<Store> repoIndex() {
        return new PageImpl<>(List.of(
                new Store() {
                    {
                        setId(1);
                        setExternalId("a");
                        setName("Test1");
                    }
                },
                new Store() {
                    {
                        setId(2);
                        setExternalId("b");
                        setName("Test2");
                    }
                },
                new Store() {
                    {
                        setId(3);
                        setExternalId("c");
                        setName("Test3");
                    }
                }
        ));
    }
}