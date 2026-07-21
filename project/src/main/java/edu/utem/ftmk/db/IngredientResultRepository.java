package edu.utem.ftmk.db;

import edu.utem.ftmk.model.IngredientResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class IngredientResultRepository {

    private final DatabaseConnection dbConn;

    @Autowired
    public IngredientResultRepository(DatabaseConnection dbConn) {
        this.dbConn = dbConn;
    }

    public void saveAll(List<IngredientResult> ingredients) {
        String sql = "INSERT INTO ingredient_result (" +
                "result_id, name_original, name_en, quantity_value, unit_original, unit_en, estimated_weight_g, " +
                "calories, total_fat_g, saturated_fat_g, cholesterol_mg, sodium_mg, total_carbohydrate_g, " +
                "dietary_fiber_g, total_sugars_g, protein_g, vitamin_d_mcg, calcium_mg, iron_mg, potassium_mg, is_hallucinated" +
                ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (IngredientResult ing : ingredients) {
                ps.setInt(1, ing.getResultId());
                ps.setString(2, ing.getNameOriginal());
                ps.setString(3, ing.getNameEn());

                setFloatOrNull(ps, 4, ing.getQuantityValue());
                ps.setString(5, ing.getUnitOriginal());
                ps.setString(6, ing.getUnitEn());
                setFloatOrNull(ps, 7, ing.getEstimatedWeightG());

                setFloatOrNull(ps, 8, ing.getCalories());
                setFloatOrNull(ps, 9, ing.getTotalFatG());
                setFloatOrNull(ps, 10, ing.getSaturatedFatG());
                setFloatOrNull(ps, 11, ing.getCholesterolMg());
                setFloatOrNull(ps, 12, ing.getSodiumMg());
                setFloatOrNull(ps, 13, ing.getTotalCarbohydrateG());
                setFloatOrNull(ps, 14, ing.getDietaryFiberG());
                setFloatOrNull(ps, 15, ing.getTotalSugarsG());
                setFloatOrNull(ps, 16, ing.getProteinG());
                setFloatOrNull(ps, 17, ing.getVitaminDMcg());
                setFloatOrNull(ps, 18, ing.getCalciumMg());
                setFloatOrNull(ps, 19, ing.getIronMg());
                setFloatOrNull(ps, 20, ing.getPotassiumMg());

                if (ing.getIsHallucinated() != null) {
                    ps.setBoolean(21, ing.getIsHallucinated());
                } else {
                    ps.setNull(21, Types.BOOLEAN);
                }

                ps.addBatch();
            }

            ps.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<IngredientResult> findByResultId(int resultId) {
        List<IngredientResult> list = new ArrayList<>();
        String sql = "SELECT * FROM ingredient_result WHERE result_id = ?";

        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, resultId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    IngredientResult ing = new IngredientResult();
                    ing.setIngredientId(rs.getInt("ingredient_id"));
                    ing.setResultId(rs.getInt("result_id"));
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

                    boolean hallucinated = rs.getBoolean("is_hallucinated");
                    ing.setIsHallucinated(rs.wasNull() ? null : hallucinated);

                    list.add(ing);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public void updateIsHallucinated(int ingredientId, boolean isHallucinated) {
        String sql = "UPDATE ingredient_result SET is_hallucinated = ? WHERE ingredient_id = ?";

        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, isHallucinated);
            ps.setInt(2, ingredientId);
            ps.executeUpdate();
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