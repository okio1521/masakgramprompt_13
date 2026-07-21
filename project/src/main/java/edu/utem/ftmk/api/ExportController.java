package edu.utem.ftmk.api;

import edu.utem.ftmk.db.DatabaseConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

/**
 * Controller to export any database table to a standard CSV format.
 * Dynamic generation based on ResultSetMetaData.
 */
@RestController
@RequestMapping("/api/export")
@CrossOrigin(origins = "*")
public class ExportController {

    private final DatabaseConnection dbConn;

    // Allowed tables to prevent SQL injection issues
    private static final List<String> ALLOWED_TABLES = Arrays.asList(
            "influencer", "reel", "audio_file", "transcript",
            "ground_truth_reel", "ground_truth_ingredient",
            "llm_model", "prompt_technique", "experiment",
            "nutrition_result", "ingredient_result"
    );

    @Autowired
    public ExportController(DatabaseConnection dbConn) {
        this.dbConn = dbConn;
    }

    /**
     * Exports the requested table as a CSV attachment.
     */
    @GetMapping("/{tableName}")
    public ResponseEntity<String> exportTableToCsv(@PathVariable String tableName) {
        String targetTable = tableName.trim().toLowerCase();
        if (!ALLOWED_TABLES.contains(targetTable)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid table name. Allowed tables: " + ALLOWED_TABLES);
        }

        StringBuilder csv = new StringBuilder();
        String sql = "SELECT * FROM " + targetTable;

        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            // 1. Write Header Row
            for (int i = 1; i <= colCount; i++) {
                csv.append(escapeCsvValue(meta.getColumnName(i)));
                if (i < colCount) {
                    csv.append(",");
                }
            }
            csv.append("\n");

            // 2. Write Data Rows
            while (rs.next()) {
                for (int i = 1; i <= colCount; i++) {
                    Object val = rs.getObject(i);
                    csv.append(escapeCsvValue(val));
                    if (i < colCount) {
                        csv.append(",");
                    }
                }
                csv.append("\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("SQL database export failure: " + e.getMessage());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + targetTable + ".csv");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");

        return new ResponseEntity<>(csv.toString(), headers, HttpStatus.OK);
    }

    /**
     * Exports evaluation layers as CSV.
     */
    @GetMapping("/layers/{layerKey}")
    public ResponseEntity<String> exportLayerToCsv(@PathVariable String layerKey) {
        String targetLayer = layerKey.trim().toLowerCase();
        
        if ("layer4_human_evaluation".equals(targetLayer)) {
            String header = "evaluation_id,result_id,experiment_id,video_id,model_name,technique_name,annotator_id,fluency_score,completeness_score,plausibility_score,evaluated_at\n";
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + targetLayer + ".csv");
            headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");
            return new ResponseEntity<>(header, headers, HttpStatus.OK);
        }

        String sql = getSqlForLayer(targetLayer);
        if (sql.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid layer key: " + layerKey);
        }

        StringBuilder csv = new StringBuilder();

        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            // 1. Write Header Row
            for (int i = 1; i <= colCount; i++) {
                csv.append(escapeCsvValue(meta.getColumnLabel(i)));
                if (i < colCount) {
                    csv.append(",");
                }
            }
            csv.append("\n");

            // 2. Write Data Rows
            while (rs.next()) {
                for (int i = 1; i <= colCount; i++) {
                    Object val = rs.getObject(i);
                    csv.append(escapeCsvValue(val));
                    if (i < colCount) {
                        csv.append(",");
                    }
                }
                csv.append("\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("SQL database export failure: " + e.getMessage());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + targetLayer + ".csv");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");

        return new ResponseEntity<>(csv.toString(), headers, HttpStatus.OK);
    }

    private String getSqlForLayer(String fileKey) {
        switch (fileKey) {
            case "layer1a_exact_match":
                return "SELECT e.experiment_id, e.transcript_id, t.reel_id AS video_id, m.model_name, pt.technique_name, e.rag_enabled, " +
                       "gti.name_original AS gt_name_original, gti.name_en AS gt_name_en, gti.unit_original AS gt_unit_original, gti.unit_en AS gt_unit_en, " +
                       "ir.name_original AS pred_name_original, ir.name_en AS pred_name_en, ir.unit_original AS pred_unit_original, ir.unit_en AS pred_unit_en " +
                       "FROM experiment e " +
                       "JOIN transcript t               ON e.transcript_id     = t.transcript_id " +
                       "JOIN llm_model m                ON e.model_id          = m.model_id " +
                       "JOIN prompt_technique pt        ON e.technique_id      = pt.technique_id " +
                       "JOIN nutrition_result nr        ON e.experiment_id     = nr.experiment_id " +
                       "JOIN ingredient_result ir       ON nr.result_id        = ir.result_id " +
                       "JOIN ground_truth_reel gtr      ON t.transcript_id     = gtr.transcript_id " +
                       "LEFT JOIN ground_truth_ingredient gti ON gtr.gt_reel_id = gti.gt_reel_id " +
                       "AND (LOWER(TRIM(ir.name_original)) = LOWER(TRIM(gti.name_original)) " +
                       "OR LOWER(TRIM(ir.name_en)) = LOWER(TRIM(gti.name_en)) " +
                       "OR LOWER(TRIM(ir.name_en)) = LOWER(TRIM(gti.name_original)) " +
                       "OR LOWER(TRIM(ir.name_original)) = LOWER(TRIM(gti.name_en))) " +
                       "WHERE e.status = 'completed' " +
                       "ORDER BY e.experiment_id, ir.ingredient_id";

            case "layer1b_text_similarity":
                return "SELECT e.experiment_id, t.reel_id AS video_id, m.model_name, pt.technique_name, e.rag_enabled, " +
                       "gti.name_original AS gt_name_original, gti.name_en AS gt_name_en, " +
                       "ir.name_original AS pred_name_original, ir.name_en AS pred_name_en " +
                       "FROM experiment e " +
                       "JOIN transcript t               ON e.transcript_id     = t.transcript_id " +
                       "JOIN llm_model m                ON e.model_id          = m.model_id " +
                       "JOIN prompt_technique pt        ON e.technique_id      = pt.technique_id " +
                       "JOIN nutrition_result nr        ON e.experiment_id     = nr.experiment_id " +
                       "JOIN ingredient_result ir       ON nr.result_id        = ir.result_id " +
                       "JOIN ground_truth_reel gtr      ON t.transcript_id     = gtr.transcript_id " +
                       "LEFT JOIN ground_truth_ingredient gti ON gtr.gt_reel_id = gti.gt_reel_id " +
                       "AND (LOWER(TRIM(ir.name_original)) = LOWER(TRIM(gti.name_original)) " +
                       "OR LOWER(TRIM(ir.name_en)) = LOWER(TRIM(gti.name_en)) " +
                       "OR LOWER(TRIM(ir.name_en)) = LOWER(TRIM(gti.name_original)) " +
                       "OR LOWER(TRIM(ir.name_original)) = LOWER(TRIM(gti.name_en))) " +
                       "WHERE e.status = 'completed' " +
                       "ORDER BY e.experiment_id, ir.ingredient_id";

            case "layer2a_numeric_quantity":
                return "SELECT e.experiment_id, t.reel_id AS video_id, m.model_name, pt.technique_name, e.rag_enabled, " +
                       "gti.quantity_value AS gt_quantity_value, gti.estimated_weight_g AS gt_weight_g, " +
                       "ir.quantity_value AS pred_quantity_value, ir.estimated_weight_g AS pred_weight_g " +
                       "FROM experiment e " +
                       "JOIN transcript t               ON e.transcript_id     = t.transcript_id " +
                       "JOIN llm_model m                ON e.model_id          = m.model_id " +
                       "JOIN prompt_technique pt        ON e.technique_id      = pt.technique_id " +
                       "JOIN nutrition_result nr        ON e.experiment_id     = nr.experiment_id " +
                       "JOIN ingredient_result ir       ON nr.result_id        = ir.result_id " +
                       "JOIN ground_truth_reel gtr      ON t.transcript_id     = gtr.transcript_id " +
                       "LEFT JOIN ground_truth_ingredient gti ON gtr.gt_reel_id = gti.gt_reel_id " +
                       "AND (LOWER(TRIM(ir.name_original)) = LOWER(TRIM(gti.name_original)) " +
                       "OR LOWER(TRIM(ir.name_en)) = LOWER(TRIM(gti.name_en)) " +
                       "OR LOWER(TRIM(ir.name_en)) = LOWER(TRIM(gti.name_original)) " +
                       "OR LOWER(TRIM(ir.name_original)) = LOWER(TRIM(gti.name_en))) " +
                       "WHERE e.status = 'completed' " +
                       "ORDER BY e.experiment_id, ir.ingredient_id";

            case "layer2b_numeric_nutrition":
                return "SELECT e.experiment_id, t.reel_id AS video_id, m.model_name, pt.technique_name, e.rag_enabled, " +
                       "gti.calories AS gt_energy_kcal, gti.protein_g AS gt_protein_g, gti.total_fat_g AS gt_fat_g, gti.total_carbohydrate_g AS gt_carbohydrate_g, " +
                       "ir.calories AS pred_energy_kcal, ir.protein_g AS pred_protein_g, ir.total_fat_g AS pred_fat_g, ir.total_carbohydrate_g AS pred_carbohydrate_g " +
                       "FROM experiment e " +
                       "JOIN transcript t               ON e.transcript_id     = t.transcript_id " +
                       "JOIN llm_model m                ON e.model_id          = m.model_id " +
                       "JOIN prompt_technique pt        ON e.technique_id      = pt.technique_id " +
                       "JOIN nutrition_result nr        ON e.experiment_id     = nr.experiment_id " +
                       "JOIN ingredient_result ir       ON nr.result_id        = ir.result_id " +
                       "JOIN ground_truth_reel gtr      ON t.transcript_id     = gtr.transcript_id " +
                       "LEFT JOIN ground_truth_ingredient gti ON gtr.gt_reel_id = gti.gt_reel_id " +
                       "AND (LOWER(TRIM(ir.name_original)) = LOWER(TRIM(gti.name_original)) " +
                       "OR LOWER(TRIM(ir.name_en)) = LOWER(TRIM(gti.name_en)) " +
                       "OR LOWER(TRIM(ir.name_en)) = LOWER(TRIM(gti.name_original)) " +
                       "OR LOWER(TRIM(ir.name_original)) = LOWER(TRIM(gti.name_en))) " +
                       "WHERE e.status = 'completed' " +
                       "ORDER BY e.experiment_id, ir.ingredient_id";

            case "layer2c_nutrition_totals":
                return "SELECT e.experiment_id, t.reel_id AS video_id, m.model_name, pt.technique_name, e.rag_enabled, " +
                       "(SELECT SUM(gti.calories) FROM ground_truth_ingredient gti JOIN ground_truth_reel gtr ON gti.gt_reel_id = gtr.gt_reel_id WHERE gtr.transcript_id = e.transcript_id) AS gt_total_energy_kcal, " +
                       "(SELECT SUM(gti.protein_g) FROM ground_truth_ingredient gti JOIN ground_truth_reel gtr ON gti.gt_reel_id = gtr.gt_reel_id WHERE gtr.transcript_id = e.transcript_id) AS gt_total_protein_g, " +
                       "(SELECT SUM(gti.total_fat_g) FROM ground_truth_ingredient gti JOIN ground_truth_reel gtr ON gti.gt_reel_id = gtr.gt_reel_id WHERE gtr.transcript_id = e.transcript_id) AS gt_total_fat_g, " +
                       "(SELECT SUM(gti.total_carbohydrate_g) FROM ground_truth_ingredient gti JOIN ground_truth_reel gtr ON gti.gt_reel_id = gtr.gt_reel_id WHERE gtr.transcript_id = e.transcript_id) AS gt_total_carbohydrate_g, " +
                       "nr.total_calories AS pred_total_energy_kcal, nr.total_protein_g AS pred_total_protein_g, nr.total_fat_g AS pred_total_fat_g, nr.total_carbohydrate_g AS pred_total_carbohydrate_g " +
                       "FROM experiment e " +
                       "JOIN transcript t               ON e.transcript_id     = t.transcript_id " +
                       "JOIN llm_model m                ON e.model_id          = m.model_id " +
                       "JOIN prompt_technique pt        ON e.technique_id      = pt.technique_id " +
                       "JOIN nutrition_result nr        ON e.experiment_id     = nr.experiment_id " +
                       "WHERE e.status = 'completed' " +
                       "GROUP BY e.experiment_id, t.reel_id, m.model_name, pt.technique_name, e.rag_enabled, " +
                       "nr.total_calories, nr.total_protein_g, nr.total_fat_g, nr.total_carbohydrate_g " +
                       "ORDER BY e.experiment_id";

            case "layer3a_json_validity":
                return "SELECT m.model_name, pt.technique_name, e.rag_enabled, COUNT(*) AS total_runs, " +
                       "SUM(CASE WHEN nr.json_valid = TRUE THEN 1 ELSE 0 END) AS valid_count, " +
                       "SUM(CASE WHEN nr.json_valid = FALSE THEN 1 ELSE 0 END) AS invalid_count, " +
                       "ROUND(SUM(CASE WHEN nr.json_valid = TRUE THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) AS validity_rate_pct " +
                       "FROM experiment e " +
                       "JOIN llm_model m           ON e.model_id      = m.model_id " +
                       "JOIN prompt_technique pt   ON e.technique_id  = pt.technique_id " +
                       "JOIN nutrition_result nr   ON e.experiment_id = nr.experiment_id " +
                       "WHERE e.status = 'completed' " +
                       "GROUP BY m.model_name, pt.technique_name, e.rag_enabled " +
                       "ORDER BY m.model_name, pt.technique_name";

            case "layer3b_hallucination":
                return "SELECT e.experiment_id, t.reel_id AS video_id, m.model_name, pt.technique_name, e.rag_enabled, " +
                       "ir.name_original AS pred_name_original, ir.name_en AS pred_name_en, ir.is_hallucinated " +
                       "FROM experiment e " +
                       "JOIN transcript t           ON e.transcript_id  = t.transcript_id " +
                       "JOIN llm_model m            ON e.model_id       = m.model_id " +
                       "JOIN prompt_technique pt    ON e.technique_id   = pt.technique_id " +
                       "JOIN nutrition_result nr    ON e.experiment_id  = nr.experiment_id " +
                       "JOIN ingredient_result ir   ON nr.result_id     = ir.result_id " +
                       "WHERE e.status = 'completed' " +
                       "ORDER BY e.experiment_id, ir.ingredient_id";

            case "layer3c_ingredient_detection":
                return "SELECT e.experiment_id, t.reel_id AS video_id, m.model_name, pt.technique_name, e.rag_enabled, " +
                       "(SELECT COUNT(*) FROM ground_truth_ingredient gti JOIN ground_truth_reel gtr ON gti.gt_reel_id = gtr.gt_reel_id WHERE gtr.transcript_id = e.transcript_id) AS gt_ingredient_count, " +
                       "COUNT(DISTINCT ir.ingredient_id) AS pred_ingredient_count, " +
                       "SUM(CASE WHEN ir.is_hallucinated = FALSE THEN 1 ELSE 0 END) AS true_positives, " +
                       "SUM(CASE WHEN ir.is_hallucinated = TRUE THEN 1 ELSE 0 END) AS false_positives " +
                       "FROM experiment e " +
                       "JOIN transcript t               ON e.transcript_id     = t.transcript_id " +
                       "JOIN llm_model m                ON e.model_id          = m.model_id " +
                       "JOIN prompt_technique pt        ON e.technique_id      = pt.technique_id " +
                       "JOIN nutrition_result nr        ON e.experiment_id     = nr.experiment_id " +
                       "LEFT JOIN ingredient_result ir  ON nr.result_id        = ir.result_id " +
                       "WHERE e.status = 'completed' " +
                       "GROUP BY e.experiment_id, t.reel_id, m.model_name, pt.technique_name, e.rag_enabled " +
                       "ORDER BY e.experiment_id";

            case "layer5_condition_scores":
                return "SELECT t.reel_id AS video_id, m.model_name, pt.technique_name, e.rag_enabled, " +
                       "COUNT(DISTINCT ir.ingredient_id) AS pred_count, " +
                       "SUM(CASE WHEN ir.is_hallucinated = FALSE THEN 1 ELSE 0 END) AS true_positives, " +
                       "SUM(CASE WHEN ir.is_hallucinated = TRUE THEN 1 ELSE 0 END) AS false_positives, " +
                       "(SELECT COUNT(*) FROM ground_truth_ingredient gti JOIN ground_truth_reel gtr ON gti.gt_reel_id = gtr.gt_reel_id WHERE gtr.transcript_id = e.transcript_id) AS gt_count, " +
                       "nr.json_valid, " +
                       "nr.total_calories AS pred_total_kcal, " +
                       "(SELECT SUM(gti.calories) FROM ground_truth_ingredient gti JOIN ground_truth_reel gtr ON gti.gt_reel_id = gtr.gt_reel_id WHERE gtr.transcript_id = e.transcript_id) AS gt_total_kcal " +
                       "FROM experiment e " +
                       "JOIN transcript t               ON e.transcript_id     = t.transcript_id " +
                       "JOIN llm_model m                ON e.model_id          = m.model_id " +
                       "JOIN prompt_technique pt        ON e.technique_id      = pt.technique_id " +
                       "JOIN nutrition_result nr        ON e.experiment_id     = nr.experiment_id " +
                       "LEFT JOIN ingredient_result ir  ON nr.result_id        = ir.result_id " +
                       "WHERE e.status = 'completed' " +
                       "GROUP BY t.reel_id, m.model_name, pt.technique_name, e.rag_enabled, nr.json_valid, nr.total_calories " +
                       "ORDER BY t.reel_id, m.model_name, pt.technique_name";

            default:
                return "";
        }
    }

    /**
     * Helper to properly escape fields with double quotes, newlines, or commas for CSV format.
     */
    private String escapeCsvValue(Object value) {
        if (value == null) {
            return "";
        }
        String str = value.toString();
        if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            str = str.replace("\"", "\"\"");
            return "\"" + str + "\"";
        }
        return str;
    }
}
