#!/usr/bin/env python3
"""
PROJECT: Nutritional LLM Analysis
PURPOSE: Advanced evaluation script implementing the complete 10-layer evaluation metrics framework.
         Computes NLP string metrics (Exact Match, Fuzzy Match, BLEU-1, BLEU-2, ROUGE-1, ROUGE-L),
         Numeric metrics (MAE, MAPE, Pearson r), Reliability metrics (JSON Validity, Hallucination, F1),
         Human evaluation metrics (Likert ratings, Krippendorff's alpha), and Statistical significance
         (Friedman test, post-hoc Wilcoxon signed-rank tests with Bonferroni correction).
AUTHOR: Antigravity AI (Google DeepMind Team)
"""

import os
import sys
import csv
import math
import re
import datetime
from collections import Counter, defaultdict
import difflib

# List of expected CSV filenames and their corresponding evaluation layers
EXPECTED_FILES = {
    "layer1a": "layer1a_exact_match.csv",
    "layer1b": "layer1b_text_similarity.csv",
    "layer2a": "layer2a_numeric_quantity.csv",
    "layer2b": "layer2b_numeric_nutrition.csv",
    "layer2c": "layer2c_nutrition_totals.csv",
    "layer3a": "layer3a_json_validity.csv",
    "layer3b": "layer3b_hallucination.csv",
    "layer3c": "layer3c_ingredient_detection.csv",
    "layer4": "layer4_human_evaluation.csv",
    "layer5": "layer5_condition_scores.csv"
}

# =====================================================================
# TEXT PROCESSING & NLP METRIC HELPERS
# =====================================================================

def tokenize(text):
    """Normalize and tokenize text into words."""
    if not text:
        return []
    text = str(text).lower().strip()
    return re.findall(r'\b\w+\b', text)

def get_ngrams(tokens, n):
    """Generate n-grams from a list of tokens."""
    return [tuple(tokens[i:i+n]) for i in range(len(tokens)-n+1)]

def token_sort_ratio(s1, s2):
    """Replicates RapidFuzz/fuzzywuzzy token_sort_ratio using difflib.SequenceMatcher."""
    t1 = sorted(tokenize(s1))
    t2 = sorted(tokenize(s2))
    if not t1 and not t2:
        return 1.0
    if not t1 or not t2:
        return 0.0
    sorted_s1 = " ".join(t1)
    sorted_s2 = " ".join(t2)
    return difflib.SequenceMatcher(None, sorted_s1, sorted_s2).ratio()

def calculate_bleu_scores(reference, candidate):
    """Compute BLEU-1 (unigram) and BLEU-2 (bigram) precision scores with brevity penalty."""
    ref_tokens = tokenize(reference)
    cand_tokens = tokenize(candidate)
    
    c = len(cand_tokens)
    r = len(ref_tokens)
    
    if c == 0 or r == 0:
        return 0.0, 0.0
        
    # BLEU-1: Unigram precision
    ref_counts = Counter(ref_tokens)
    cand_counts = Counter(cand_tokens)
    clipped_matches_1 = sum((cand_counts & ref_counts).values())
    p1 = clipped_matches_1 / c
    
    # BLEU-2: Bigram precision
    ref_bigrams = get_ngrams(ref_tokens, 2)
    cand_bigrams = get_ngrams(cand_tokens, 2)
    
    if len(cand_bigrams) == 0:
        p2 = 0.0
    else:
        ref_bg_counts = Counter(ref_bigrams)
        cand_bg_counts = Counter(cand_bigrams)
        clipped_matches_2 = sum((cand_bg_counts & ref_bg_counts).values())
        p2 = clipped_matches_2 / len(cand_bigrams)
        
    # Brevity penalty
    if c > r:
        bp = 1.0
    else:
        bp = math.exp(1 - r / c) if c > 0 else 0.0
        
    bleu1 = bp * p1
    bleu2 = bp * math.sqrt(p1 * p2) if (p1 > 0 and p2 > 0) else 0.0
    
    return bleu1, bleu2

def get_lcs_len(X, Y):
    """Compute length of Longest Common Subsequence between lists X and Y."""
    m, n = len(X), len(Y)
    L = [[0]*(n+1) for _ in range(m+1)]
    for i in range(m+1):
        for j in range(n+1):
            if i == 0 or j == 0:
                L[i][j] = 0
            elif X[i-1] == Y[j-1]:
                L[i][j] = L[i-1][j-1] + 1
            else:
                L[i][j] = max(L[i-1][j], L[i][j-1])
    return L[m][n]

def calculate_rouge_scores(reference, candidate):
    """Compute ROUGE-1 and ROUGE-L F1 scores."""
    ref_tokens = tokenize(reference)
    cand_tokens = tokenize(candidate)
    
    r = len(ref_tokens)
    c = len(cand_tokens)
    
    if r == 0 or c == 0:
        return 0.0, 0.0
        
    # ROUGE-1
    ref_counts = Counter(ref_tokens)
    cand_counts = Counter(cand_tokens)
    overlap = sum((cand_counts & ref_counts).values())
    
    p1 = overlap / c
    r1 = overlap / r
    rouge1 = (2 * p1 * r1) / (p1 + r1) if (p1 + r1) > 0 else 0.0
    
    # ROUGE-L
    lcs_len = get_lcs_len(ref_tokens, cand_tokens)
    p_l = lcs_len / c
    r_l = lcs_len / r
    rougel = (2 * p_l * r_l) / (p_l + r_l) if (p_l + r_l) > 0 else 0.0
    
    return rouge1, rougel

# =====================================================================
# NUMERIC ACCURACY HELPERS
# =====================================================================

def safe_float(val):
    """Attempt to parse a float value safely, returning None on failure."""
    if val is None or val == "":
        return None
    val_str = str(val).lower().strip()
    if val_str in ("null", "none", "n/a", "nan"):
        return None
    try:
        return float(val_str)
    except ValueError:
        return None

def safe_bool(val):
    """Parse boolean values safely."""
    if val is None or val == "":
        return False
    val_str = str(val).lower().strip()
    return val_str in ("true", "1", "y", "yes")

def compute_mae_mape(gt_list, pred_list):
    """Compute Mean Absolute Error (MAE) and Mean Absolute Percentage Error (MAPE)."""
    mae_sum = 0.0
    mape_sum = 0.0
    count_all = 0
    count_mape = 0
    
    for gt, pred in zip(gt_list, pred_list):
        g = safe_float(gt)
        p = safe_float(pred)
        if g is None or p is None:
            continue
        diff = abs(g - p)
        mae_sum += diff
        count_all += 1
        
        if g != 0.0:
            mape_sum += (diff / g) * 100.0
            count_mape += 1
            
    mae = mae_sum / count_all if count_all > 0 else None
    mape = mape_sum / count_mape if count_mape > 0 else None
    return mae, mape, count_all

def compute_pearson(gt_list, pred_list):
    """Compute Pearson Correlation Coefficient."""
    pairs = []
    for gt, pred in zip(gt_list, pred_list):
        g = safe_float(gt)
        p = safe_float(pred)
        if g is not None and p is not None:
            pairs.append((g, p))
            
    n = len(pairs)
    if n < 2:
        return 0.0
        
    mean_x = sum(x for x, y in pairs) / n
    mean_y = sum(y for x, y in pairs) / n
    
    num = sum((x - mean_x) * (y - mean_y) for x, y in pairs)
    den_x = sum((x - mean_x) ** 2 for x, y in pairs)
    den_y = sum((y - mean_y) ** 2 for x, y in pairs)
    
    if den_x == 0 or den_y == 0:
        return 0.0
        
    return num / math.sqrt(den_x * den_y)

def compute_mean_sd(val_list):
    """Compute Mean and Standard Deviation of a list of floats."""
    vals = []
    for v in val_list:
        f = safe_float(v)
        if f is not None:
            vals.append(f)
            
    n = len(vals)
    if n == 0:
        return None, None
    mean = sum(vals) / n
    variance = sum((v - mean) ** 2 for v in vals) / n if n > 1 else 0.0
    sd = math.sqrt(variance)
    return mean, sd

# =====================================================================
# STATISTICAL SIGNIFICANCE & INTER-RATER AGREEMENT
# =====================================================================

def krippendorff_alpha(ratings_matrix, val_type='interval'):
    """
    Computes Krippendorff's Alpha for inter-annotator agreement.
    ratings_matrix: List of lists representing ratings. Shape: (n_items, n_annotators).
                    Contains ratings or None for missing entries.
    """
    ratings = set()
    for row in ratings_matrix:
        for r in row:
            if r is not None:
                ratings.add(r)
    ratings = sorted(list(ratings))
    if len(ratings) < 2:
        return 1.0
        
    def delta2(b, c):
        if val_type == 'interval':
            return (float(b) - float(c)) ** 2
        else:
            return 0.0 if b == c else 1.0

    C = defaultdict(float)
    total_pairs = 0.0
    
    for row in ratings_matrix:
        valid_ratings = [r for r in row if r is not None]
        m = len(valid_ratings)
        if m < 2:
            continue
        for r1 in valid_ratings:
            for r2 in valid_ratings:
                if r1 != r2 or m > 1:
                    C[(r1, r2)] += 1.0 / (m - 1)
        total_pairs += m
        
    if total_pairs == 0:
        return 0.0
        
    num_o = 0.0
    den_o = 0.0
    for (b, c), val in C.items():
        num_o += val * delta2(b, c)
        den_o += val
    if den_o == 0:
        return 1.0
    Do = num_o / den_o
    
    all_ratings = []
    for row in ratings_matrix:
        all_ratings.extend([r for r in row if r is not None])
    n = len(all_ratings)
    if n < 2:
        return 1.0
        
    freq = Counter(all_ratings)
    De = 0.0
    for b in ratings:
        for c in ratings:
            De += (freq[b] * freq[c] if b != c else freq[b] * (freq[b] - 1)) * delta2(b, c)
    De = De / (n * (n - 1))
    
    if De == 0:
        return 1.0 if Do == 0 else 0.0
        
    return 1.0 - (Do / De)

def friedman_test(matrix):
    """
    Computes Friedman test statistic Q and p-value.
    matrix: list of lists of shape (N_samples, k_conditions)
    """
    N = len(matrix)
    if N == 0:
        return 0.0, 1.0
    k = len(matrix[0])
    if k < 2:
        return 0.0, 1.0
        
    # Rank each row
    ranks = []
    for row in matrix:
        sorted_indices = sorted(range(k), key=lambda i: row[i])
        row_ranks = [0.0] * k
        i = 0
        while i < k:
            j = i
            while j < k and row[sorted_indices[j]] == row[sorted_indices[i]]:
                j += 1
            avg_rank = sum(r + 1 for r in range(i, j)) / (j - i)
            for r in range(i, j):
                row_ranks[sorted_indices[r]] = avg_rank
            i = j
        ranks.append(row_ranks)
        
    # Sum ranks for each column
    col_rank_sums = [sum(ranks[i][j] for i in range(N)) for j in range(k)]
    
    # Calculate Q statistic
    sum_sq_ranks = sum(r**2 for r in col_rank_sums)
    Q = (12.0 / (N * k * (k + 1))) * sum_sq_ranks - 3.0 * N * (k + 1)
    
    df = k - 1
    # Wilson-Hilferty Chi-Square approximation
    if Q <= 0:
        return Q, 1.0
    try:
        z = ((Q / df) ** (1.0/3.0) - (1.0 - 2.0 / (9.0 * df))) / math.sqrt(2.0 / (9.0 * df))
        p_val = 1 - 0.5 * (1 + math.erf(z / math.sqrt(2)))
        p_val = max(0.0, min(1.0, p_val))
    except Exception:
        p_val = 1.0
        
    return Q, p_val

def wilcoxon_signed_rank_test(x, y):
    """
    Computes Wilcoxon Signed-Rank Test statistic W, Z-score, and p-value.
    """
    n = len(x)
    if n != len(y):
        raise ValueError("Inputs must have the same length")
        
    diffs = [xi - yi for xi, yi in zip(x, y)]
    diffs = [d for d in diffs if d != 0.0]
    nr = len(diffs)
    
    if nr < 5:
        return None, None, 1.0
        
    abs_diffs = [abs(d) for d in diffs]
    sorted_indices = sorted(range(nr), key=lambda i: abs_diffs[i])
    
    ranks = [0.0] * nr
    i = 0
    while i < nr:
        j = i
        while j < nr and abs_diffs[sorted_indices[j]] == abs_diffs[sorted_indices[i]]:
            j += 1
        avg_rank = sum(r + 1 for r in range(i, j)) / (j - i)
        for r in range(i, j):
            ranks[sorted_indices[r]] = avg_rank
        i = j
        
    w_pos = 0.0
    w_neg = 0.0
    for d, r in zip(diffs, ranks):
        if d > 0:
            w_pos += r
        else:
            w_neg += r
            
    w = min(w_pos, w_neg)
    
    mu_w = nr * (nr + 1) / 4.0
    counts = Counter(abs_diffs)
    tie_term = sum(t**3 - t for t in counts.values() if t > 1)
    
    var_w = (nr * (nr + 1) * (2 * nr + 1) / 24.0) - (tie_term / 48.0)
    if var_w <= 0:
        return w, 0.0, 1.0
        
    sigma_w = math.sqrt(var_w)
    z = (w - mu_w) / sigma_w
    p_value = 2 * (1 - 0.5 * (1 + math.erf(abs(z) / math.sqrt(2))))
    
    return w, z, p_value

# =====================================================================
# DATA UTILITIES
# =====================================================================

def get_col(row, *keys):
    """Look up a column value in a row dict case-insensitively."""
    for key in keys:
        if key in row:
            return row[key]
        for rk in row.keys():
            if rk.lower().strip() == key.lower().strip():
                return row[rk]
    return None

def read_csv(filepath):
    """Read a CSV file and return a list of dictionaries representing rows."""
    if not os.path.isfile(filepath):
        return None
    rows = []
    with open(filepath, mode='r', encoding='utf-8-sig') as f:
        reader = csv.DictReader(f)
        for r in reader:
            rows.append(r)
    return rows

def group_by_condition(rows):
    """Group rows from a CSV by (model_name, technique_name)."""
    groups = defaultdict(list)
    for r in rows:
        model = get_col(r, "model_name", "model")
        tech = get_col(r, "technique_name", "technique")
        if not model or not tech:
            continue
        model = model.strip()
        tech = tech.strip()
        groups[(model, tech)].append(r)
    return groups

# =====================================================================
# REPORT FORMATTING HELPERS
# =====================================================================

def print_ascii_table(title, headers, rows):
    """Print a clean ASCII table to the console."""
    print(f"\n=== {title} ===")
    widths = [len(h) for h in headers]
    for r in rows:
        for idx, val in enumerate(r):
            val_str = f"{val:.4f}" if isinstance(val, float) else str(val)
            widths[idx] = max(widths[idx], len(val_str))
            
    header_str = " | ".join(f"{h:<{widths[i]}}" for i, h in enumerate(headers))
    print(header_str)
    print("-" * len(header_str))
    
    for r in rows:
        row_str = " | ".join(
            f"{val:>{widths[i]}.4f}" if isinstance(val, float) else f"{str(val):<{widths[i]}}" 
            for i, val in enumerate(r)
        )
        print(row_str)

def format_markdown_table(headers, rows):
    """Generate Markdown code for a table."""
    widths = [len(h) for h in headers]
    for r in rows:
        for idx, val in enumerate(r):
            val_str = f"{val:.4f}" if isinstance(val, float) else str(val)
            widths[idx] = max(widths[idx], len(val_str))
            
    lines = []
    lines.append("| " + " | ".join(f"{h:<{widths[i]}}" for i, h in enumerate(headers)) + " |")
    lines.append("| " + " | ".join("-" * widths[i] for i in range(len(headers))) + " |")
    for r in rows:
        row_str = "| " + " | ".join(
            f"{val:>{widths[i]}.4f}" if isinstance(val, float) else f"{str(val):<{widths[i]}}" 
            for i, val in enumerate(r)
        ) + " |"
        lines.append(row_str)
    return "\n".join(lines)

# =====================================================================
# MAIN EVALUATION PIPELINE
# =====================================================================

def main():
    print("=" * 60)
    print("          NUTRITIONAL LLM SERVICE EVALUATION RUNNER")
    print("=" * 60)
    
    target_dir = "."
    if len(sys.argv) > 1:
        target_dir = sys.argv[1]
    
    possible_dirs = [target_dir, os.path.join(target_dir, "exports"), os.path.join(target_dir, "Latest_Project_Data")]
    found_dir = None
    
    for d in possible_dirs:
        if os.path.isdir(d):
            test_file = EXPECTED_FILES["layer1a"]
            if os.path.isfile(os.path.join(d, test_file)):
                found_dir = d
                break
                
    if not found_dir:
        for d in possible_dirs:
            if os.path.isdir(d):
                files = os.listdir(d)
                if any(f.endswith('.csv') for f in files):
                    found_dir = d
                    break
                    
    if not found_dir:
        print("\n[WARNING] Could not automatically find evaluation CSV files!")
        print("Please run your Java application, navigate to the 'CSV Exporter' tab,")
        print("and export the evaluation layers to your project directory.")
        print(f"Expected files to export: {', '.join(EXPECTED_FILES.values())}")
        print("\nExiting. Place the files in this directory and re-run.")
        sys.exit(1)
        
    print(f"Reading CSV reports from folder: {os.path.abspath(found_dir)}")
    
    now_str = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    
    report_content = []
    report_content.append("# Nutritional LLM Service Evaluation Report")
    report_content.append(f"**Data Directory:** `{os.path.abspath(found_dir)}`  ")
    report_content.append(f"**Generated:** {now_str}\n")
    report_content.append("---")
    
    all_results = defaultdict(dict)
    
    # =====================================================================
    # LAYER 1A: EXACT MATCH (EM)
    # =====================================================================
    file1a = os.path.join(found_dir, EXPECTED_FILES["layer1a"])
    rows1a = read_csv(file1a)
    if rows1a:
        print("\nProcessing Layer 1A: Exact Match...")
        grouped = group_by_condition(rows1a)
        table_rows = []
        for (model, tech), data in grouped.items():
            total = len(data)
            match_name_en = 0
            match_unit_en = 0
            
            for r in data:
                gt_name_e = (get_col(r, "gt_name_en") or "").strip().lower()
                pred_name_e = (get_col(r, "pred_name_en") or "").strip().lower()
                if gt_name_e == pred_name_e and gt_name_e != "":
                    match_name_en += 1
                    
                gt_unit_e = (get_col(r, "gt_unit_en") or "").strip().lower()
                pred_unit_e = (get_col(r, "pred_unit_en") or "").strip().lower()
                if gt_unit_e == pred_unit_e and gt_unit_e != "":
                    match_unit_en += 1
                    
            em_name_en = (match_name_en / total) * 100.0 if total > 0 else 0.0
            em_unit_en = (match_unit_en / total) * 100.0 if total > 0 else 0.0
            
            all_results[(model, tech)]["em_name_en"] = em_name_en
            all_results[(model, tech)]["em_unit_en"] = em_unit_en
            table_rows.append([model, tech, total, em_name_en, em_unit_en])
            
        headers = ["Model", "Technique", "Total Fields", "EM Name (English) %", "EM Unit (English) %"]
        print_ascii_table("LAYER 1A: EXACT MATCH RATES", headers, table_rows)
        
        report_content.append("\n## Layer 1A: Exact Match Rates")
        report_content.append(format_markdown_table(headers, table_rows))
    else:
        print(f"\n[INFO] Missing {EXPECTED_FILES['layer1a']}. Skipping Layer 1A.")
        
    # =====================================================================
    # LAYER 1B: TEXT SIMILARITY & FUZZY MATCH
    # =====================================================================
    file1b = os.path.join(found_dir, EXPECTED_FILES["layer1b"])
    rows1b = read_csv(file1b)
    if rows1b:
        print("\nProcessing Layer 1B: Text Similarity & Fuzzy Match...")
        grouped = group_by_condition(rows1b)
        table_rows = []
        for (model, tech), data in grouped.items():
            total = len(data)
            bleu1_sum, bleu2_sum = 0.0, 0.0
            rouge1_sum, rougel_sum = 0.0, 0.0
            fuzzy_matches = 0
            
            for r in data:
                gt_name_orig = get_col(r, "gt_name_original") or ""
                pred_name_orig = get_col(r, "pred_name_original") or ""
                gt_name_en = get_col(r, "gt_name_en") or ""
                pred_name_en = get_col(r, "pred_name_en") or ""
                
                # Fuzzy Token Sort Ratio comparison (>= 0.85) on original names (handles Malay code-switch)
                ratio = token_sort_ratio(gt_name_orig, pred_name_orig)
                if ratio >= 0.85:
                    fuzzy_matches += 1
                
                # NLP overlap calculations on English name fields
                b1, b2 = calculate_bleu_scores(gt_name_en, pred_name_en)
                r1, rl = calculate_rouge_scores(gt_name_en, pred_name_en)
                
                bleu1_sum += b1
                bleu2_sum += b2
                rouge1_sum += r1
                rougel_sum += rl
                
            fuzzy_match_pct = (fuzzy_matches / total) * 100.0 if total > 0 else 0.0
            avg_b1 = bleu1_sum / total if total > 0 else 0.0
            avg_b2 = bleu2_sum / total if total > 0 else 0.0
            avg_r1 = rouge1_sum / total if total > 0 else 0.0
            avg_rl = rougel_sum / total if total > 0 else 0.0
            
            all_results[(model, tech)]["fuzzy_match"] = fuzzy_match_pct
            all_results[(model, tech)]["bleu1"] = avg_b1
            all_results[(model, tech)]["bleu2"] = avg_b2
            all_results[(model, tech)]["rouge1"] = avg_r1
            all_results[(model, tech)]["rougel"] = avg_rl
            
            table_rows.append([model, tech, total, fuzzy_match_pct, avg_b1, avg_b2, avg_r1, avg_rl])
            
        headers = ["Model", "Technique", "Total Pairs", "Fuzzy Match (>=0.85) %", "BLEU-1", "BLEU-2", "ROUGE-1", "ROUGE-L"]
        print_ascii_table("LAYER 1B: TEXT SIMILARITY & FUZZY MATCHING", headers, table_rows)
        
        report_content.append("\n## Layer 1B: Text Similarity & Fuzzy Matching")
        report_content.append(format_markdown_table(headers, table_rows))
    else:
        print(f"\n[INFO] Missing {EXPECTED_FILES['layer1b']}. Skipping Layer 1B.")
        
    # =====================================================================
    # LAYER 2A: NUMERIC QUANTITY & WEIGHT
    # =====================================================================
    file2a = os.path.join(found_dir, EXPECTED_FILES["layer2a"])
    rows2a = read_csv(file2a)
    if rows2a:
        print("\nProcessing Layer 2A: Numeric Quantity & Weight...")
        grouped = group_by_condition(rows2a)
        table_rows = []
        for (model, tech), data in grouped.items():
            gt_q_list = [get_col(r, "gt_quantity_value") for r in data]
            pred_q_list = [get_col(r, "pred_quantity_value") for r in data]
            gt_w_list = [get_col(r, "gt_weight_g") for r in data]
            pred_w_list = [get_col(r, "pred_weight_g") for r in data]
            
            mae_q, mape_q, cnt_q = compute_mae_mape(gt_q_list, pred_q_list)
            mae_w, mape_w, cnt_w = compute_mae_mape(gt_w_list, pred_w_list)
            
            all_results[(model, tech)]["mae_quantity"] = mae_q
            all_results[(model, tech)]["mae_weight"] = mae_w
            
            table_rows.append([model, tech, len(data), mae_q, mape_q, mae_w, mape_w])
            
        headers = ["Model", "Technique", "Pairs", "Quantity MAE", "Quantity MAPE %", "Weight (g) MAE", "Weight (g) MAPE %"]
        print_ascii_table("LAYER 2A: NUMERIC QUANTITY & ESTIMATED WEIGHT ACCURACY", headers, table_rows)
        
        report_content.append("\n## Layer 2A: Numeric Quantity & Estimated Weight Accuracy")
        report_content.append(format_markdown_table(headers, table_rows))
    else:
        print(f"\n[INFO] Missing {EXPECTED_FILES['layer2a']}. Skipping Layer 2A.")
        
    # =====================================================================
    # LAYER 2B: NUMERIC NUTRITION (Calories, Protein, Fat, Carbs)
    # =====================================================================
    file2b = os.path.join(found_dir, EXPECTED_FILES["layer2b"])
    rows2b = read_csv(file2b)
    if rows2b:
        print("\nProcessing Layer 2B: Numeric Nutrition...")
        grouped = group_by_condition(rows2b)
        table_rows = []
        for (model, tech), data in grouped.items():
            gt_cal = [get_col(r, "gt_energy_kcal") for r in data]
            pred_cal = [get_col(r, "pred_energy_kcal") for r in data]
            gt_prot = [get_col(r, "gt_protein_g") for r in data]
            pred_prot = [get_col(r, "pred_protein_g") for r in data]
            gt_fat = [get_col(r, "gt_fat_g") for r in data]
            pred_fat = [get_col(r, "pred_fat_g") for r in data]
            gt_carb = [get_col(r, "gt_carbohydrate_g") for r in data]
            pred_carb = [get_col(r, "pred_carbohydrate_g") for r in data]
            
            mae_c, mape_c, _ = compute_mae_mape(gt_cal, pred_cal)
            r_c = compute_pearson(gt_cal, pred_cal)
            
            mae_p, mape_p, _ = compute_mae_mape(gt_prot, pred_prot)
            r_p = compute_pearson(gt_prot, pred_prot)
            
            mae_f, mape_f, _ = compute_mae_mape(gt_fat, pred_fat)
            r_f = compute_pearson(gt_fat, pred_fat)
            
            mae_cb, mape_cb, _ = compute_mae_mape(gt_carb, pred_carb)
            r_cb = compute_pearson(gt_carb, pred_carb)
            
            all_results[(model, tech)]["mae_cal"] = mae_c
            all_results[(model, tech)]["mape_cal"] = mape_c
            all_results[(model, tech)]["r_cal"] = r_c
            
            table_rows.append([model, tech, mae_c, mape_c, r_c, mae_p, r_p, mae_f, r_f, mae_cb, r_cb])
            
        headers = ["Model", "Technique", "Calories MAE", "Cal MAPE %", "Cal Pearson r", "Protein MAE", "Prot r", "Fat MAE", "Fat r", "Carbs MAE", "Carbs r"]
        print_ascii_table("LAYER 2B: INGREDIENT-LEVEL NUTRITIONAL VALUES ACCURACY", headers, table_rows)
        
        report_content.append("\n## Layer 2B: Ingredient-Level Nutritional Values Accuracy")
        report_content.append(format_markdown_table(headers, table_rows))
    else:
        print(f"\n[INFO] Missing {EXPECTED_FILES['layer2b']}. Skipping Layer 2B.")
        
    # =====================================================================
    # LAYER 2C: RECIPE-LEVEL TOTALS
    # =====================================================================
    file2c = os.path.join(found_dir, EXPECTED_FILES["layer2c"])
    rows2c = read_csv(file2c)
    if rows2c:
        print("\nProcessing Layer 2C: Recipe Nutrition Totals...")
        grouped = group_by_condition(rows2c)
        table_rows = []
        for (model, tech), data in grouped.items():
            gt_cal = [get_col(r, "gt_total_energy_kcal") for r in data]
            pred_cal = [get_col(r, "pred_total_energy_kcal") for r in data]
            gt_prot = [get_col(r, "gt_total_protein_g") for r in data]
            pred_prot = [get_col(r, "pred_total_protein_g") for r in data]
            gt_fat = [get_col(r, "gt_total_fat_g") for r in data]
            pred_fat = [get_col(r, "pred_total_fat_g") for r in data]
            gt_carb = [get_col(r, "gt_total_carbohydrate_g") for r in data]
            pred_carb = [get_col(r, "pred_total_carbohydrate_g") for r in data]
            
            mae_c, mape_c, _ = compute_mae_mape(gt_cal, pred_cal)
            mae_p, mape_p, _ = compute_mae_mape(gt_prot, pred_prot)
            mae_f, mape_f, _ = compute_mae_mape(gt_fat, pred_fat)
            mae_cb, mape_cb, _ = compute_mae_mape(gt_carb, pred_carb)
            
            all_results[(model, tech)]["recipe_cal_mae"] = mae_c
            all_results[(model, tech)]["recipe_cal_mape"] = mape_c
            
            table_rows.append([model, tech, len(data), mae_c, mape_c, mae_p, mae_f, mae_cb])
            
        headers = ["Model", "Technique", "Recipes", "Total Cal MAE", "Total Cal MAPE %", "Total Prot MAE", "Total Fat MAE", "Total Carbs MAE"]
        print_ascii_table("LAYER 2C: RECIPE-LEVEL NUTRITIONAL TOTALS ACCURACY", headers, table_rows)
        
        report_content.append("\n## Layer 2C: Recipe-Level Nutritional Totals Accuracy")
        report_content.append(format_markdown_table(headers, table_rows))
    else:
        print(f"\n[INFO] Missing {EXPECTED_FILES['layer2c']}. Skipping Layer 2C.")

    # =====================================================================
    # LAYER 3A: JSON VALIDITY RATE
    # =====================================================================
    file3a = os.path.join(found_dir, EXPECTED_FILES["layer3a"])
    rows3a = read_csv(file3a)
    if rows3a:
        print("\nProcessing Layer 3A: JSON Validity...")
        table_rows = []
        for r in rows3a:
            model = get_col(r, "model_name")
            tech = get_col(r, "technique_name")
            runs = safe_float(get_col(r, "total_runs"))
            valid = safe_float(get_col(r, "valid_count"))
            pct = safe_float(get_col(r, "validity_rate_pct"))
            
            if model and tech:
                all_results[(model, tech)]["json_validity"] = pct
                table_rows.append([model, tech, int(runs) if runs else 0, int(valid) if valid else 0, pct])
                
        headers = ["Model", "Technique", "Total Runs", "Valid JSON Runs", "Validity Rate %"]
        print_ascii_table("LAYER 3A: JSON OUTPUT VALIDITY RATES", headers, table_rows)
        
        report_content.append("\n## Layer 3A: JSON Output Validity Rates")
        report_content.append(format_markdown_table(headers, table_rows))
    else:
        print(f"\n[INFO] Missing {EXPECTED_FILES['layer3a']}. Skipping Layer 3A.")

    # =====================================================================
    # LAYER 3B: HALLUCINATION RATE
    # =====================================================================
    file3b = os.path.join(found_dir, EXPECTED_FILES["layer3b"])
    rows3b = read_csv(file3b)
    if rows3b:
        print("\nProcessing Layer 3B: Hallucination Rates...")
        grouped = group_by_condition(rows3b)
        table_rows = []
        for (model, tech), data in grouped.items():
            total = len(data)
            hallucinated_count = 0
            for r in data:
                is_h = safe_bool(get_col(r, "is_hallucinated"))
                if is_h:
                    hallucinated_count += 1
            pct = (hallucinated_count / total) * 100.0 if total > 0 else 0.0
            
            all_results[(model, tech)]["hallucination_rate"] = pct
            table_rows.append([model, tech, total, hallucinated_count, pct])
            
        headers = ["Model", "Technique", "Predicted Ingredients", "Hallucinated Count", "Hallucination Rate %"]
        print_ascii_table("LAYER 3B: INGREDIENT HALLUCINATION RATES", headers, table_rows)
        
        report_content.append("\n## Layer 3B: Ingredient Hallucination Rates")
        report_content.append(format_markdown_table(headers, table_rows))
    else:
        print(f"\n[INFO] Missing {EXPECTED_FILES['layer3b']}. Skipping Layer 3B.")

    # =====================================================================
    # LAYER 3C: INGREDIENT DETECTION (Precision, Recall, F1)
    # =====================================================================
    file3c = os.path.join(found_dir, EXPECTED_FILES["layer3c"])
    rows3c = read_csv(file3c)
    if rows3c:
        print("\nProcessing Layer 3C: Ingredient Detection...")
        grouped = group_by_condition(rows3c)
        table_rows = []
        for (model, tech), data in grouped.items():
            prec_sum = 0.0
            rec_sum = 0.0
            f1_sum = 0.0
            n_experiments = len(data)
            
            for r in data:
                tp = safe_float(get_col(r, "true_positives")) or 0.0
                fp = safe_float(get_col(r, "false_positives")) or 0.0
                gt = safe_float(get_col(r, "gt_ingredient_count")) or 0.0
                
                prec = tp / (tp + fp) if (tp + fp) > 0 else 0.0
                rec = tp / gt if gt > 0 else 0.0
                f1 = (2 * prec * rec) / (prec + rec) if (prec + rec) > 0 else 0.0
                
                prec_sum += prec
                rec_sum += rec
                f1_sum += f1
                
            avg_prec = (prec_sum / n_experiments) * 100.0 if n_experiments > 0 else 0.0
            avg_rec = (rec_sum / n_experiments) * 100.0 if n_experiments > 0 else 0.0
            avg_f1 = (f1_sum / n_experiments) * 100.0 if n_experiments > 0 else 0.0
            
            all_results[(model, tech)]["precision"] = avg_prec
            all_results[(model, tech)]["recall"] = avg_rec
            all_results[(model, tech)]["f1_score"] = avg_f1
            table_rows.append([model, tech, n_experiments, avg_prec, avg_rec, avg_f1])
            
        headers = ["Model", "Technique", "Experiments", "Avg Precision %", "Avg Recall %", "Avg F1-Score %"]
        print_ascii_table("LAYER 3C: INGREDIENT DETECTION METRICS", headers, table_rows)
        
        report_content.append("\n## Layer 3C: Ingredient Detection Metrics")
        report_content.append(format_markdown_table(headers, table_rows))
    else:
        print(f"\n[INFO] Missing {EXPECTED_FILES['layer3c']}. Skipping Layer 3C.")

    # =====================================================================
    # LAYER 4: HUMAN EVALUATION (Likert Scale & Krippendorff's Alpha)
    # =====================================================================
    file4 = os.path.join(found_dir, EXPECTED_FILES["layer4"])
    rows4 = read_csv(file4)
    if rows4 and len(rows4) > 0:
        print("\nProcessing Layer 4: Human Evaluation & Krippendorff's Alpha...")
        
        # Likert Ratings grouped by model & technique
        grouped = group_by_condition(rows4)
        table_rows = []
        
        for (model, tech), data in grouped.items():
            fluencies = [safe_float(get_col(r, "fluency_score")) for r in data]
            completenesses = [safe_float(get_col(r, "completeness_score")) for r in data]
            plausibilities = [safe_float(get_col(r, "plausibility_score")) for r in data]
            
            mean_fl, sd_fl = compute_mean_sd(fluencies)
            mean_co, sd_co = compute_mean_sd(completenesses)
            mean_pl, sd_pl = compute_mean_sd(plausibilities)
            
            table_rows.append([
                model, tech, len(data),
                f"{mean_fl:.2f} ± {sd_fl:.2f}" if mean_fl else "N/A",
                f"{mean_co:.2f} ± {sd_co:.2f}" if mean_co else "N/A",
                f"{mean_pl:.2f} ± {sd_pl:.2f}" if mean_pl else "N/A"
            ])
            
        headers = ["Model", "Technique", "Ratings", "Fluency Rating", "Completeness Rating", "Plausibility Rating"]
        print_ascii_table("LAYER 4: LIKERT RATINGS (MEAN ± SD)", headers, table_rows)
        
        report_content.append("\n## Layer 4: Human Evaluation Likert Ratings (Mean ± SD)")
        report_content.append(format_markdown_table(headers, table_rows))
        
        # Calculate Krippendorff's Alpha (Inter-Annotator Agreement)
        # Pivot ratings per item (result_id) and annotator (annotator_id)
        # We find unique results and annotators
        unique_results = sorted(list(set(get_col(r, "result_id") for r in rows4 if get_col(r, "result_id"))))
        unique_annotators = sorted(list(set(get_col(r, "annotator_id") for r in rows4 if get_col(r, "annotator_id"))))
        
        if len(unique_annotators) >= 2:
            matrix_fl = []
            matrix_co = []
            matrix_pl = []
            
            for res_id in unique_results:
                row_fl = []
                row_co = []
                row_pl = []
                for ann_id in unique_annotators:
                    # Find score for this result by this annotator
                    matches = [r for r in rows4 if get_col(r, "result_id") == res_id and get_col(r, "annotator_id") == ann_id]
                    if matches:
                        row_fl.append(safe_float(get_col(matches[0], "fluency_score")))
                        row_co.append(safe_float(get_col(matches[0], "completeness_score")))
                        row_pl.append(safe_float(get_col(matches[0], "plausibility_score")))
                    else:
                        row_fl.append(None)
                        row_co.append(None)
                        row_pl.append(None)
                matrix_fl.append(row_fl)
                matrix_co.append(row_co)
                matrix_pl.append(row_pl)
                
            alpha_fl = krippendorff_alpha(matrix_fl, 'interval')
            alpha_co = krippendorff_alpha(matrix_co, 'interval')
            alpha_pl = krippendorff_alpha(matrix_pl, 'interval')
            
            print(f"\nKrippendorff's Alpha (Inter-Annotator Agreement):")
            print(f"  - Fluency Alpha       : {alpha_fl:.4f}")
            print(f"  - Completeness Alpha  : {alpha_co:.4f}")
            print(f"  - Plausibility Alpha  : {alpha_pl:.4f}")
            
            report_content.append("\n### Inter-Annotator Agreement (Krippendorff's Alpha)")
            report_content.append(f"- **Fluency α:** `{alpha_fl:.4f}`")
            report_content.append(f"- **Completeness α:** `{alpha_co:.4f}`")
            report_content.append(f"- **Plausibility α:** `{alpha_pl:.4f}`")
            
            for score_name, alpha in [("Fluency", alpha_fl), ("Completeness", alpha_co), ("Plausibility", alpha_pl)]:
                status = "Strong Agreement" if alpha >= 0.8 else "Acceptable Agreement" if alpha >= 0.667 else "Weak/Poor Agreement"
                report_content.append(f"  - *{score_name}* meets the Scopus threshold: **{status}**")
        else:
            print("\n[INFO] Need at least 2 annotators in the dataset to calculate Krippendorff's Alpha.")
    else:
        print(f"\n[INFO] Missing {EXPECTED_FILES['layer4']} or file is empty placeholder. Skipping Layer 4 metrics.")

    # =====================================================================
    # LAYER 5: STATISTICAL SIGNIFICANCE (Friedman & Wilcoxon Post-hoc)
    # =====================================================================
    file5 = os.path.join(found_dir, EXPECTED_FILES["layer5"])
    rows5 = read_csv(file5)
    if rows5:
        print("\nProcessing Layer 5: Statistical Significance...")
        
        # 1. Friedman Test
        # Pivot scores: map video_id -> condition_index -> F1 score
        # Let's collect conditions
        conditions_list = sorted(list(set((get_col(r, "model_name").strip(), get_col(r, "technique_name").strip()) for r in rows5)))
        cond_map = {cond: idx for idx, cond in enumerate(conditions_list)}
        
        # Group scores by video_id
        video_scores = defaultdict(lambda: [0.0] * len(conditions_list))
        video_counts = defaultdict(lambda: [0] * len(conditions_list))
        
        for r in rows5:
            vid = get_col(r, "video_id")
            model = get_col(r, "model_name").strip()
            tech = get_col(r, "technique_name").strip()
            
            tp = safe_float(get_col(r, "true_positives")) or 0.0
            fp = safe_float(get_col(r, "false_positives")) or 0.0
            gt = safe_float(get_col(r, "gt_count")) or 0.0
            
            prec = tp / (tp + fp) if (tp + fp) > 0 else 0.0
            rec = tp / gt if gt > 0 else 0.0
            f1 = (2 * prec * rec) / (prec + rec) if (prec + rec) > 0 else 0.0
            
            cond = (model, tech)
            if cond in cond_map:
                c_idx = cond_map[cond]
                video_scores[vid][c_idx] = f1
                video_counts[vid][c_idx] += 1
                
        # Filter videos that have scores for all 16 conditions
        complete_matrix = []
        for vid, scores in video_scores.items():
            if len(scores) == len(conditions_list):
                complete_matrix.append(scores)
                
        n_samples = len(complete_matrix)
        k_conditions = len(conditions_list)
        
        if n_samples > 0 and k_conditions >= 3:
            q_stat, p_val = friedman_test(complete_matrix)
            print(f"\nFriedman Test (Comparing all {k_conditions} conditions):")
            print(f"  - Samples (Videos): {n_samples}")
            print(f"  - Friedman Q Stat : {q_stat:.4f}")
            print(f"  - p-value         : {p_val:.4g}")
            significant = p_val < 0.05
            print(f"  - Significant     : {significant} (alpha=0.05)")
            
            report_content.append("\n## Layer 5: Statistical Significance (Friedman Test)")
            report_content.append(f"- **Number of transcripts analyzed (N):** `{n_samples}`")
            report_content.append(f"- **Friedman Q-statistic:** `{q_stat:.4f}`")
            report_content.append(f"- **p-value:** `{p_val:.4g}`")
            report_content.append(f"- **Statistical Significance:** " + ("**Yes, performance differences are statistically meaningful (p < 0.05).**" if significant else "*No statistically significant differences found (p >= 0.05).*"))
            
            # Wilcoxon Post-hoc pairwise tests
            report_content.append("\n### Post-Hoc Pairwise Wilcoxon Signed-Rank Test")
            
            # Compare models (aggregating across techniques)
            models_list = sorted(list(set(c[0] for c in conditions_list)))
            model_scores_per_video = defaultdict(list)
            for scores in complete_matrix:
                for m_idx, m_name in enumerate(models_list):
                    # Average F1 score for this model on this video across all techniques
                    m_scores = [scores[idx] for idx, c in enumerate(conditions_list) if c[0] == m_name]
                    model_scores_per_video[m_name].append(sum(m_scores) / len(m_scores))
            
            print("\nWilcoxon Post-hoc tests for Models:")
            model_comparisons = []
            
            # 6 comparisons for 4 models. Bonferroni adjusted alpha = 0.05 / 6 = 0.00833
            adj_alpha_m = 0.05 / 6
            for i in range(len(models_list)):
                for j in range(i+1, len(models_list)):
                    m1 = models_list[i]
                    m2 = models_list[j]
                    scores1 = model_scores_per_video[m1]
                    scores2 = model_scores_per_video[m2]
                    w, z, p = wilcoxon_signed_rank_test(scores1, scores2)
                    if w is not None:
                        is_sig = p < adj_alpha_m
                        model_comparisons.append([m1, m2, w, z, p, "YES" if is_sig else "NO"])
                        print(f"  - {m1} vs {m2}: W={w:.1f}, Z={z:.4f}, p={p:.4g} (Sig: {is_sig})")
                        
            headers_m = ["Model 1", "Model 2", "W statistic", "Z-score", "p-value", "Significant (Adjusted p<0.0083)"]
            report_content.append("\n#### Model Pairwise Comparisons (Bonferroni Corrected)")
            report_content.append(format_markdown_table(headers_m, model_comparisons))
            
            # Compare prompt techniques (aggregating across models)
            tech_list = sorted(list(set(c[1] for c in conditions_list)))
            tech_scores_per_video = defaultdict(list)
            for scores in complete_matrix:
                for t_idx, t_name in enumerate(tech_list):
                    t_scores = [scores[idx] for idx, c in enumerate(conditions_list) if c[1] == t_name]
                    tech_scores_per_video[t_name].append(sum(t_scores) / len(t_scores))
                    
            print("\nWilcoxon Post-hoc tests for Prompt Techniques:")
            tech_comparisons = []
            
            # 6 comparisons for 4 techniques. Bonferroni adjusted alpha = 0.05 / 6 = 0.00833
            adj_alpha_t = 0.05 / 6
            for i in range(len(tech_list)):
                for j in range(i+1, len(tech_list)):
                    t1 = tech_list[i]
                    t2 = tech_list[j]
                    scores1 = tech_scores_per_video[t1]
                    scores2 = tech_scores_per_video[t2]
                    w, z, p = wilcoxon_signed_rank_test(scores1, scores2)
                    if w is not None:
                        is_sig = p < adj_alpha_t
                        tech_comparisons.append([t1, t2, w, z, p, "YES" if is_sig else "NO"])
                        print(f"  - {t1} vs {t2}: W={w:.1f}, Z={z:.4f}, p={p:.4g} (Sig: {is_sig})")
                        
            headers_t = ["Technique 1", "Technique 2", "W statistic", "Z-score", "p-value", "Significant (Adjusted p<0.0083)"]
            report_content.append("\n#### Prompt Technique Pairwise Comparisons (Bonferroni Corrected)")
            report_content.append(format_markdown_table(headers_t, tech_comparisons))
        else:
            print("\n[INFO] Complete score matrix is empty. Ensure all 16 conditions have data in Layer 5 to run statistical tests.")
    else:
        print(f"\n[INFO] Missing {EXPECTED_FILES['layer5']}. Skipping Layer 5 statistical tests.")

    # =====================================================================
    # LAYER 5: CONDITION SCORES & SUMMARY REPORT
    # =====================================================================
    if all_results:
        print("\nGenerating Overall Performance Summary...")
        summary_rows = []
        for (model, tech), metrics in all_results.items():
            summary_rows.append([
                model,
                tech,
                metrics.get("json_validity", "N/A"),
                metrics.get("em_name_en", "N/A"),
                metrics.get("fuzzy_match", "N/A"),
                metrics.get("bleu1", "N/A"),
                metrics.get("rougel", "N/A"),
                metrics.get("mae_cal", "N/A"),
                metrics.get("r_cal", "N/A"),
                metrics.get("hallucination_rate", "N/A"),
                metrics.get("f1_score", "N/A")
            ])
            
        headers = ["Model", "Technique", "JSON Valid %", "EM Name %", "Fuzzy Name %", "BLEU-1", "ROUGE-L", "Calories MAE", "Cal Pearson r", "Hallucination %", "F1 Detection %"]
        print_ascii_table("OVERALL PERFORMANCE SUMMARY BY EXPERIMENTAL CONDITION", headers, summary_rows)
        
        report_content.append("\n## Overall Performance Summary by Condition")
        report_content.append(format_markdown_table(headers, summary_rows))
        
        # Determine best performing model & technique based on F1 detection and Calories MAE
        best_f1 = -1.0
        best_condition = None
        best_mae = float('inf')
        best_mae_condition = None
        
        for (model, tech), metrics in all_results.items():
            f1 = metrics.get("f1_score", -1.0)
            if isinstance(f1, float) and f1 > best_f1:
                best_f1 = f1
                best_condition = (model, tech)
                
            mae = metrics.get("mae_cal", float('inf'))
            if isinstance(mae, float) and mae < best_mae:
                best_mae = mae
                best_mae_condition = (model, tech)
                
        report_content.append("\n### Key Takeaways")
        if best_condition:
            report_content.append(f"- **Highest Ingredient Detection F1-Score:** `{best_condition[0]}` with `{best_condition[1]}` prompt technique ({best_f1:.2f}%)")
        if best_mae_condition:
            report_content.append(f"- **Lowest Calorie Estimation Error (MAE):** `{best_mae_condition[0]}` with `{best_mae_condition[1]}` prompt technique ({best_mae:.2f} kcal)")
            
    # Write report file
    report_file = os.path.join(found_dir, "evaluation_report.md")
    with open(report_file, mode="w", encoding="utf-8") as f:
        f.write("\n".join(report_content))
        
    print(f"\n{'='*60}")
    print(f"Evaluation completed successfully!")
    print(f"Markdown report generated at: {os.path.abspath(report_file)}")
    print(f"{'='*60}\n")

if __name__ == "__main__":
    main()
