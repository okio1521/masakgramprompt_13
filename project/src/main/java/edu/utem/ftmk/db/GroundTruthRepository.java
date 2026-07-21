package edu.utem.ftmk.db;

import edu.utem.ftmk.model.IngredientResult;
import edu.utem.ftmk.model.NutritionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class GroundTruthRepository {

    private final DatabaseConnection dbConn;

    @Autowired
    public GroundTruthRepository(DatabaseConnection dbConn) {
        this.dbConn = dbConn;
    }

    public NutritionResult findByTranscriptId(int transcriptId) {
        String sql = "SELECT * FROM ground_truth_reel WHERE transcript_id = ?";

        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, transcriptId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NutritionResult nr = new NutritionResult();
                    nr.setResultId(rs.getInt("gt_reel_id"));
                    nr.setRecipeName(rs.getString("recipe_name"));
                    nr.setServingsEstimated(rs.getInt("servings"));

                    nr.setIngredients(findIngredientsByGtReelId(nr.getResultId()));
                    calculateTotals(nr);

                    return nr;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private List<IngredientResult> findIngredientsByGtReelId(int gtReelId) {
        List<IngredientResult> list = new ArrayList<>();
        String sql = "SELECT * FROM ground_truth_ingredient WHERE gt_reel_id = ? AND annotation_layer = 'layer1'";

        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, gtReelId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    IngredientResult ing = new IngredientResult();
                    ing.setIngredientId(rs.getInt("gt_ingredient_id"));
                    ing.setResultId(rs.getInt("gt_reel_id"));
                    ing.setNameOriginal(rs.getString("name_original"));
                    ing.setNameEn(rs.getString("name_en"));
                    ing.setQuantityValue(getFloatOrNull(rs, "quantity_value"));
                    ing.setUnitOriginal(rs.getString("unit_original"));
                    ing.setUnitEn(rs.getString("unit_en"));
                    ing.setEstimatedWeightG(getFloatOrNull(rs, "estimated_weight_g"));

                    ing.setCalories(getFloatOrNull(rs, "calories"));
                    ing.setTotalFatG(getFloatOrNull(rs, "total_fat_g"));
                    ing.setSaturatedFatG(getFloatOrNull(rs, "saturated_fat_g"));
                    ing.setCholesterolMg(getFloatOrNull(rs, "cholesterol_mg"));
                    ing.setSodiumMg(getFloatOrNull(rs, "sodium_mg"));
                    ing.setTotalCarbohydrateG(getFloatOrNull(rs, "total_carbohydrate_g"));
                    ing.setDietaryFiberG(getFloatOrNull(rs, "dietary_fiber_g"));
                    ing.setTotalSugarsG(getFloatOrNull(rs, "total_sugars_g"));
                    ing.setProteinG(getFloatOrNull(rs, "protein_g"));
                    ing.setVitaminDMcg(getFloatOrNull(rs, "vitamin_d_mcg"));
                    ing.setCalciumMg(getFloatOrNull(rs, "calcium_mg"));
                    ing.setIronMg(getFloatOrNull(rs, "iron_mg"));
                    ing.setPotassiumMg(getFloatOrNull(rs, "potassium_mg"));
                    ing.setIsHallucinated(false);

                    list.add(ing);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    private void calculateTotals(NutritionResult nr) {
        float calories = 0, fat = 0, satFat = 0, chol = 0, sod = 0, carb = 0,
              fiber = 0, sugars = 0, prot = 0, vitd = 0, calc = 0, iron = 0, pot = 0;

        for (IngredientResult ing : nr.getIngredients()) {
            calories += ing.getCalories() != null ? ing.getCalories() : 0;
            fat += ing.getTotalFatG() != null ? ing.getTotalFatG() : 0;
            satFat += ing.getSaturatedFatG() != null ? ing.getSaturatedFatG() : 0;
            chol += ing.getCholesterolMg() != null ? ing.getCholesterolMg() : 0;
            sod += ing.getSodiumMg() != null ? ing.getSodiumMg() : 0;
            carb += ing.getTotalCarbohydrateG() != null ? ing.getTotalCarbohydrateG() : 0;
            fiber += ing.getDietaryFiberG() != null ? ing.getDietaryFiberG() : 0;
            sugars += ing.getTotalSugarsG() != null ? ing.getTotalSugarsG() : 0;
            prot += ing.getProteinG() != null ? ing.getProteinG() : 0;
            vitd += ing.getVitaminDMcg() != null ? ing.getVitaminDMcg() : 0;
            calc += ing.getCalciumMg() != null ? ing.getCalciumMg() : 0;
            iron += ing.getIronMg() != null ? ing.getIronMg() : 0;
            pot += ing.getPotassiumMg() != null ? ing.getPotassiumMg() : 0;
        }

        nr.setTotalCalories(calories);
        nr.setTotalFatG(fat);
        nr.setTotalSaturatedFatG(satFat);
        nr.setTotalCholesterolMg(chol);
        nr.setTotalSodiumMg(sod);
        nr.setTotalCarbohydrateG(carb);
        nr.setTotalFiberG(fiber);
        nr.setTotalSugarsG(sugars);
        nr.setTotalProteinG(prot);
        nr.setTotalVitaminDMcg(vitd);
        nr.setTotalCalciumMg(calc);
        nr.setTotalIronMg(iron);
        nr.setTotalPotassiumMg(pot);

        int servings = Math.max(1, nr.getServingsEstimated());
        nr.setServingCalories(calories / servings);
        nr.setServingTotalFatG(fat / servings);
        nr.setServingSaturatedFatG(satFat / servings);
        nr.setServingCholesterolMg(chol / servings);
        nr.setServingSodiumMg(sod / servings);
        nr.setServingCarbohydrateG(carb / servings);
        nr.setServingFiberG(fiber / servings);
        nr.setServingSugarsG(sugars / servings);
        nr.setServingProteinG(prot / servings);
        nr.setServingVitaminDMcg(vitd / servings);
        nr.setServingCalciumMg(calc / servings);
        nr.setServingIronMg(iron / servings);
        nr.setServingPotassiumMg(pot / servings);
    }

    private Float getFloatOrNull(ResultSet rs, String colName) throws SQLException {
        float f = rs.getFloat(colName);
        return rs.wasNull() ? null : f;
    }
}