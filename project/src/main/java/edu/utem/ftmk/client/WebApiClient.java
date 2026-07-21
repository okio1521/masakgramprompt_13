package edu.utem.ftmk.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.utem.ftmk.model.Experiment;
import edu.utem.ftmk.model.IngredientResult;
import edu.utem.ftmk.model.LlmModel;
import edu.utem.ftmk.model.NutritionResult;
import edu.utem.ftmk.model.PromptTechnique;
import edu.utem.ftmk.model.Transcript;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebApiClient {

    private final String baseUrl;
    private final HttpClient client;
    private final Gson gson;

    public WebApiClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    private String get(String path) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("HTTP GET failed: " + response.statusCode() + " - " + response.body());
            }

            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("GET request failed for path: " + path + " - " + e.getMessage(), e);
        }
    }

    private String post(String path, String jsonBody, Duration timeout) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

            if (timeout != null) {
                builder.timeout(timeout);
            }

            HttpRequest request = builder.build();
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("HTTP POST failed: " + response.statusCode() + " - " + response.body());
            }

            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("POST request failed for path: " + path + " - " + e.getMessage(), e);
        }
    }

    public List<Transcript> getAllTranscripts() {
        String json = get("/api/transcripts");
        return gson.fromJson(json, new TypeToken<List<Transcript>>() {}.getType());
    }

    public Transcript getTranscriptById(int id) {
        String json = get("/api/transcripts/" + id);
        return gson.fromJson(json, Transcript.class);
    }

    public void saveTranscript(Transcript transcript) {
        String body = gson.toJson(transcript);
        post("/api/transcripts", body, null);
    }

    public int addTranscript(Transcript transcript) {
        String body = gson.toJson(transcript);
        String json = post("/api/transcripts", body, null);

        try {
            Map<String, Object> result = gson.fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());
            Object idObj = result.get("transcriptId");

            if (idObj instanceof Number) {
                return ((Number) idObj).intValue();
            }
            if (idObj != null) {
                return Integer.parseInt(idObj.toString());
            }
            return -1;
        } catch (Exception e) {
            try {
                Transcript saved = gson.fromJson(json, Transcript.class);
                return saved != null ? saved.getTranscriptId() : -1;
            } catch (Exception ex) {
                return -1;
            }
        }
    }

    public List<LlmModel> getModels() {
        String json = get("/api/experiments/models");
        return gson.fromJson(json, new TypeToken<List<LlmModel>>() {}.getType());
    }

    public List<LlmModel> getAllModels() {
        return getModels();
    }

    public List<PromptTechnique> getTechniques() {
        String json = get("/api/experiments/techniques");
        return gson.fromJson(json, new TypeToken<List<PromptTechnique>>() {}.getType());
    }

    public List<PromptTechnique> getAllTechniques() {
        return getTechniques();
    }

    public List<Experiment> getAllExperiments() {
        String json = get("/api/experiments");
        return gson.fromJson(json, new TypeToken<List<Experiment>>() {}.getType());
    }

    public Experiment getExperimentById(int id) {
        String json = get("/api/experiments/" + id);
        return gson.fromJson(json, Experiment.class);
    }

    public Experiment runExperiment(int transcriptId, int modelId, int techniqueId, boolean ragEnabled) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("transcriptId", transcriptId);
        payload.put("modelId", modelId);
        payload.put("techniqueId", techniqueId);
        payload.put("ragEnabled", ragEnabled);

        String body = gson.toJson(payload);
        String json = post("/api/experiments/run", body, Duration.ofMinutes(25));
        return gson.fromJson(json, Experiment.class);
    }

    public NutritionResult getNutritionByExperimentId(int experimentId) {
        String json = get("/api/nutrition/" + experimentId);
        return gson.fromJson(json, NutritionResult.class);
    }

    public NutritionResult getNutritionResultByExperimentId(int experimentId) {
        return getNutritionByExperimentId(experimentId);
    }

    public NutritionResult getGroundTruthByTranscriptId(int transcriptId) {
        String json = get("/api/nutrition/ground-truth/" + transcriptId);
        return gson.fromJson(json, NutritionResult.class);
    }

    public List<IngredientResult> getIngredientsByResultId(int resultId) {
        String json = get("/api/nutrition/ingredients/" + resultId);
        return gson.fromJson(json, new TypeToken<List<IngredientResult>>() {}.getType());
    }

    public List<IngredientResult> getIngredientResultsByResultId(int resultId) {
        return getIngredientsByResultId(resultId);
    }

    public String exportLayerToCsv(String layerKey) {
        return get("/api/export/layers/" + layerKey.trim().toLowerCase());
    }
}