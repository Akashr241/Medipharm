package com.example.demo.ai.prescription.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Service;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class OCRServiceImpl implements OCRService {

    private static final Logger log =
            LoggerFactory.getLogger(OCRServiceImpl.class);

    private static final String AI_DETECTION_URL =
            "https://api.sightengine.com/1.0/check.json";

    private final ITesseract tesseract;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final String sightengineApiUser;
    private final String sightengineApiSecret;

    private final double aiRejectThreshold;
    private final boolean aiDetectionEnabled;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public OCRServiceImpl(

            ITesseract tesseract,

            @Value("${sightengine.api.user}")
            String sightengineApiUser,

            @Value("${sightengine.api.secret}")
            String sightengineApiSecret,

            @Value("${prescription.ai-reject-threshold}")
            double aiRejectThreshold,

            @Value("${prescription.ai-detection.enabled}")
            boolean aiDetectionEnabled

    ) {

        this.tesseract = tesseract;

        this.restTemplate = new RestTemplate();

        this.objectMapper = new ObjectMapper();

        this.sightengineApiUser = sightengineApiUser;
        this.sightengineApiSecret = sightengineApiSecret;

        this.aiRejectThreshold = aiRejectThreshold;
        this.aiDetectionEnabled = aiDetectionEnabled;

        log.info("Simple OCR Service Ready");
        log.info("AI Detection Enabled: {}", aiDetectionEnabled);
    }


    // =====================================================
    // MAIN OCR METHOD
    // =====================================================

    @Override
    public String extractText(MultipartFile file) {

        long startTime = System.currentTimeMillis();

        log.info("========== OCR STARTED ==========");

        // 1. Validate file
        validateFile(file);


        // 2. Read uploaded image
        byte[] imageBytes;

        try {

            imageBytes = file.getBytes();

        } catch (IOException exception) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unable to read uploaded image",
                    exception
            );
        }


        // 3. AI-generated image verification
        if (aiDetectionEnabled) {

            rejectIfAiGenerated(
                    imageBytes,
                    file.getOriginalFilename()
            );
        }


        // 4. Convert bytes to image
        BufferedImage image =
                readImageFromBytes(imageBytes);


        log.info(
                "Image: {} | Size: {}x{}",
                file.getOriginalFilename(),
                image.getWidth(),
                image.getHeight()
        );


        // =================================================
        // SINGLE OCR PASS
        // =================================================

        String extractedText =
                performOcr(image);


        // =================================================
        // VALIDATE OCR RESULT
        // =================================================

        if (extractedText.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "OCR could not detect readable text"
            );
        }


        long endTime = System.currentTimeMillis();

        log.info(
                "OCR completed in {} ms",
                endTime - startTime
        );

        log.info("========== OCR COMPLETED ==========");
        log.info("OCR Text:\n{}", extractedText);


        return extractedText;
    }


    // =====================================================
    // TESSERACT OCR
    // =====================================================

    private String performOcr(
            BufferedImage image
    ) {

        try {

            synchronized (tesseract) {

                // PSM 6:
                // Assume the image contains one block of text.
                tesseract.setPageSegMode(6);

                String text =
                        tesseract.doOCR(image);

                return cleanText(text);
            }

        } catch (TesseractException exception) {

            log.error(
                    "Tesseract OCR failed",
                    exception
            );

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Unable to extract text from prescription",
                    exception
            );
        }
    }


    // =====================================================
    // AI GENERATED IMAGE DETECTION
    // =====================================================

    private void rejectIfAiGenerated(

            byte[] imageBytes,

            String originalFilename

    ) {

        boolean userConfigured =
                sightengineApiUser != null
                        &&
                !sightengineApiUser.isBlank();


        boolean secretConfigured =
                sightengineApiSecret != null
                        &&
                !sightengineApiSecret.isBlank();


        if (!userConfigured || !secretConfigured) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "AI image verification service is not configured"
            );
        }


        ByteArrayResource imageResource =
                new ByteArrayResource(imageBytes) {

                    @Override
                    public String getFilename() {

                        if (
                                originalFilename == null
                                        ||
                                originalFilename.isBlank()
                        ) {

                            return "prescription-image.jpg";
                        }

                        return originalFilename;
                    }
                };


        MultiValueMap<String, Object> body =
                new LinkedMultiValueMap<>();


        body.add(
                "media",
                imageResource
        );

        body.add(
                "models",
                "genai"
        );

        body.add(
                "api_user",
                sightengineApiUser
        );

        body.add(
                "api_secret",
                sightengineApiSecret
        );


        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.MULTIPART_FORM_DATA
        );


        try {

            log.info("Checking AI-generated image...");


            ResponseEntity<String> response =
                    restTemplate.postForEntity(

                            AI_DETECTION_URL,

                            new HttpEntity<>(
                                    body,
                                    headers
                            ),

                            String.class
                    );


            if (
                    response.getBody() == null
                            ||
                    response.getBody().isBlank()
            ) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "AI verification returned empty response"
                );
            }


            JsonNode root =
                    objectMapper.readTree(
                            response.getBody()
                    );


            String status =
                    root.path("status").asText();


            if (!"success".equals(status)) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "AI image verification failed"
                );
            }


            double aiScore =
                    root
                            .path("type")
                            .path("ai_generated")
                            .asDouble(-1);


            log.info(
                    "AI Generated Score: {}",
                    aiScore
            );


            if (aiScore < 0) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "AI verification returned no score"
                );
            }


            if (aiScore >= aiRejectThreshold) {

                throw new ResponseStatusException(

                        HttpStatus.UNPROCESSABLE_ENTITY,

                        "This prescription image appears to be " +
                        "AI-generated or AI-edited. " +
                        "Please upload an original prescription image."
                );
            }


            log.info("AI image verification PASSED");


        } catch (RestClientException exception) {

            log.error(
                    "Sightengine request failed",
                    exception
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Could not verify AI-generated image",
                    exception
            );


        } catch (IOException exception) {

            log.error(
                    "Unable to parse AI verification response",
                    exception
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Could not read AI verification response",
                    exception
            );
        }
    }


    // =====================================================
    // FILE VALIDATION
    // =====================================================

    private void validateFile(
            MultipartFile file
    ) {

        if (file == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please upload an image file"
            );
        }


        if (file.isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Uploaded file is empty"
            );
        }


        String contentType =
                file.getContentType();


        if (
                contentType == null
                        ||
                !contentType.startsWith("image/")
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only image files are supported"
            );
        }
    }


    // =====================================================
    // READ IMAGE
    // =====================================================

    private BufferedImage readImageFromBytes(
            byte[] imageBytes
    ) {

        try {

            BufferedImage image =
                    ImageIO.read(
                            new ByteArrayInputStream(
                                    imageBytes
                            )
                    );


            if (image == null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid or unsupported image"
                );
            }


            return image;


        } catch (IOException exception) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unable to read uploaded image",
                    exception
            );
        }
    }


    // =====================================================
    // CLEAN OCR TEXT
    // =====================================================

    private String cleanText(
            String text
    ) {

        if (text == null) {

            return "";
        }


        return text

                .replace("\r", "")

                .replaceAll(
                        "[ \\t]+",
                        " "
                )

                .replaceAll(
                        "\\n{3,}",
                        "\n\n"
                )

                .trim();
    }
}