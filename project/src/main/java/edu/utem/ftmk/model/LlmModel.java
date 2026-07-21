package edu.utem.ftmk.model;

/**
 * Domain model representing one row from the llm_model table.
 * Reference data for the four Ollama-hosted LLMs used in the experiment.
 */
public class LlmModel {

    private int modelId;
    private String modelName;   // Display name e.g. "Llama 3.1 8B Instruct"
    private String modelTag;    // Ollama tag   e.g. "llama3.1:8b"
    private String provider;    // e.g. "Meta", "Mistral AI"
    private String description;

    public LlmModel() {}

    public LlmModel(int modelId, String modelName, String modelTag,
                    String provider, String description) {
        this.modelId     = modelId;
        this.modelName   = modelName;
        this.modelTag    = modelTag;
        this.provider    = provider;
        this.description = description;
    }

    // ----------------------------------------------------------------
    // Getters and Setters
    // ----------------------------------------------------------------

    public int getModelId()              { return modelId; }
    public void setModelId(int v)        { this.modelId = v; }

    public String getModelName()         { return modelName; }
    public void setModelName(String v)   { this.modelName = v; }

    public String getModelTag()          { return modelTag; }
    public void setModelTag(String v)    { this.modelTag = v; }

    public String getProvider()          { return provider; }
    public void setProvider(String v)    { this.provider = v; }

    public String getDescription()       { return description; }
    public void setDescription(String v) { this.description = v; }

    @Override
    public String toString() {
        return modelName + " [" + modelTag + "]";
    }
}
