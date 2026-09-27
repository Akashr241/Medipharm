package com.example.demo.ai.prescription.service;

import com.example.demo.ai.chatbot.client.GeminiClient;
import com.example.demo.ai.prescription.dto.MedicineResponseDto;
import com.example.demo.ai.prescription.util.MedicineNameNormalizer;
import com.example.demo.product.entity.Product;
import com.example.demo.product.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PrescriptionServiceImplTest {

    @Test
    void searchesProductsUsingPrescriptionMedicineNameFirst() {
        GeminiClient geminiClient = mock(GeminiClient.class);
        MedicineService medicineService = mock(MedicineService.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        MedicineResponseDto medicine = new MedicineResponseDto();
        medicine.setName("Theo Levolin 50 mg/5 mg Syrup");
        Product product = mock(Product.class);
        when(product.getId()).thenReturn(17L);
        when(product.getName()).thenReturn("Levolin Syrup");
        when(product.getPrice()).thenReturn(42.0);

        when(geminiClient.askGemini(anyString())).thenReturn("""
                [{"medicineName":"Levolin","dosage":"3ml","frequency":"three times daily","duration":"5 days"}]
                """);
        when(medicineService.searchMedicine("Levolin"))
                .thenReturn(List.of(medicine));
        when(productRepository.findByNameContainingIgnoreCase("Levolin"))
                .thenReturn(List.of(product));

        PrescriptionServiceImpl service = new PrescriptionServiceImpl(
                geminiClient,
                new ObjectMapper(),
                medicineService,
                productRepository,
                new MedicineNameNormalizer()
        );

        var results = service.analyzePrescription("Syp Levolin 3 ml");

        assertEquals(1, results.size());
        assertEquals(17L, results.get(0).getProductId());
        assertEquals("Levolin Syrup", results.get(0).getProductName());
        verify(productRepository)
                .findByNameContainingIgnoreCase("Levolin");
    }
}