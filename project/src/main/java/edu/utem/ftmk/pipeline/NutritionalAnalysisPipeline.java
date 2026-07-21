package edu.utem.ftmk.pipeline;

import edu.utem.ftmk.db.ExperimentRepository;
import edu.utem.ftmk.db.GroundTruthRepository;
import edu.utem.ftmk.db.IngredientResultRepository;
import edu.utem.ftmk.db.LlmModelRepository;
import edu.utem.ftmk.db.NutritionResultRepository;
import edu.utem.ftmk.db.PromptTechniqueRepository;
import edu.utem.ftmk.db.TranscriptRepository;
import edu.utem.ftmk.llm.LLMService;
import edu.utem.ftmk.model.Experiment;
import edu.utem.ftmk.model.IngredientResult;
import edu.utem.ftmk.model.LlmModel;
import edu.utem.ftmk.model.NutritionResult;
import edu.utem.ftmk.model.PromptTechnique;
import edu.utem.ftmk.model.Transcript;
import edu.utem.ftmk.prompt.PromptEngine;
import edu.utem.ftmk.prompt.PromptEngine.CompiledPrompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

/**
 * Orchestrates the full nutritional analysis pipeline:
 * 1. Checks or creates an experiment run record.
 * 2. Compiles prompts.
 * 3. Sends system and user prompts to the local LLM.
 * 4. Parses the JSON output.
 * 5. Persists the results (nutrition totals, per-serving facts, and ingredient breakdown).
 */
@Service
public class NutritionalAnalysisPipeline {

    private final LLMService llmService;
    private final PromptEngine promptEngine;
    private final JsonResponseParser jsonResponseParser;

    private final LlmModelRepository modelRepository;
    private final PromptTechniqueRepository techniqueRepository;
    private final TranscriptRepository transcriptRepository;
    private final ExperimentRepository experimentRepository;
    private final NutritionResultRepository nutritionResultRepository;
    private final IngredientResultRepository ingredientResultRepository;
    private final GroundTruthRepository groundTruthRepository;

    @Autowired
    public NutritionalAnalysisPipeline(LLMService llmService,
                                       PromptEngine promptEngine,
                                       JsonResponseParser jsonResponseParser,
                                       LlmModelRepository modelRepository,
                                       PromptTechniqueRepository techniqueRepository,
                                       TranscriptRepository transcriptRepository,
                                       ExperimentRepository experimentRepository,
                                       NutritionResultRepository nutritionResultRepository,
                                       IngredientResultRepository ingredientResultRepository,
                                       GroundTruthRepository groundTruthRepository) {
        this.llmService = llmService;
        this.promptEngine = promptEngine;
        this.jsonResponseParser = jsonResponseParser;
        this.modelRepository = modelRepository;
        this.techniqueRepository = techniqueRepository;
        this.transcriptRepository = transcriptRepository;
        this.experimentRepository = experimentRepository;
        this.nutritionResultRepository = nutritionResultRepository;
        this.ingredientResultRepository = ingredientResultRepository;
        this.groundTruthRepository = groundTruthRepository;
    }

    /**
     * Executes the experimental condition. If the experiment was run before,
     * it clears old results and re-runs.
     */
    public Experiment runExperiment(int transcriptId, int modelId, int techniqueId, boolean ragEnabled) {
        long startTime = System.currentTimeMillis();

        Experiment exp = experimentRepository.findByCondition(transcriptId, modelId, techniqueId, ragEnabled);
        if (exp == null) {
            exp = new Experiment();
            exp.setTranscriptId(transcriptId);
            exp.setModelId(modelId);
            exp.setTechniqueId(techniqueId);
            exp.setRagEnabled(ragEnabled);
            exp.setStatus("pending");
            experimentRepository.save(exp);
        }

        try {
            exp.setStatus("running");
            experimentRepository.updateStatus(exp.getExperimentId(), "running", null, null);

            LlmModel model = modelRepository.findById(modelId);
            PromptTechnique technique = techniqueRepository.findById(techniqueId);
            Transcript transcript = transcriptRepository.findById(transcriptId);

            if (model == null || technique == null || transcript == null) {
                throw new IllegalArgumentException(
                        "Model, Technique, or Transcript not found for IDs: "
                                + modelId + ", " + techniqueId + ", " + transcriptId
                );
            }

            CompiledPrompt compiled = promptEngine.compile(technique, transcript.getContent());

            if (Thread.currentThread().isInterrupted()) {
                return null;
            }

            String rawOutput = llmService.promptWithSystem(
                    model.getModelTag(),
                    compiled.getSystemPrompt(),
                    compiled.getUserPrompt()
            );

            if (Thread.currentThread().isInterrupted()) {
                return null;
            }

            nutritionResultRepository.deleteByExperimentId(exp.getExperimentId());

            NutritionResult parsedResult = null;
            String lastRawOutput = rawOutput;
            Exception lastParseError = null;
            int maxAttempts = 3;

            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    parsedResult = jsonResponseParser.parse(lastRawOutput, exp.getExperimentId());
                    parsedResult.setJsonValid(true);
                    lastParseError = null;
                    break;
                } catch (Exception parseException) {
                    lastParseError = parseException;
                    System.err.println(
                            "[Pipeline] JSON parse attempt " + attempt + "/" + maxAttempts +
                            " failed for experiment " + exp.getExperimentId() + ": " +
                            parseException.getMessage()
                    );

                    if (attempt < maxAttempts) {
                        if (Thread.currentThread().isInterrupted()) {
                            return null;
                        }

                        System.out.println(
                                "[Pipeline] Retrying LLM query (attempt " + (attempt + 1) +
                                "/" + maxAttempts + ")..."
                        );

                        try {
                            lastRawOutput = llmService.promptWithSystem(
                                    model.getModelTag(),
                                    compiled.getSystemPrompt(),
                                    compiled.getUserPrompt()
                            );
                        } catch (Exception retryEx) {
                            System.err.println("[Pipeline] Retry LLM call failed: " + retryEx.getMessage());
                        }
                    }
                }
            }

            if (lastParseError != null) {
                System.err.println(
                        "[Pipeline] All " + maxAttempts + " parse attempts failed for experiment " +
                        exp.getExperimentId() + ". Saving partial result."
                );
                parsedResult = new NutritionResult();
                parsedResult.setExperimentId(exp.getExperimentId());
                parsedResult.setRawJsonOutput(lastRawOutput);
                parsedResult.setJsonValid(false);
            }

            int resultId = nutritionResultRepository.save(parsedResult);

            List<IngredientResult> ingredients = parsedResult.getIngredients();
            if (ingredients != null && !ingredients.isEmpty()) {
                NutritionResult gtResult = groundTruthRepository.findByTranscriptId(transcriptId);

                for (IngredientResult ing : ingredients) {
                    ing.setResultId(resultId);

                    boolean found = false;
                    if (gtResult != null && gtResult.getIngredients() != null) {
                        String predOrig = ing.getNameOriginal() != null
                                ? ing.getNameOriginal().trim().toLowerCase()
                                : "";
                        String predEn = ing.getNameEn() != null
                                ? ing.getNameEn().trim().toLowerCase()
                                : "";

                        for (IngredientResult gtIng : gtResult.getIngredients()) {
                            String gtOrig = gtIng.getNameOriginal() != null
                                    ? gtIng.getNameOriginal().trim().toLowerCase()
                                    : "";
                            String gtEn = gtIng.getNameEn() != null
                                    ? gtIng.getNameEn().trim().toLowerCase()
                                    : "";

                            if ((!predOrig.isEmpty() && (predOrig.equals(gtOrig) || predOrig.equals(gtEn))) ||
                                (!predEn.isEmpty() && (predEn.equals(gtOrig) || predEn.equals(gtEn)))) {
                                found = true;
                                break;
                            }
                        }
                    }

                    ing.setIsHallucinated(!found);
                }

                ingredientResultRepository.saveAll(ingredients);
            }

            long endSuccessTime = System.currentTimeMillis();
            long duration = endSuccessTime - startTime;

            exp.setStatus("completed");
            experimentRepository.updateStatus(
                    exp.getExperimentId(),
                    "completed",
                    new Timestamp(endSuccessTime),
                    (int) duration
            );

        } catch (Exception e) {
            boolean isInterrupted =
                    Thread.currentThread().isInterrupted() ||
                    e instanceof InterruptedException ||
                    (e.getMessage() != null && e.getMessage().contains("Interrupted")) ||
                    (e.getCause() != null &&
                            (e.getCause() instanceof InterruptedException ||
                             (e.getCause().getMessage() != null &&
                              e.getCause().getMessage().contains("Interrupted")))) ||
                    (e.getCause() != null && e.getCause().getCause() != null &&
                            (e.getCause().getCause() instanceof InterruptedException ||
                             (e.getCause().getCause().getMessage() != null &&
                              e.getCause().getCause().getMessage().contains("Interrupted"))));

            if (isInterrupted) {
                System.out.println("Experiment " + exp.getExperimentId() + " run was cancelled/interrupted.");
                Thread.currentThread().interrupt();
                experimentRepository.updateStatus(
                        exp.getExperimentId(),
                        "cancelled",
                        new Timestamp(System.currentTimeMillis()),
                        null
                );
            } else {
                e.printStackTrace();
                long endFailedTime = System.currentTimeMillis();
                long duration = endFailedTime - startTime;
                exp.setStatus("failed");
                experimentRepository.updateStatus(
                        exp.getExperimentId(),
                        "failed",
                        new Timestamp(endFailedTime),
                        (int) duration
                );
            }
        }

        return experimentRepository.findById(exp.getExperimentId());
    }
}