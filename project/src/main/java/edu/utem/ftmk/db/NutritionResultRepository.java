package edu.utem.ftmk.db;

import edu.utem.ftmk.model.NutritionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
public class NutritionResultRepository {

    private final DatabaseConnection dbConn;

    @Autowired
    public NutritionResultRepository(DatabaseConnection dbConn) {
        this.dbConn = dbConn;
    }

    public int save(NutritionResult nr) {
        String sql = "INSERT INTO nutrition_result (" +
                "experiment_id, recipe_name, servings_estimated, " +
                "serving_calories, serving_total_fat_g, serving_saturated_fat_g, " +
                "serving_cholesterol_mg, serving_sodium_mg, serving_carbohydrate_g, " +
                "serving_fiber_g, serving_sugars_g, serving_protein_g, " +
                "serving_vitamin_d_mcg, serving_calcium_mg, serving_iron_mg, serving_potassium_mg, " +
                "total_calories, total_fat_g, total_saturated_fat_g, " +
                "total_cholesterol_mg, total_sodium_mg, total_carbohydrate_g, " +
                "total_fiber_g, total_sugars_g, total_protein_g, " +
                "total_vitamin_d_mcg, total_calcium_mg, total_iron_mg, total_potassium_mg, " +
                "raw_json_output, json_valid" +
                ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, nr.getExperimentId());
            ps.setString(2, nr.getRecipeName());
            ps.setInt(3, nr.getServingsEstimated());

            setFloatOrNull(ps, 4, nr.getServingCalories());
            setFloatOrNull(ps, 5, nr.getServingTotalFatG());
            setFloatOrNull(ps, 6, nr.getServingSaturatedFatG());
            setFloatOrNull(ps, 7, nr.getServingCholesterolMg());
            setFloatOrNull(ps, 8, nr.getServingSodiumMg());
            setFloatOrNull(ps, 9, nr.getServingCarbohydrateG());
            setFloatOrNull(ps, 10, nr.getServingFiberG());
            setFloatOrNull(ps, 11, nr.getServingSugarsG());
            setFloatOrNull(ps, 12, nr.getServingProteinG());
            setFloatOrNull(ps, 13, nr.getServingVitaminDMcg());
            setFloatOrNull(ps, 14, nr.getServingCalciumMg());
            setFloatOrNull(ps, 15, nr.getServingIronMg());
            setFloatOrNull(ps, 16, nr.getServingPotassiumMg());

            setFloatOrNull(ps, 17, nr.getTotalCalories());
            setFloatOrNull(ps, 18, nr.getTotalFatG());
            setFloatOrNull(ps, 19, nr.getTotalSaturatedFatG());
            setFloatOrNull(ps, 20, nr.getTotalCholesterolMg());
            setFloatOrNull(ps, 21, nr.getTotalSodiumMg());
            setFloatOrNull(ps, 22, nr.getTotalCarbohydrateG());
            setFloatOrNull(ps, 23, nr.getTotalFiberG());
            setFloatOrNull(ps, 24, nr.getTotalSugarsG());
            setFloatOrNull(ps, 25, nr.getTotalProteinG());
            setFloatOrNull(ps, 26, nr.getTotalVitaminDMcg());
            setFloatOrNull(ps, 27, nr.getTotalCalciumMg());
            setFloatOrNull(ps, 28, nr.getTotalIronMg());
            setFloatOrNull(ps, 29, nr.getTotalPotassiumMg());

            ps.setString(30, nr.getRawJsonOutput());
            if (nr.getJsonValid() != null) {
                ps.setBoolean(31, nr.getJsonValid());
            } else {
                ps.setNull(31, Types.BOOLEAN);
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    nr.setResultId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public NutritionResult findByExperimentId(int experimentId) {
        String sql = "SELECT * FROM nutrition_result WHERE experiment_id = ?";

        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, experimentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NutritionResult nr = new NutritionResult();
                    nr.setResultId(rs.getInt("result_id"));
                    nr.setExperimentId(rs.getInt("experiment_id"));
                    nr.setRecipeName(rs.getString("recipe_name"));
                    nr.setServingsEstimated(rs.getInt("servings_estimated"));

                    nr.setServingCalories(getFloatOrNull(rs, "serving_calories"));
                    nr.setServingTotalFatG(getFloatOrNull(rs, "serving_total_fat_g"));
                    nr.setServingSaturatedFatG(getFloatOrNull(rs, "serving_saturated_fat_g"));
                    nr.setServingCholesterolMg(getFloatOrNull(rs, "serving_cholesterol_mg"));
                    nr.setServingSodiumMg(getFloatOrNull(rs, "serving_sodium_mg"));
                    nr.setServingCarbohydrateG(getFloatOrNull(rs, "serving_carbohydrate_g"));
                    nr.setServingFiberG(getFloatOrNull(rs, "serving_fiber_g"));
                    nr.setServingSugarsG(getFloatOrNull(rs, "serving_sugars_g"));
                    nr.setServingProteinG(getFloatOrNull(rs, "serving_protein_g"));
                    nr.setServingVitaminDMcg(getFloatOrNull(rs, "serving_vitamin_d_mcg"));
                    nr.setServingCalciumMg(getFloatOrNull(rs, "serving_calcium_mg"));
                    nr.setServingIronMg(getFloatOrNull(rs, "serving_iron_mg"));
                    nr.setServingPotassiumMg(getFloatOrNull(rs, "serving_potassium_mg"));

                    nr.setTotalCalories(getFloatOrNull(rs, "total_calories"));
                    nr.setTotalFatG(getFloatOrNull(rs, "total_fat_g"));
                    nr.setTotalSaturatedFatG(getFloatOrNull(rs, "total_saturated_fat_g"));
                    nr.setTotalCholesterolMg(getFloatOrNull(rs, "total_cholesterol_mg"));
                    nr.setTotalSodiumMg(getFloatOrNull(rs, "total_sodium_mg"));
                    nr.setTotalCarbohydrateG(getFloatOrNull(rs, "total_carbohydrate_g"));
                    nr.setTotalFiberG(getFloatOrNull(rs, "total_fiber_g"));
                    nr.setTotalSugarsG(getFloatOrNull(rs, "total_sugars_g"));
                    nr.setTotalProteinG(getFloatOrNull(rs, "total_protein_g"));
                    nr.setTotalVitaminDMcg(getFloatOrNull(rs, "total_vitamin_d_mcg"));
                    nr.setTotalCalciumMg(getFloatOrNull(rs, "total_calcium_mg"));
                    nr.setTotalIronMg(getFloatOrNull(rs, "total_iron_mg"));
                    nr.setTotalPotassiumMg(getFloatOrNull(rs, "total_potassium_mg"));

                    nr.setRawJsonOutput(rs.getString("raw_json_output"));

                    boolean jsonValid = rs.getBoolean("json_valid");
                    nr.setJsonValid(rs.wasNull() ? null : jsonValid);

                    nr.setCreatedAt(rs.getTimestamp("created_at"));
                    return nr;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void deleteByExperimentId(int experimentId) {
        String deleteIngredientsSql = "DELETE FROM ingredient_result WHERE result_id = (SELECT result_id FROM nutrition_result WHERE experiment_id = ?)";
        String deleteResultSql = "DELETE FROM nutrition_result WHERE experiment_id = ?";

        try (Connection conn = dbConn.getConnection()) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement ps = conn.prepareStatement(deleteIngredientsSql)) {
                    ps.setInt(1, experimentId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(deleteResultSql)) {
                    ps.setInt(1, experimentId);
                    ps.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setFloatOrNull(PreparedStatement ps, int paramIndex, Float value) throws SQLException {
        if (value != null) {
            ps.setFloat(paramIndex, value);
        } else {
            ps.setNull(paramIndex, Types.FLOAT);
        }
    }

    private Float getFloatOrNull(ResultSet rs, String colName) throws SQLException {
        float f = rs.getFloat(colName);
        return rs.wasNull() ? null : f;
    }
}