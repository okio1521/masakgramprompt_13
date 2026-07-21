package edu.utem.ftmk.api;

import edu.utem.ftmk.db.ExperimentRepository;
import edu.utem.ftmk.db.LlmModelRepository;
import edu.utem.ftmk.db.PromptTechniqueRepository;
import edu.utem.ftmk.model.Experiment;
import edu.utem.ftmk.model.LlmModel;
import edu.utem.ftmk.model.PromptTechnique;
import edu.utem.ftmk.pipeline.NutritionalAnalysisPipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/experiments")
@CrossOrigin(origins = "*") // Allow Swing desktop client or cross-origin browser requests
public class ExperimentController {

    private final ExperimentRepository experimentRepository;
    private final LlmModelRepository modelRepository;
    private final PromptTechniqueRepository techniqueRepository;
    private final NutritionalAnalysisPipeline pipeline;

    @Autowired
    public ExperimentController(ExperimentRepository experimentRepository,
                                LlmModelRepository modelRepository,
                                PromptTechniqueRepository techniqueRepository,
                                NutritionalAnalysisPipeline pipeline) {
        this.experimentRepository = experimentRepository;
        this.modelRepository = modelRepository;
        this.techniqueRepository = techniqueRepository;
        this.pipeline = pipeline;
    }

    @GetMapping
    public List<Experiment> getAllExperiments() {
        return experimentRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Experiment> getExperimentById(@PathVariable int id) {
        Experiment exp = experimentRepository.findById(id);
        if (exp != null) {
            return ResponseEntity.ok(exp);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/models")
    public List<LlmModel> getModels() {
        return modelRepository.findAll();
    }

    @GetMapping("/techniques")
    public List<PromptTechnique> getTechniques() {
        return techniqueRepository.findAll();
    }

    @PostMapping("/run")
    public ResponseEntity<Experiment> runExperiment(@RequestBody RunRequest request) {
        // Run synchronously to return final status in response
        Experiment exp = pipeline.runExperiment(
                request.getTranscriptId(),
                request.getModelId(),
                request.getTechniqueId(),
                request.isRagEnabled()
        );
        return ResponseEntity.ok(exp);
    }

    // Helper request class
    public static class RunRequest {
        private int transcriptId;
        private int modelId;
        private int techniqueId;
        private boolean ragEnabled;

        public int getTranscriptId() { return transcriptId; }
        public void setTranscriptId(int transcriptId) { this.transcriptId = transcriptId; }

        public int getModelId() { return modelId; }
        public void setModelId(int modelId) { this.modelId = modelId; }

        public int getTechniqueId() { return techniqueId; }
        public void setTechniqueId(int techniqueId) { this.techniqueId = techniqueId; }

        public boolean isRagEnabled() { return ragEnabled; }
        public void setRagEnabled(boolean ragEnabled) { this.ragEnabled = ragEnabled; }
    }
}
