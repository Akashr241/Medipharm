# Render deployment and diagnostics

## Render service configuration

The Spring Boot backend lives in `E-commerce`. Configure the Render Web Service as follows:

- **Root Directory:** `E-commerce`
- **Runtime:** Docker
- **Dockerfile Path:** `./Dockerfile`
- **Health Check Path:** `/actuator/health`

The Docker build runs `./mvnw -B clean verify`, so a failing JUnit test stops the deployment and its test output is visible in the Render build logs. The running JVM writes logs and full exception stack traces to standard output, which Render captures in the service logs.

## Required environment variables

Set these in the Render service's Environment settings. Do not commit credentials to this repository.

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `RAZORPAY_KEY_ID`
- `RAZORPAY_KEY_SECRET`
- `SIGHTENGINE_API_USER`
- `SIGHTENGINE_API_SECRET`
- `PRESCRIPTION_AI_DETECTION_ENABLED`
- `PRESCRIPTION_AI_REJECT_THRESHOLD`
- `TESSERACT_DATA_PATH` (for the Docker image, typically `/usr/share/tesseract-ocr/5/tessdata`)
- `GOOGLE_API_KEY`
- `GEMINI_API_URL`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `FRONTEND_URL` (the deployed frontend URL)

Render supplies `PORT`; the application uses it automatically. `GEMINI_API_MODEL` and the fallback model variables have application defaults.

## Running tests locally

From the `E-commerce` directory:

```bash
./mvnw -B clean verify
```

If a deployment fails, check both the Render **Build Logs** (Maven/JUnit and Docker build output) and **Service Logs** (application startup and runtime exceptions). The `/actuator/health` endpoint provides a basic readiness check without exposing health details.
