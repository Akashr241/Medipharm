package com.example.demo.ai.prescription.service;

import com.example.demo.ai.chatbot.client.GeminiClient;
import com.example.demo.ai.prescription.dto.MedicineResponseDto;
import com.example.demo.ai.prescription.dto.PrescriptionResponseDto;
import com.example.demo.ai.prescription.util.MedicineNameNormalizer;
import com.example.demo.product.entity.Product;
import com.example.demo.product.repository.ProductRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PrescriptionServiceImpl
        implements PrescriptionService {

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;
    private final MedicineService medicineService;
    private final ProductRepository productRepository;
    private final MedicineNameNormalizer normalizer;

    public PrescriptionServiceImpl(
            GeminiClient geminiClient,
            ObjectMapper objectMapper,
            MedicineService medicineService,
            ProductRepository productRepository,
            MedicineNameNormalizer normalizer) {

        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
        this.medicineService = medicineService;
        this.productRepository = productRepository;
        this.normalizer = normalizer;
    }

    @Override
    public List<PrescriptionResponseDto> analyzePrescription(
            String extractedText) {

        System.out.println(
                "========== PRESCRIPTION SERVICE =========="
        );

        System.out.println("OCR TEXT:");
        System.out.println(extractedText);

        System.out.println(
                "=========================================="
        );


        // =====================================================
        // STEP 1: GEMINI
        // =====================================================

        String prompt = """

                You are a prescription analysis AI.

                Analyze the following OCR text extracted
                from a doctor's prescription.

                Identify EVERY medicine prescribed.

                OCR text may contain spelling mistakes.
                Correct obvious OCR mistakes using medical context.

                IMPORTANT:

                - Do not skip any medicine.
                - If 4 medicines are present, return 4 medicines.
                - Do not merge two different medicines.
                - Do not duplicate medicines.
                - Do not invent medicines that are not present.

                For every medicine return:

                - medicineName
                - dosage
                - frequency
                - duration

                Return ONLY valid JSON.

                Do NOT return markdown.
                Do NOT return explanations.
                Do NOT use ```json.

                Return an array using exactly this format:

                [
                  {
                    "medicineName": "Calpol",
                    "dosage": "500mg",
                    "frequency": "twice daily",
                    "duration": "5 days"
                  }
                ]

                Rules:

                medicineName:
                The name of the medicine.

                dosage:
                The strength or dosage such as 500mg, 5ml, etc.

                frequency:
                How often the medicine should be taken.

                duration:
                How long the medicine should be taken.

                Do not invent information.

                If information is not present,
                return an empty string.

                PRESCRIPTION OCR:
                """ + extractedText;


        String aiResult = geminiClient.askGemini(prompt);


        System.out.println(
                "========== GEMINI RESULT =========="
        );

        System.out.println(aiResult);


        // =====================================================
        // STEP 2: CLEAN JSON
        // =====================================================

        String cleanResult = cleanJson(aiResult);

        List<PrescriptionResponseDto> finalResults =
                new ArrayList<>();


        try {

            JsonNode medicines =
                    objectMapper.readTree(cleanResult);


            if (!medicines.isArray()) {

                throw new RuntimeException(
                        "Gemini response is not a JSON array"
                );
            }


            System.out.println(
                    "TOTAL MEDICINES DETECTED BY GEMINI: "
                            + medicines.size()
            );


            // =================================================
            // STEP 3: PROCESS EACH MEDICINE
            // =================================================

            for (JsonNode medicineNode : medicines) {

                String medicineName =
                        medicineNode
                                .path("medicineName")
                                .asText("")
                                .trim();

                String dosage =
                        medicineNode
                                .path("dosage")
                                .asText("")
                                .trim();

                String frequency =
                        medicineNode
                                .path("frequency")
                                .asText("")
                                .trim();

                String duration =
                        medicineNode
                                .path("duration")
                                .asText("")
                                .trim();


                if (medicineName.isEmpty()) {

                    System.out.println(
                            "Skipping medicine because name is empty."
                    );

                    continue;
                }


                System.out.println(
                        "========== MEDICINE =========="
                );

                System.out.println(
                        "Gemini Medicine: " + medicineName
                );

                System.out.println(
                        "Dosage: " + dosage
                );

                System.out.println(
                        "Frequency: " + frequency
                );

                System.out.println(
                        "Duration: " + duration
                );


                // =================================================
                // STEP 4: NORMALIZE
                // =================================================

                String normalizedName =
                        normalizer.normalize(medicineName);

                System.out.println(
                        "Normalized Medicine: "
                                + normalizedName
                );


                // =================================================
                // STEP 5: MEDICINE DATABASE + RANKING
                // =================================================

                List<MedicineResponseDto> medicineResults =
                        medicineService.searchMedicine(
                                normalizedName
                        );

                System.out.println(
                        "Medicine Matches: "
                                + medicineResults.size()
                );


                if (medicineResults.isEmpty()) {

                    System.out.println(
                            "NO MEDICINE FOUND: "
                                    + medicineName
                    );

                    continue;
                }


                /*
                 * MedicineService has already performed
                 * the medicine search and ranking.
                 *
                 * The first result is therefore the
                 * highest-ranked medicine.
                 */

                MedicineResponseDto selectedMedicine =
                        medicineResults.get(0);


                System.out.println(
                        "SELECTED MEDICINE: "
                                + selectedMedicine.getName()
                );


                // =================================================
                // STEP 6: SEARCH PRODUCT DATABASE
                // =================================================

                String productSearchName =
                        selectedMedicine.getName();

                System.out.println(
                        "PRODUCT SEARCH NAME: "
                                + productSearchName
                );


                List<Product> products =
                        productRepository
                                .findByNameContainingIgnoreCase(
                                        productSearchName
                                );


                System.out.println(
                        "PRODUCT MATCHES: "
                                + products.size()
                );


                // =================================================
                // STEP 7: NO PRODUCT
                // =================================================

                if (products.isEmpty()) {

                    System.out.println(
                            "NO PRODUCT FOUND"
                    );

                    System.out.println(
                            "Medicine: "
                                    + medicineName
                    );

                    System.out.println(
                            "Selected Medicine: "
                                    + selectedMedicine.getName()
                    );

                    System.out.println(
                            "Product Search: "
                                    + productSearchName
                    );

                    System.out.println(
                            "-----------------------------------"
                    );

                    continue;
                }


                // =================================================
                // STEP 8: PROCESS PRODUCTS
                // =================================================

                for (Product product : products) {

                    System.out.println(
                            "========== PRODUCT FOUND =========="
                    );

                    System.out.println(
                            "Product ID: "
                                    + product.getId()
                    );

                    System.out.println(
                            "Product Name: "
                                    + product.getName()
                    );

                    System.out.println(
                            "Product Price: ₹"
                                    + product.getPrice()
                    );

                    System.out.println(
                            "Product Category: "
                                    + product.getCategory()
                    );


                    // =================================================
                    // STEP 9: CREATE RESPONSE
                    // =================================================

                    PrescriptionResponseDto dto =
                            new PrescriptionResponseDto();


                    // Prescription information

                    dto.setMedicineName(
                            medicineName
                    );

                    dto.setDosage(
                            dosage
                    );

                    dto.setFrequency(
                            frequency
                    );

                    dto.setDuration(
                            duration
                    );


                    // Product information

                    dto.setProductId(
                            product.getId()
                    );

                    dto.setProductName(
                            product.getName()
                    );

                    dto.setPrice(
                            product.getPrice()
                    );


                    finalResults.add(dto);


                    System.out.println(
                            "Product added to final result."
                    );

                    System.out.println(
                            "=================================="
                    );
                }
            }


        } catch (Exception e) {

            System.out.println(
                    "ERROR PROCESSING GEMINI RESPONSE"
            );

            e.printStackTrace();

            throw new RuntimeException(
                    "Unable to process prescription AI response",
                    e
            );
        }


        // =====================================================
        // FINAL RESULT
        // =====================================================

        System.out.println(
                "========== PRESCRIPTION FINAL RESULT =========="
        );

        System.out.println(
                "Total products returned: "
                        + finalResults.size()
        );

        System.out.println(
                "================================================"
        );


        return finalResults;
    }


    // =========================================================
    // CLEAN GEMINI JSON
    // =========================================================

    private String cleanJson(String response) {

        if (response == null) {

            throw new RuntimeException(
                    "Gemini returned empty response"
            );
        }


        response = response.trim();


        if (response.startsWith("```json")) {

            response =
                    response.substring(7);
        }


        if (response.startsWith("```")) {

            response =
                    response.substring(3);
        }


        if (response.endsWith("```")) {

            response =
                    response.substring(
                            0,
                            response.length() - 3
                    );
        }


        return response.trim();
    }
}