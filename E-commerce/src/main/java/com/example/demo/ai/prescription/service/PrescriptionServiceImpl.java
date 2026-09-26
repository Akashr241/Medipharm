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
    public List<PrescriptionResponseDto>
    analyzePrescription(String extractedText) {

        System.out.println(
                "========== PRESCRIPTION SERVICE =========="
        );

        System.out.println("OCR TEXT:");
        System.out.println(extractedText);

        System.out.println(
                "=========================================="
        );


        // ==========================================
        // STEP 1: GEMINI AI ANALYSIS
        // ==========================================

        String prompt = """

                You are a prescription analysis AI.

                Analyze the following OCR text extracted
                from a doctor's prescription.

                Identify every medicine prescribed.

                OCR text may contain spelling mistakes.
                Correct obvious OCR mistakes using medical context.

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
                The strength or dosage such as 500mg,
                5ml, etc.

                frequency:
                How often the medicine should be taken.

                duration:
                How long the medicine should be taken.

                Do not invent information.

                If information is not present,
                return an empty string.

                PRESCRIPTION OCR:
                """ + extractedText;


        String aiResult =
                geminiClient.askGemini(prompt);


        System.out.println(
                "========== GEMINI RESULT =========="
        );

        System.out.println(aiResult);


        // ==========================================
        // STEP 2: CLEAN GEMINI RESPONSE
        // ==========================================

        String cleanResult =
                cleanJson(aiResult);


        List<PrescriptionResponseDto> finalResults =
                new ArrayList<>();


        try {

            JsonNode medicines =
                    objectMapper.readTree(cleanResult);


            // ==========================================
            // STEP 3: PROCESS EACH MEDICINE
            // ==========================================

            for (JsonNode medicineNode : medicines) {

                String medicineName =
                        medicineNode
                                .path("medicineName")
                                .asText();

                String dosage =
                        medicineNode
                                .path("dosage")
                                .asText();

                String frequency =
                        medicineNode
                                .path("frequency")
                                .asText();

                String duration =
                        medicineNode
                                .path("duration")
                                .asText();


                if (medicineName == null ||
                        medicineName.trim().isEmpty()) {

                    continue;
                }


                System.out.println(
                        "========== MEDICINE =========="
                );

                System.out.println(
                        "Gemini Medicine: "
                                + medicineName
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


                // ======================================
                // STEP 4: NORMALIZE MEDICINE NAME
                // ======================================

                String normalizedName =
                        normalizer.normalize(
                                medicineName
                        );

                System.out.println(
                        "Normalized Medicine: "
                                + normalizedName
                );


                // ======================================
                // STEP 5: EXISTING MEDICINE SEARCH
                // ======================================
                //
                // We reuse your existing:
                //
                // MedicineService
                // MedicineRepository
                // MedicineSearchRanker
                //
                // This part is responsible for
                // finding the best medicine match.
                // ======================================

                List<MedicineResponseDto> medicineResults =
                        medicineService.searchMedicine(
                                normalizedName
                        );


                System.out.println(
                        "Medicine Matches: "
                                + medicineResults.size()
                );


                // ======================================
                // STEP 6: GET BEST MEDICINE
                // ======================================

                if (!medicineResults.isEmpty()) {

                    MedicineResponseDto bestMedicine =
                            medicineResults.get(0);


                    System.out.println(
                            "BEST MEDICINE: "
                                    + bestMedicine.getName()
                    );


                    // ==================================
                    // STEP 7: SEARCH PRODUCT TABLE
                    // ==================================
                    //
                    // IMPORTANT:
                    //
                    // We do NOT use Medicine ID.
                    //
                    // We search the products table
                    // using the matched medicine name.
                    //
                    // The Product ID obtained here
                    // is the ID that Cart needs.
                    // ==================================

                    List<Product> products =
                            productRepository
                                    .findByNameContainingIgnoreCase(
                                            bestMedicine.getName()
                                    );


                    System.out.println(
                            "Product Matches: "
                                    + products.size()
                    );


                    // ==================================
                    // STEP 8: BEST PRODUCT
                    // ==================================

                    if (!products.isEmpty()) {

                        Product bestProduct =
                                products.get(0);


                        PrescriptionResponseDto dto =
                                new PrescriptionResponseDto();


                        // ==================================
                        // AI / PRESCRIPTION INFORMATION
                        // ==================================

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


                        // ==================================
                        // PRODUCT INFORMATION
                        // ==================================

                        dto.setProductId(
                                bestProduct.getId()
                        );

                        dto.setProductName(
                                bestProduct.getName()
                        );

                        dto.setPrice(
                                bestProduct.getPrice()
                        );


                        finalResults.add(dto);


                        // ==================================
                        // DEBUG OUTPUT
                        // ==================================

                        System.out.println(
                                "========== FINAL PRODUCT =========="
                        );

                        System.out.println(
                                "Medicine: "
                                        + medicineName
                        );

                        System.out.println(
                                "Product Name: "
                                        + bestProduct.getName()
                        );

                        System.out.println(
                                "Product ID: "
                                        + bestProduct.getId()
                        );

                        System.out.println(
                                "Product Price: ₹"
                                        + bestProduct.getPrice()
                        );

                        System.out.println(
                                "Dosage: "
                                        + dosage
                        );

                        System.out.println(
                                "Frequency: "
                                        + frequency
                        );

                        System.out.println(
                                "Duration: "
                                        + duration
                        );

                        System.out.println(
                                "==================================="
                        );


                    } else {

                        System.out.println(
                                "NO PRODUCT FOUND FOR MEDICINE: "
                                        + bestMedicine.getName()
                        );
                    }


                } else {

                    System.out.println(
                            "NO MEDICINE FOUND: "
                                    + medicineName
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


        return finalResults;
    }


    // ==============================================
    // CLEAN GEMINI JSON
    // ==============================================

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