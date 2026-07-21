-- ============================================================
-- FILE    : metrics_evaluation_queries.sql
-- PROJECT : Nutritional LLM Analysis
-- PURPOSE : SQL queries for metric computation per evaluation
--           layer. Each query produces one CSV export that
--           feeds directly into evaluate.py.
--           These queries match the actual database schema
--           and are used by ExportPanel.java in the Swing UI.
-- AUTHOR  : EMALIANA KASMURI
-- ============================================================


-- ============================================================
-- LAYER 1A : EXACT MATCH (EM)
-- OUTPUT FILE : layer1a_exact_match.csv
-- PURPOSE : Extracts predicted vs ground truth ingredient name
--           and unit fields for exact match computation.
--           One row per ingredient pair per experiment.
-- ============================================================
SELECT
    e.experiment_id,
    e.transcript_id,
    t.reel_id                AS video_id,
    m.model_name,
    pt.technique_name,
    e.rag_enabled,

    -- Ground truth fields (reference)
    gti.name_original        AS gt_name_original,
    gti.name_en              AS gt_name_en,
    gti.unit_original        AS gt_unit_original,
    gti.unit_en              AS gt_unit_en,

    -- LLM predicted fields
    ir.name_original         AS pred_name_original,
    ir.name_en               AS pred_name_en,
    ir.unit_original         AS pred_unit_original,
    ir.unit_en               AS pred_unit_en

FROM experiment e
JOIN transcript t               ON e.transcript_id     = t.transcript_id
JOIN llm_model m                ON e.model_id          = m.model_id
JOIN prompt_technique pt        ON e.technique_id      = pt.technique_id
JOIN nutrition_result nr        ON e.experiment_id     = nr.experiment_id
JOIN ingredient_result ir       ON nr.result_id        = ir.result_id
JOIN ground_truth_reel gtr      ON t.transcript_id     = gtr.transcript_id
LEFT JOIN ground_truth_ingredient gti ON gtr.gt_reel_id = gti.gt_reel_id
    AND (LOWER(TRIM(ir.name_original)) = LOWER(TRIM(gti.name_original))
         OR LOWER(TRIM(ir.name_en)) = LOWER(TRIM(gti.name_en))
         OR LOWER(TRIM(ir.name_en)) = LOWER(TRIM(gti.name_original))
         OR LOWER(TRIM(ir.name_original)) = LOWER(TRIM(gti.name_en)))

WHERE e.status = 'completed'
ORDER BY e.experiment_id, ir.ingredient_id;


-- ============================================================
-- LAYER 1B : FUZZY MATCH & BLEU / ROUGE
-- OUTPUT FILE : layer1b_text_similarity.csv
-- PURPOSE : Extracts all free-text ingredient fields for
--           fuzzy match, BLEU-1, BLEU-2, ROUGE-1, ROUGE-L
--           computation in evaluate.py.
-- ============================================================
SELECT
    e.experiment_id,
    t.reel_id                AS video_id,
    m.model_name,
    pt.technique_name,
    e.rag_enabled,

    -- Ground truth text fields
    gti.name_original        AS gt_name_original,
    gti.name_en              AS gt_name_en,

    -- Predicted text fields
    ir.name_original         AS pred_name_original,
    ir.name_en               AS pred_name_en

FROM experiment e
JOIN transcript t               ON e.transcript_id     = t.transcript_id
JOIN llm_model m                ON e.model_id          = m.model_id
JOIN prompt_technique pt        ON e.technique_id      = pt.technique_id
JOIN nutrition_result nr        ON e.experiment_id     = nr.experiment_id
JOIN ingredient_result ir       ON nr.result_id        = ir.result_id
JOIN ground_truth_reel gtr      ON t.transcript_id     = gtr.transcript_id
LEFT JOIN ground_truth_ingredient gti ON gtr.gt_reel_id = gti.gt_reel_id
    AND (LOWER(TRIM(ir.name_original)) = LOWER(TRIM(gti.name_original))
         OR LOWER(TRIM(ir.name_en)) = LOWER(TRIM(gti.name_en))
         OR LOWER(TRIM(ir.name_en)) = LOWER(TRIM(gti.name_original))
         OR LOWER(TRIM(ir.name_original)) = LOWER(TRIM(gti.name_en)))

WHERE e.status = 'completed'
ORDER BY e.experiment_id, ir.ingredient_id;


-- ============================================================
-- LAYER 2A : MAE & MAPE — QUANTITY & WEIGHT
-- OUTPUT FILE : layer2a_numeric_quantity.csv
-- PURPOSE : Extracts quantity_value and estimated_weight_g
--           pairs for MAE and MAPE computation.
-- ============================================================
SELECT
    e.experiment_id,
    t.reel_id                AS video_id,
    m.model_name,
    pt.technique_name,
    e.rag_enabled,

    -- Ground truth numeric fields
    gti.quantity_value       AS gt_quantity_value,
    gti.estimated_weight_g   AS gt_weight_g,

    -- Predicted numeric fields
    ir.quantity_value        AS pred_quantity_value,
    ir.estimated_weight_g    AS pred_weight_g

FROM experiment e
JOIN transcript t               ON e.transcript_id     = t.transcript_id
JOIN llm_model m                ON e.model_id          = m.model_id
JOIN prompt_technique pt        ON e.technique_id      = pt.technique_id
JOIN nutrition_result nr        ON e.experiment_id     = nr.experiment_id
JOIN ingredient_result ir       ON nr.result_id        = ir.result_id
JOIN ground_truth_reel gtr      ON t.transcript_id     = gtr.transcript_id
LEFT JOIN ground_truth_ingredient gti ON gtr.gt_reel_id = gti.gt_reel_id
    AND (LOWER(TRIM(ir.name_original)) = LOWER(TRIM(gti.name_original))
         OR LOWER(TRIM(ir.name_en)) = LOWER(TRIM(gti.name_en))
         OR LOWER(TRIM(ir.name_en)) = LOWER(TRIM(gti.name_original))
         OR LOWER(TRIM(ir.name_original)) = LOWER(TRIM(gti.name_en)))

WHERE e.status = 'completed'
ORDER BY e.experiment_id, ir.ingredient_id;


-- ============================================================
-- LAYER 2B : MAE, MAPE & PEARSON — NUTRITION VALUES
-- OUTPUT FILE : layer2b_numeric_nutrition.csv
-- PURPOSE : Extracts per-ingredient nutrition values for
--           MAE, MAPE, and Pearson correlation per nutrient
--           per model and prompt technique.
-- ============================================================
SELECT
    e.experiment_id,
    t.reel_id                AS video_id,
    m.model_name,
    pt.technique_name,
    e.rag_enabled,

    -- Ground truth nutrition values
    gti.calories             AS gt_energy_kcal,
    gti.protein_g            AS gt_protein_g,
    gti.total_fat_g          AS gt_fat_g,
    gti.total_carbohydrate_g AS gt_carbohydrate_g,

    -- Predicted nutrition values
    ir.calories              AS pred_energy_kcal,
    ir.protein_g             AS pred_protein_g,
    ir.total_fat_g           AS pred_fat_g,
    ir.total_carbohydrate_g  AS pred_carbohydrate_g

FROM experiment e
JOIN transcript t               ON e.transcript_id     = t.transcript_id
JOIN llm_model m                ON e.model_id          = m.model_id
JOIN prompt_technique pt        ON e.technique_id      = pt.technique_id
JOIN nutrition_result nr        ON e.experiment_id     = nr.experiment_id
JOIN ingredient_result ir       ON nr.result_id        = ir.result_id
JOIN ground_truth_reel gtr      ON t.transcript_id     = gtr.transcript_id
LEFT JOIN ground_truth_ingredient gti ON gtr.gt_reel_id = gti.gt_reel_id
    AND (LOWER(TRIM(ir.name_original)) = LOWER(TRIM(gti.name_original))
         OR LOWER(TRIM(ir.name_en)) = LOWER(TRIM(gti.name_en))
         OR LOWER(TRIM(ir.name_en)) = LOWER(TRIM(gti.name_original))
         OR LOWER(TRIM(ir.name_original)) = LOWER(TRIM(gti.name_en)))

WHERE e.status = 'completed'
ORDER BY e.experiment_id, ir.ingredient_id;


-- ============================================================
-- LAYER 2C : RECIPE-LEVEL NUTRITION TOTALS
-- OUTPUT FILE : layer2c_nutrition_totals.csv
-- PURPOSE : Compares aggregated recipe-level totals between
--           ground truth and LLM output. Used for overall
--           recipe accuracy reporting in the paper.
-- ============================================================
SELECT
    e.experiment_id,
    t.reel_id                AS video_id,
    m.model_name,
    pt.technique_name,
    e.rag_enabled,

    -- Ground truth recipe totals (summed from ingredient level via subqueries to avoid Cartesian multiplication)
    (SELECT SUM(gti.calories) FROM ground_truth_ingredient gti JOIN ground_truth_reel gtr ON gti.gt_reel_id = gtr.gt_reel_id WHERE gtr.transcript_id = e.transcript_id) AS gt_total_energy_kcal,
    (SELECT SUM(gti.protein_g) FROM ground_truth_ingredient gti JOIN ground_truth_reel gtr ON gti.gt_reel_id = gtr.gt_reel_id WHERE gtr.transcript_id = e.transcript_id) AS gt_total_protein_g,
    (SELECT SUM(gti.total_fat_g) FROM ground_truth_ingredient gti JOIN ground_truth_reel gtr ON gti.gt_reel_id = gtr.gt_reel_id WHERE gtr.transcript_id = e.transcript_id) AS gt_total_fat_g,
    (SELECT SUM(gti.total_carbohydrate_g) FROM ground_truth_ingredient gti JOIN ground_truth_reel gtr ON gti.gt_reel_id = gtr.gt_reel_id WHERE gtr.transcript_id = e.transcript_id) AS gt_total_carbohydrate_g,

    -- Predicted recipe totals (stored in nutrition_result)
    nr.total_calories            AS pred_total_energy_kcal,
    nr.total_protein_g           AS pred_total_protein_g,
    nr.total_fat_g               AS pred_total_fat_g,
    nr.total_carbohydrate_g      AS pred_total_carbohydrate_g

FROM experiment e
JOIN transcript t               ON e.transcript_id     = t.transcript_id
JOIN llm_model m                ON e.model_id          = m.model_id
JOIN prompt_technique pt        ON e.technique_id      = pt.technique_id
JOIN nutrition_result nr        ON e.experiment_id     = nr.experiment_id

WHERE e.status = 'completed'
GROUP BY
    e.experiment_id, t.reel_id, m.model_name,
    pt.technique_name, e.rag_enabled,
    nr.total_calories, nr.total_protein_g,
    nr.total_fat_g, nr.total_carbohydrate_g
ORDER BY e.experiment_id;


-- ============================================================
-- LAYER 3A : JSON VALIDITY RATE
-- OUTPUT FILE : layer3a_json_validity.csv
-- PURPOSE : Counts valid vs invalid JSON outputs per model
--           and prompt technique. Computes validity rate
--           as a percentage.
-- ============================================================
SELECT
    m.model_name,
    pt.technique_name,
    e.rag_enabled,
    COUNT(*)                                      AS total_runs,
    SUM(CASE WHEN nr.json_valid = TRUE THEN 1
             ELSE 0 END)                          AS valid_count,
    SUM(CASE WHEN nr.json_valid = FALSE THEN 1
             ELSE 0 END)                          AS invalid_count,
    ROUND(
        SUM(CASE WHEN nr.json_valid = TRUE THEN 1
                 ELSE 0 END) * 100.0 / COUNT(*), 2
    )                                             AS validity_rate_pct

FROM experiment e
JOIN llm_model m           ON e.model_id      = m.model_id
JOIN prompt_technique pt   ON e.technique_id  = pt.technique_id
JOIN nutrition_result nr   ON e.experiment_id = nr.experiment_id

WHERE e.status = 'completed'
GROUP BY m.model_name, pt.technique_name, e.rag_enabled
ORDER BY m.model_name, pt.technique_name;


-- ============================================================
-- LAYER 3B : HALLUCINATION RATE
-- OUTPUT FILE : layer3b_hallucination.csv
-- PURPOSE : Extracts is_hallucinated flag per ingredient
--           result. evaluate.py aggregates into hallucination
--           rate per model and prompt technique.
-- ============================================================
SELECT
    e.experiment_id,
    t.reel_id                AS video_id,
    m.model_name,
    pt.technique_name,
    e.rag_enabled,
    ir.name_original         AS pred_name_original,
    ir.name_en               AS pred_name_en,
    ir.is_hallucinated

FROM experiment e
JOIN transcript t           ON e.transcript_id  = t.transcript_id
JOIN llm_model m            ON e.model_id       = m.model_id
JOIN prompt_technique pt    ON e.technique_id   = pt.technique_id
JOIN nutrition_result nr    ON e.experiment_id  = nr.experiment_id
JOIN ingredient_result ir   ON nr.result_id     = ir.result_id

WHERE e.status = 'completed'
ORDER BY e.experiment_id, ir.ingredient_id;


-- ============================================================
-- LAYER 3C : INGREDIENT PRECISION, RECALL & F1
-- OUTPUT FILE : layer3c_ingredient_detection.csv
-- PURPOSE : Provides ingredient counts per experiment for
--           precision, recall, and F1 computation.
--           TP = matched ingredients (not hallucinated)
--           FP = hallucinated ingredients
--           FN = ground truth ingredients not found by LLM
-- ============================================================
SELECT
    e.experiment_id,
    t.reel_id                AS video_id,
    m.model_name,
    pt.technique_name,
    e.rag_enabled,

    -- Ground truth ingredient count (FN denominator)
    (SELECT COUNT(*) FROM ground_truth_ingredient gti JOIN ground_truth_reel gtr ON gti.gt_reel_id = gtr.gt_reel_id WHERE gtr.transcript_id = e.transcript_id) AS gt_ingredient_count,

    -- Predicted ingredient count
    COUNT(DISTINCT ir.ingredient_id)              AS pred_ingredient_count,

    -- True positives (predicted and not hallucinated)
    SUM(CASE WHEN ir.is_hallucinated = FALSE
             THEN 1 ELSE 0 END)                   AS true_positives,

    -- False positives (hallucinated)
    SUM(CASE WHEN ir.is_hallucinated = TRUE
             THEN 1 ELSE 0 END)                   AS false_positives

FROM experiment e
JOIN transcript t               ON e.transcript_id     = t.transcript_id
JOIN llm_model m                ON e.model_id          = m.model_id
JOIN prompt_technique pt        ON e.technique_id      = pt.technique_id
JOIN nutrition_result nr        ON e.experiment_id     = nr.experiment_id
LEFT JOIN ingredient_result ir  ON nr.result_id        = ir.result_id

WHERE e.status = 'completed'
GROUP BY
    e.experiment_id, t.reel_id, m.model_name,
    pt.technique_name, e.rag_enabled
ORDER BY e.experiment_id;


-- ============================================================
-- LAYER 4 : HUMAN EVALUATION — LIKERT & KRIPPENDORFF
-- OUTPUT FILE : layer4_human_evaluation.csv
-- PURPOSE : Placeholder query — to be populated after
--           human_evaluation table is created in Phase 2.
--           Structure shown here for planning purposes.
--           Annotators rate each nutrition_result on three
--           dimensions: fluency, completeness, plausibility.
-- ============================================================
-- NOTE: Uncomment and run after human_evaluation table exists.
--
-- SELECT
--     he.evaluation_id,
--     he.result_id,
--     e.experiment_id,
--     t.reel_id                AS video_id,
--     m.model_name,
--     pt.technique_name,
--     he.annotator_id,
--     he.fluency_score,
--     he.completeness_score,
--     he.plausibility_score,
--     he.evaluated_at
-- FROM human_evaluation he
-- JOIN nutrition_result nr   ON he.result_id     = nr.result_id
-- JOIN experiment e          ON nr.experiment_id = e.experiment_id
-- JOIN transcript t          ON e.transcript_id  = t.transcript_id
-- JOIN llm_model m           ON e.model_id       = m.model_id
-- JOIN prompt_technique pt   ON e.technique_id   = pt.technique_id
-- ORDER BY he.result_id, he.annotator_id;


-- ============================================================
-- LAYER 5 : STATISTICAL SIGNIFICANCE — FRIEDMAN & WILCOXON
-- OUTPUT FILE : layer5_condition_scores.csv
-- PURPOSE : Aggregates F1 metrics per condition (model × technique)
--           across all transcripts for statistical significance.
-- ============================================================
SELECT
    t.reel_id                AS video_id,
    m.model_name,
    pt.technique_name,
    e.rag_enabled,
    COUNT(DISTINCT ir.ingredient_id)              AS pred_count,
    SUM(CASE WHEN ir.is_hallucinated = FALSE
             THEN 1 ELSE 0 END)                   AS true_positives,
    SUM(CASE WHEN ir.is_hallucinated = TRUE
             THEN 1 ELSE 0 END)                   AS false_positives,
    (SELECT COUNT(*) FROM ground_truth_ingredient gti JOIN ground_truth_reel gtr ON gti.gt_reel_id = gtr.gt_reel_id WHERE gtr.transcript_id = e.transcript_id) AS gt_count,
    nr.json_valid,
    nr.total_calories                             AS pred_total_kcal,
    (SELECT SUM(gti.calories) FROM ground_truth_ingredient gti JOIN ground_truth_reel gtr ON gti.gt_reel_id = gtr.gt_reel_id WHERE gtr.transcript_id = e.transcript_id) AS gt_total_kcal

FROM experiment e
JOIN transcript t               ON e.transcript_id     = t.transcript_id
JOIN llm_model m                ON e.model_id          = m.model_id
JOIN prompt_technique pt        ON e.technique_id      = pt.technique_id
JOIN nutrition_result nr        ON e.experiment_id     = nr.experiment_id
LEFT JOIN ingredient_result ir  ON nr.result_id        = ir.result_id

WHERE e.status = 'completed'
GROUP BY
    t.reel_id, m.model_name, pt.technique_name,
    e.rag_enabled, nr.json_valid, nr.total_calories
ORDER BY t.reel_id, m.model_name, pt.technique_name;
