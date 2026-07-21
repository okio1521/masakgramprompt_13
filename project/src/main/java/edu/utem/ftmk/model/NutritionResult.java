package edu.utem.ftmk.model;

import java.sql.Timestamp;
import java.util.List;

/**
 * Domain model representing the nutritional analysis result of an experiment.
 * Corresponds to the nutrition_result table.
 */
public class NutritionResult {

    private int       resultId;
    private int       experimentId;

    // Recipe summary
    private String    recipeName;
    private int       servingsEstimated;

    // Amount per serving
    private Float     servingCalories;
    private Float     servingTotalFatG;
    private Float     servingSaturatedFatG;
    private Float     servingCholesterolMg;
    private Float     servingSodiumMg;
    private Float     servingCarbohydrateG;
    private Float     servingFiberG;
    private Float     servingSugarsG;
    private Float     servingProteinG;
    private Float     servingVitaminDMcg;
    private Float     servingCalciumMg;
    private Float     servingIronMg;
    private Float     servingPotassiumMg;

    // Nutrition total (full recipe)
    private Float     totalCalories;
    private Float     totalFatG;
    private Float     totalSaturatedFatG;
    private Float     totalCholesterolMg;
    private Float     totalSodiumMg;
    private Float     totalCarbohydrateG;
    private Float     totalFiberG;
    private Float     totalSugarsG;
    private Float     totalProteinG;
    private Float     totalVitaminDMcg;
    private Float     totalCalciumMg;
    private Float     totalIronMg;
    private Float     totalPotassiumMg;

    // Output metadata
    private String    rawJsonOutput;
    private Boolean   jsonValid;
    private Timestamp createdAt;

    // Nested ingredients
    private List<IngredientResult> ingredients;

    public NutritionResult() {}

    public int getResultId() { return resultId; }
    public void setResultId(int resultId) { this.resultId = resultId; }

    public int getExperimentId() { return experimentId; }
    public void setExperimentId(int experimentId) { this.experimentId = experimentId; }

    public String getRecipeName() { return recipeName; }
    public void setRecipeName(String recipeName) { this.recipeName = recipeName; }

    public int getServingsEstimated() { return servingsEstimated; }
    public void setServingsEstimated(int servingsEstimated) { this.servingsEstimated = servingsEstimated; }

    public Float getServingCalories() { return servingCalories; }
    public void setServingCalories(Float servingCalories) { this.servingCalories = servingCalories; }

    public Float getServingTotalFatG() { return servingTotalFatG; }
    public void setServingTotalFatG(Float servingTotalFatG) { this.servingTotalFatG = servingTotalFatG; }

    public Float getServingSaturatedFatG() { return servingSaturatedFatG; }
    public void setServingSaturatedFatG(Float servingSaturatedFatG) { this.servingSaturatedFatG = servingSaturatedFatG; }

    public Float getServingCholesterolMg() { return servingCholesterolMg; }
    public void setServingCholesterolMg(Float servingCholesterolMg) { this.servingCholesterolMg = servingCholesterolMg; }

    public Float getServingSodiumMg() { return servingSodiumMg; }
    public void setServingSodiumMg(Float servingSodiumMg) { this.servingSodiumMg = servingSodiumMg; }

    public Float getServingCarbohydrateG() { return servingCarbohydrateG; }
    public void setServingCarbohydrateG(Float servingCarbohydrateG) { this.servingCarbohydrateG = servingCarbohydrateG; }

    public Float getServingFiberG() { return servingFiberG; }
    public void setServingFiberG(Float servingFiberG) { this.servingFiberG = servingFiberG; }

    public Float getServingSugarsG() { return servingSugarsG; }
    public void setServingSugarsG(Float servingSugarsG) { this.servingSugarsG = servingSugarsG; }

    public Float getServingProteinG() { return servingProteinG; }
    public void setServingProteinG(Float servingProteinG) { this.servingProteinG = servingProteinG; }

    public Float getServingVitaminDMcg() { return servingVitaminDMcg; }
    public void setServingVitaminDMcg(Float servingVitaminDMcg) { this.servingVitaminDMcg = servingVitaminDMcg; }

    public Float getServingCalciumMg() { return servingCalciumMg; }
    public void setServingCalciumMg(Float servingCalciumMg) { this.servingCalciumMg = servingCalciumMg; }

    public Float getServingIronMg() { return servingIronMg; }
    public void setServingIronMg(Float servingIronMg) { this.servingIronMg = servingIronMg; }

    public Float getServingPotassiumMg() { return servingPotassiumMg; }
    public void setServingPotassiumMg(Float servingPotassiumMg) { this.servingPotassiumMg = servingPotassiumMg; }

    public Float getTotalCalories() { return totalCalories; }
    public void setTotalCalories(Float totalCalories) { this.totalCalories = totalCalories; }

    public Float getTotalFatG() { return totalFatG; }
    public void setTotalFatG(Float totalFatG) { this.totalFatG = totalFatG; }

    public Float getTotalSaturatedFatG() { return totalSaturatedFatG; }
    public void setTotalSaturatedFatG(Float totalSaturatedFatG) { this.totalSaturatedFatG = totalSaturatedFatG; }

    public Float getTotalCholesterolMg() { return totalCholesterolMg; }
    public void setTotalCholesterolMg(Float totalCholesterolMg) { this.totalCholesterolMg = totalCholesterolMg; }

    public Float getTotalSodiumMg() { return totalSodiumMg; }
    public void setTotalSodiumMg(Float totalSodiumMg) { this.totalSodiumMg = totalSodiumMg; }

    public Float getTotalCarbohydrateG() { return totalCarbohydrateG; }
    public void setTotalCarbohydrateG(Float totalCarbohydrateG) { this.totalCarbohydrateG = totalCarbohydrateG; }

    public Float getTotalFiberG() { return totalFiberG; }
    public void setTotalFiberG(Float totalFiberG) { this.totalFiberG = totalFiberG; }

    public Float getTotalSugarsG() { return totalSugarsG; }
    public void setTotalSugarsG(Float totalSugarsG) { this.totalSugarsG = totalSugarsG; }

    public Float getTotalProteinG() { return totalProteinG; }
    public void setTotalProteinG(Float totalProteinG) { this.totalProteinG = totalProteinG; }

    public Float getTotalVitaminDMcg() { return totalVitaminDMcg; }
    public void setTotalVitaminDMcg(Float totalVitaminDMcg) { this.totalVitaminDMcg = totalVitaminDMcg; }

    public Float getTotalCalciumMg() { return totalCalciumMg; }
    public void setTotalCalciumMg(Float totalCalciumMg) { this.totalCalciumMg = totalCalciumMg; }

    public Float getTotalIronMg() { return totalIronMg; }
    public void setTotalIronMg(Float totalIronMg) { this.totalIronMg = totalIronMg; }

    public Float getTotalPotassiumMg() { return totalPotassiumMg; }
    public void setTotalPotassiumMg(Float totalPotassiumMg) { this.totalPotassiumMg = totalPotassiumMg; }

    public String getRawJsonOutput() { return rawJsonOutput; }
    public void setRawJsonOutput(String rawJsonOutput) { this.rawJsonOutput = rawJsonOutput; }

    public Boolean getJsonValid() { return jsonValid; }
    public void setJsonValid(Boolean jsonValid) { this.jsonValid = jsonValid; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public List<IngredientResult> getIngredients() { return ingredients; }
    public void setIngredients(List<IngredientResult> ingredients) { this.ingredients = ingredients; }
}
