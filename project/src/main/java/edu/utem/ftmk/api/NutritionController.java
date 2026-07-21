package edu.utem.ftmk.api;

import edu.utem.ftmk.db.GroundTruthRepository;
import edu.utem.ftmk.db.IngredientResultRepository;
import edu.utem.ftmk.db.NutritionResultRepository;
import edu.utem.ftmk.model.NutritionResult;
import edu.utem.ftmk.model.IngredientResult;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nutrition")
@CrossOrigin(origins = "*")
public class NutritionController {

    private final NutritionResultRepository nutritionResultRepository;
    private final IngredientResultRepository ingredientResultRepository;
    private final GroundTruthRepository groundTruthRepository;

    @Autowired
    public NutritionController(NutritionResultRepository nutritionResultRepository,
                               IngredientResultRepository ingredientResultRepository,
                               GroundTruthRepository groundTruthRepository) {
        this.nutritionResultRepository = nutritionResultRepository;
        this.ingredientResultRepository = ingredientResultRepository;
        this.groundTruthRepository = groundTruthRepository;
    }

    @GetMapping("/{experimentId}")
    public ResponseEntity<NutritionResult> getNutritionByExperimentId(@PathVariable int experimentId) {
        NutritionResult result = nutritionResultRepository.findByExperimentId(experimentId);
        if (result != null) {
            // Populate nested ingredients
            result.setIngredients(ingredientResultRepository.findByResultId(result.getResultId()));
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/ground-truth/{transcriptId}")
    public ResponseEntity<NutritionResult> getGroundTruthByTranscriptId(@PathVariable int transcriptId) {
        NutritionResult result = groundTruthRepository.findByTranscriptId(transcriptId);
        if (result != null) {
            // In GroundTruthRepository findByTranscriptId, ingredients are already populated.
            // But we double check to ensure completeness
            if (result.getIngredients() == null || result.getIngredients().isEmpty()) {
                result.setIngredients(ingredientResultRepository.findByResultId(result.getResultId()));
            }
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/ingredients/{resultId}")
    public ResponseEntity<List<IngredientResult>> getIngredientsByResultId(@PathVariable int resultId) {
        return ResponseEntity.ok(ingredientResultRepository.findByResultId(resultId));
    }
}
