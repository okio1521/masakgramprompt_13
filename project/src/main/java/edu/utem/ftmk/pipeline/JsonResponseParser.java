package edu.utem.ftmk.pipeline;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import edu.utem.ftmk.model.IngredientResult;
import edu.utem.ftmk.model.NutritionResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses and sanitizes LLM JSON output into NutritionResult and IngredientResult models.
 * Includes multi-stage structural repair for common LLM JSON defects:
 *   - Missing colon between key and value  ("key" "value" -> "key": "value")
 *   - Trailing commas before } or ]        (,}  ->  })
 *   - Unquoted bare-word string values     (key: bare  -> key: "bare")
 *   - Truncated / incomplete JSON          (auto-closed by repairJson)
 */
@Component
public class JsonResponseParser {

    private final Gson gson = new Gson();

    /**
     * Parses raw JSON string into a structured NutritionResult object.
     * Extracts values defensively, handling potential LLM parsing and type anomalies.
     */
    public NutritionResult parse(String rawResponse, int experimentId) throws Exception {
        String cleaned = cleanJson(rawResponse);

        // --- Structural repair passes ---
        // Missing-commas MUST run before missing-colons, because missing-colons would
        // misinterpret a value string followed by a key string as a key:value pair.
        cleaned = repairMissingCommas(cleaned);
        cleaned = repairMissingColons(cleaned);
        cleaned = repairTrailingCommas(cleaned);

        // Parse as generic JsonElement to handle cases where the LLM returns a primitive (e.g., a string) instead of an object.
        JsonReader reader = new JsonReader(new java.io.StringReader(cleaned));
        reader.setLenient(true);
        JsonElement rootElement;
        try {
            rootElement = gson.fromJson(reader, JsonElement.class);
        } catch (Exception firstEx) {
            // Last-resort: strip everything after the last valid '}' and re-try
            System.err.println("[JsonResponseParser] First parse attempt failed (" + firstEx.getMessage() + "), attempting last-resort strip repair...");
            cleaned = lastResortRepair(cleaned);
            try {
                JsonReader reader2 = new JsonReader(new java.io.StringReader(cleaned));
                reader2.setLenient(true);
                rootElement = gson.fromJson(reader2, JsonElement.class);
            } catch (Exception secondEx) {
                // All repair attempts failed — return a result with jsonValid=false
                // instead of throwing, so the experiment can still complete
                System.err.println("[JsonResponseParser] All parse attempts failed (" + secondEx.getMessage() + "). Returning empty result with raw output.");
                NutritionResult emptyResult = new NutritionResult();
                emptyResult.setExperimentId(experimentId);
                emptyResult.setRawJsonOutput(rawResponse);
                emptyResult.setJsonValid(false);
                emptyResult.setIngredients(new ArrayList<>());
                return emptyResult;
            }
        }
        JsonObject root = null;
        boolean isValid = false;
        if (rootElement != null && rootElement.isJsonObject()) {
            root = rootElement.getAsJsonObject();
            isValid = true;
        }
        
        NutritionResult result = new NutritionResult();
        result.setExperimentId(experimentId);
        result.setRawJsonOutput(rawResponse);
        result.setJsonValid(isValid);
        
        // If the JSON is not a valid object, skip further parsing.
        if (!isValid) {
            return result;
        }
        
        if (root.has("recipe_name") && !root.get("recipe_name").isJsonNull()) {
            result.setRecipeName(root.get("recipe_name").getAsString());
        }
        if (root.has("servings_estimated") && !root.get("servings_estimated").isJsonNull()) {
            try {
                result.setServingsEstimated(root.get("servings_estimated").getAsInt());
            } catch (Exception e) {
                result.setServingsEstimated(1); // Default safe value
            }
        }

        // Amount per serving
        if (root.has("amount_per_serving") && root.get("amount_per_serving").isJsonObject()) {
            JsonObject perServing = root.getAsJsonObject("amount_per_serving");
            result.setServingCalories(getFloatOrNull(perServing, "calories"));
            result.setServingTotalFatG(getFloatOrNull(perServing, "total_fat_g"));
            result.setServingSaturatedFatG(getFloatOrNull(perServing, "saturated_fat_g"));
            result.setServingCholesterolMg(getFloatOrNull(perServing, "cholesterol_mg"));
            result.setServingSodiumMg(getFloatOrNull(perServing, "sodium_mg"));
            result.setServingCarbohydrateG(getFloatOrNull(perServing, "total_carbohydrate_g"));
            result.setServingFiberG(getFloatOrNull(perServing, "dietary_fiber_g"));
            result.setServingSugarsG(getFloatOrNull(perServing, "total_sugars_g"));
            result.setServingProteinG(getFloatOrNull(perServing, "protein_g"));
            result.setServingVitaminDMcg(getFloatOrNull(perServing, "vitamin_d_mcg"));
            result.setServingCalciumMg(getFloatOrNull(perServing, "calcium_mg"));
            result.setServingIronMg(getFloatOrNull(perServing, "iron_mg"));
            result.setServingPotassiumMg(getFloatOrNull(perServing, "potassium_mg"));
        }

        // Nutrition total (full recipe)
        if (root.has("nutrition_total") && root.get("nutrition_total").isJsonObject()) {
            JsonObject total = root.getAsJsonObject("nutrition_total");
            result.setTotalCalories(getFloatOrNull(total, "calories"));
            result.setTotalFatG(getFloatOrNull(total, "total_fat_g"));
            result.setTotalSaturatedFatG(getFloatOrNull(total, "saturated_fat_g"));
            result.setTotalCholesterolMg(getFloatOrNull(total, "total_cholesterol_mg"));
            result.setTotalSodiumMg(getFloatOrNull(total, "total_sodium_mg"));
            result.setTotalCarbohydrateG(getFloatOrNull(total, "total_carbohydrate_g"));
            result.setTotalFiberG(getFloatOrNull(total, "dietary_fiber_g"));
            result.setTotalSugarsG(getFloatOrNull(total, "total_sugars_g"));
            result.setTotalProteinG(getFloatOrNull(total, "protein_g"));
            result.setTotalVitaminDMcg(getFloatOrNull(total, "vitamin_d_mcg"));
            result.setTotalCalciumMg(getFloatOrNull(total, "calcium_mg"));
            result.setTotalIronMg(getFloatOrNull(total, "iron_mg"));
            result.setTotalPotassiumMg(getFloatOrNull(total, "potassium_mg"));
        }

        // Ingredients
        List<IngredientResult> ingredientsList = new ArrayList<>();
        if (root.has("ingredients") && root.get("ingredients").isJsonArray()) {
            JsonArray ingredientsArray = root.getAsJsonArray("ingredients");
            for (JsonElement element : ingredientsArray) {
                if (element.isJsonObject()) {
                    JsonObject ingObj = element.getAsJsonObject();
                    IngredientResult ing = new IngredientResult();
                    ing.setNameOriginal(getStringOrNull(ingObj, "ingredient_name_original"));
                    ing.setNameEn(getStringOrNull(ingObj, "ingredient_name_en"));
                    ing.setQuantityValue(getFloatOrNull(ingObj, "quantity_value"));
                    ing.setUnitOriginal(getStringOrNull(ingObj, "quantity_unit_original"));
                    ing.setUnitEn(getStringOrNull(ingObj, "quantity_unit_en"));
                    ing.setEstimatedWeightG(getFloatOrNull(ingObj, "estimated_weight_g"));

                    ing.setCalories(getFloatOrNull(ingObj, "calories"));
                    ing.setTotalFatG(getFloatOrNull(ingObj, "total_fat_g"));
                    ing.setSaturatedFatG(getFloatOrNull(ingObj, "saturated_fat_g"));
                    ing.setCholesterolMg(getFloatOrNull(ingObj, "cholesterol_mg"));
                    ing.setSodiumMg(getFloatOrNull(ingObj, "sodium_mg"));
                    ing.setTotalCarbohydrateG(getFloatOrNull(ingObj, "total_carbohydrate_g"));
                    ing.setDietaryFiberG(getFloatOrNull(ingObj, "dietary_fiber_g"));
                    ing.setTotalSugarsG(getFloatOrNull(ingObj, "total_sugars_g"));
                    ing.setProteinG(getFloatOrNull(ingObj, "protein_g"));
                    ing.setVitaminDMcg(getFloatOrNull(ingObj, "vitamin_d_mcg"));
                    ing.setCalciumMg(getFloatOrNull(ingObj, "calcium_mg"));
                    ing.setIronMg(getFloatOrNull(ingObj, "iron_mg"));
                    ing.setPotassiumMg(getFloatOrNull(ingObj, "potassium_mg"));

                    ingredientsList.add(ing);
                }
            }
        }
        result.setIngredients(ingredientsList);
        return result;
    }

    /**
     * Sanitizes raw input string to extract clean JSON object block.
     * Automatically detects and repairs truncated JSON responses from the LLM.
     */
    private String cleanJson(String rawText) {
        if (rawText == null) return "";
        String cleaned = rawText.trim();

        // Remove markdown code-block fences (```json ... ``` or ``` ... ```)
        if (cleaned.startsWith("```")) {
            int firstLineEnd = cleaned.indexOf('\n');
            if (firstLineEnd != -1) {
                cleaned = cleaned.substring(firstLineEnd).trim();
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
            }
        }

        int start = cleaned.indexOf('{');
        if (start == -1) {
            return "";
        }
        cleaned = cleaned.substring(start);

        // Find if the outer JSON object closes successfully
        int outerCloseIndex = findOuterCloseIndex(cleaned);
        if (outerCloseIndex != -1) {
            cleaned = cleaned.substring(0, outerCloseIndex + 1);
        } else {
            // Truncated JSON detected — perform automated repair.
            cleaned = repairJson(cleaned);
        }
        return cleaned;
    }

    /**
     * Fixes a very common LLM defect: a JSON string key immediately followed by
     * another string value with no `:` in between.
     *
     * Examples of what gets repaired:
     *   "quantity_unit" "grams"   ->   "quantity_unit": "grams"
     *   "quantity_unit"  123      ->   "quantity_unit": 123
     */
    private String repairMissingColons(String json) {
        if (json == null || json.isEmpty()) return json;
        // State-machine scan: after closing the quote of a key string, if the next
        // non-whitespace character is not ':', ',', '}', or ']' we inject a ':'.
        // This repairs the common LLM defect:  "quantity_unit" "grams"  ->  "quantity_unit": "grams"
        StringBuilder sb = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;
        boolean justClosedKey = false; // true after we closed a string that was a key

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (inString) {
                sb.append(c);
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                    // Check if next non-whitespace is NOT : , } ] — then it's a key needing colon
                    justClosedKey = true;
                }
            } else {
                if (justClosedKey) {
                    // Skip whitespace
                    if (Character.isWhitespace(c)) {
                        sb.append(c);
                        continue;
                    }
                    justClosedKey = false;
                    // If the next char is NOT a colon, comma, }, ] — inject a colon
                    if (c != ':' && c != ',' && c != '}' && c != ']') {
                        sb.append(':');
                    }
                }
                sb.append(c);
                if (c == '"') {
                    inString = true;
                    escaped = false;
                    justClosedKey = false; // reset; will be set when string closes
                }
            }
        }
        return sb.toString();
    }

    /**
     * Removes trailing commas before } or ] which are illegal in strict JSON
     * but commonly emitted by LLMs.
     * e.g.  {"a":1,}  ->  {"a":1}
     */
    private String repairTrailingCommas(String json) {
        if (json == null || json.isEmpty()) return json;
        // Remove commas that are immediately before a } or ], respecting strings
        StringBuilder sb = new StringBuilder();
        boolean inStr = false;
        boolean esc = false;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (inStr) {
                sb.append(c);
                if (esc) { esc = false; }
                else if (c == '\\') { esc = true; }
                else if (c == '"') { inStr = false; }
            } else {
                if (c == '"') { inStr = true; sb.append(c); }
                else if (c == ',') {
                    // Peek ahead for the next non-whitespace character
                    int j = i + 1;
                    while (j < json.length() && Character.isWhitespace(json.charAt(j))) j++;
                    if (j < json.length() && (json.charAt(j) == '}' || json.charAt(j) == ']')) {
                        // Skip this trailing comma
                        continue;
                    }
                    sb.append(c);
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Repairs missing commas between JSON elements.
     * Detects patterns where a value-ending token (closing ", }, ], true, false, null,
     * or a complete number) is immediately followed (after optional whitespace/newlines)
     * by a new element-starting token without a separating comma.
     *
     * Examples repaired:
     *   "quantity_value": 2\n"quantity_unit": "g"  ->  "quantity_value": 2,\n"quantity_unit": "g"
     *   }\n{  ->  },\n{
     */
    private String repairMissingCommas(String json) {
        if (json == null || json.isEmpty()) return json;

        StringBuilder sb = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (inString) {
                sb.append(c);
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                    // A closing quote IS a value-end — check for missing comma
                    maybeInsertComma(json, i, sb);
                }
                continue;
            }

            sb.append(c);

            if (c == '"') {
                inString = true;
                escaped = false;
                continue;
            }

            // After a value-ending character, check if we need a comma before the next element
            boolean isValueEnd = false;
            if (c == '}' || c == ']') {
                isValueEnd = true;
            } else if (Character.isDigit(c) || c == '.') {
                // Only treat as value-end if the next char is NOT part of the same number
                int peek = i + 1;
                if (peek < json.length()) {
                    char nc = json.charAt(peek);
                    if (!Character.isDigit(nc) && nc != '.' && nc != 'e' && nc != 'E'
                            && nc != '+' && nc != '-') {
                        isValueEnd = true;
                    }
                } else {
                    isValueEnd = true; // end of string
                }
            } else if (c == 'e' || c == 'l') {
                // Possible end of true/false/null
                isValueEnd = checkLiteralEnd(json, i);
            }

            if (isValueEnd) {
                maybeInsertComma(json, i, sb);
            }
        }
        return sb.toString();
    }

    /**
     * After a value-ending position, scans ahead past whitespace and injects a comma
     * if the next non-whitespace character starts a new JSON element.
     */
    private void maybeInsertComma(String json, int pos, StringBuilder sb) {
        int j = pos + 1;
        while (j < json.length() && (json.charAt(j) == ' ' || json.charAt(j) == '\t'
                || json.charAt(j) == '\r' || json.charAt(j) == '\n')) {
            j++;
        }
        if (j < json.length()) {
            char next = json.charAt(j);
            // If next non-ws char starts a new element (not a comma, colon, }, or ])
            if (next == '"' || next == '{' || next == '['
                    || next == 't' || next == 'f' || next == 'n'
                    || Character.isDigit(next) || next == '-') {
                // Verify we didn't just emit a structural character
                char lastNonWs = lastNonWhitespace(sb);
                if (lastNonWs != ':' && lastNonWs != ',' && lastNonWs != '{' && lastNonWs != '[') {
                    sb.append(',');
                }
            }
        }
    }

    /** Check if position i in json is the last char of a boolean/null literal (true/false/null). */
    private boolean checkLiteralEnd(String json, int i) {
        // Check for "true", "false", "null"
        if (i >= 3 && json.substring(i - 3, i + 1).equals("true")) return true;
        if (i >= 4 && json.substring(i - 4, i + 1).equals("false")) return true;
        if (i >= 3 && json.substring(i - 3, i + 1).equals("null")) return true;
        return false;
    }

    /** Returns the last non-whitespace character in the StringBuilder, or '\0' if none. */
    private char lastNonWhitespace(StringBuilder sb) {
        for (int i = sb.length() - 1; i >= 0; i--) {
            char c = sb.charAt(i);
            if (!Character.isWhitespace(c)) return c;
        }
        return '\0';
    }

    /**
     * Last-resort: find the rightmost valid closing '}' that balances the outermost '{'
     * and truncate everything after it, then re-attempt repair.
     */
    private String lastResortRepair(String json) {
        if (json == null || json.isEmpty()) return json;
        // Walk backwards from end looking for a position where braces are balanced
        for (int end = json.length() - 1; end >= 0; end--) {
            if (json.charAt(end) == '}') {
                String candidate = json.substring(0, end + 1);
                if (findOuterCloseIndex(candidate) != -1) {
                    return candidate;
                }
            }
        }
        // Complete fallback — return the repair attempt even if still broken
        return repairJson(json);
    }

    private int findOuterCloseIndex(String json) {
        boolean inString = false;
        boolean escaped = false;
        java.util.Stack<Character> stack = new java.util.Stack<>();
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
            } else {
                if (c == '"') {
                    inString = true;
                    escaped = false;
                } else if (c == '{') {
                    stack.push('}');
                } else if (c == '[') {
                    stack.push(']');
                } else if (c == '}' || c == ']') {
                    if (!stack.isEmpty() && stack.peek() == c) {
                        stack.pop();
                        if (stack.isEmpty()) {
                            return i;
                        }
                    }
                }
            }
        }
        return -1;
    }

    private String repairJson(String json) {
        StringBuilder sb = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;
        java.util.Stack<Character> stack = new java.util.Stack<>();
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            sb.append(c);
            
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
            } else {
                if (c == '"') {
                    inString = true;
                    escaped = false;
                } else if (c == '{') {
                    stack.push('}');
                } else if (c == '[') {
                    stack.push(']');
                } else if (c == '}' || c == ']') {
                    if (!stack.isEmpty() && stack.peek() == c) {
                        stack.pop();
                    }
                }
            }
        }
        
        if (inString) {
            if (escaped) {
                sb.setLength(sb.length() - 1);
            }
            sb.append('"');
        }
        
        String temp = sb.toString().trim();
        
        // Clean up trailing key without a value (e.g. , "key")
        if (temp.endsWith("\"")) {
            int lastQuote = temp.length() - 1;
            int prevQuote = -1;
            for (int j = lastQuote - 1; j >= 0; j--) {
                if (temp.charAt(j) == '"' && (j == 0 || temp.charAt(j - 1) != '\\')) {
                    prevQuote = j;
                    break;
                }
            }
            if (prevQuote != -1) {
                int nonWsIdx = -1;
                for (int j = prevQuote - 1; j >= 0; j--) {
                    if (!Character.isWhitespace(temp.charAt(j))) {
                        nonWsIdx = j;
                        break;
                    }
                }
                if (nonWsIdx != -1) {
                    char prevChar = temp.charAt(nonWsIdx);
                    if ((prevChar == ',' || prevChar == '{') && !stack.isEmpty() && stack.peek() == '}') {
                        temp = temp.substring(0, prevQuote).trim();
                        if (temp.endsWith(",")) {
                            temp = temp.substring(0, temp.length() - 1).trim();
                        }
                    }
                }
            }
        }
        
        // Strip trailing commas, colons or incomplete structures
        while (true) {
            if (temp.endsWith(",")) {
                temp = temp.substring(0, temp.length() - 1).trim();
            } else if (temp.endsWith(":")) {
                temp = temp + " null";
                break;
            } else {
                break;
            }
        }
        
        // Clean up partial boolean or null literals at the cut-off
        if (temp.endsWith(" tr")) temp = temp.substring(0, temp.length() - 3) + " true";
        else if (temp.endsWith(" tru")) temp = temp.substring(0, temp.length() - 4) + " true";
        else if (temp.endsWith(":tr")) temp = temp.substring(0, temp.length() - 3) + ":true";
        else if (temp.endsWith(":tru")) temp = temp.substring(0, temp.length() - 4) + ":true";
        
        else if (temp.endsWith(" fa")) temp = temp.substring(0, temp.length() - 3) + " false";
        else if (temp.endsWith(" fal")) temp = temp.substring(0, temp.length() - 4) + " false";
        else if (temp.endsWith(" fals")) temp = temp.substring(0, temp.length() - 5) + " false";
        else if (temp.endsWith(":fa")) temp = temp.substring(0, temp.length() - 3) + ":false";
        else if (temp.endsWith(":fal")) temp = temp.substring(0, temp.length() - 4) + ":false";
        else if (temp.endsWith(":fals")) temp = temp.substring(0, temp.length() - 5) + ":false";
        
        else if (temp.endsWith(" nu")) temp = temp.substring(0, temp.length() - 3) + " null";
        else if (temp.endsWith(" nul")) temp = temp.substring(0, temp.length() - 4) + " null";
        else if (temp.endsWith(":nu")) temp = temp.substring(0, temp.length() - 3) + ":null";
        else if (temp.endsWith(":nul")) temp = temp.substring(0, temp.length() - 4) + ":null";
        
        sb = new StringBuilder(temp);
        
        while (!stack.isEmpty()) {
            sb.append(stack.pop());
        }
        
        return sb.toString();
    }

    private Float getFloatOrNull(JsonObject obj, String memberName) {
        if (obj.has(memberName) && !obj.get(memberName).isJsonNull()) {
            try {
                // If it is a string representation of a float, parse it
                JsonElement element = obj.get(memberName);
                if (element.isJsonPrimitive()) {
                    return element.getAsFloat();
                }
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private String getStringOrNull(JsonObject obj, String memberName) {
        if (obj.has(memberName) && !obj.get(memberName).isJsonNull()) {
            return obj.get(memberName).getAsString();
        }
        return null;
    }
}
