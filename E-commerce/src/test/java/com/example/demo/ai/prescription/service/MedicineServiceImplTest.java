package com.example.demo.ai.prescription.service;

import com.example.demo.ai.prescription.entity.Medicine;
import com.example.demo.ai.prescription.repository.MedicineRepository;
import com.example.demo.ai.prescription.util.MedicineSearchRanker;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MedicineServiceImplTest {

    @Test
    void retriesMedicineSearchByTokensWithoutDroppingRequestedStrength() {
        MedicineRepository repository = mock(MedicineRepository.class);
        MedicineSearchRanker ranker = new MedicineSearchRanker();
        MedicineServiceImpl service = new MedicineServiceImpl(repository, ranker);

        Medicine correctStrength = medicine(1L, "Calpol 250 mg Syrup");
        Medicine differentStrength = medicine(2L, "Calpol 500 mg Syrup");

        when(repository.findByNameContainingIgnoreCaseAndDiscontinuedFalse("Calpol 250"))
                .thenReturn(List.of());
        when(repository.findByNameContainingIgnoreCaseAndDiscontinuedFalse("calpol"))
                .thenReturn(List.of(correctStrength, differentStrength));
        when(repository.findByNameContainingIgnoreCaseAndDiscontinuedFalse("250"))
                .thenReturn(List.of(correctStrength));

        var results = service.searchMedicine("Calpol 250");

        assertEquals(1, results.size());
        assertEquals("Calpol 250 mg Syrup", results.get(0).getName());
    }

    private Medicine medicine(Long id, String name) {
        Medicine medicine = new Medicine();
        medicine.setId(id);
        medicine.setName(name);
        return medicine;
    }
}