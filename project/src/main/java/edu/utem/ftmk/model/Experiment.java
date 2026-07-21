package edu.utem.ftmk.model;

import java.sql.Timestamp;

/**
 * Domain model representing an experimental run.
 * Links a transcript, an LLM, and a prompt engineering technique.
 */
public class Experiment {

    private int             experimentId;
    private int             transcriptId;
    private int             modelId;
    private int             techniqueId;
    private boolean         ragEnabled;
    private String          status; // pending, running, completed, failed
    private Timestamp       executedAt;
    private Timestamp       createdAt;
    private Integer         durationMs;

    // Joined relations for convenience in API and UI
    private LlmModel        model;
    private PromptTechnique technique;
    private Transcript      transcript;

    public Experiment() {}

    public Integer getDurationMs() { return durationMs; }
    public void setDurationMs(Integer durationMs) { this.durationMs = durationMs; }

    public int getExperimentId() { return experimentId; }
    public void setExperimentId(int experimentId) { this.experimentId = experimentId; }

    public int getTranscriptId() { return transcriptId; }
    public void setTranscriptId(int transcriptId) { this.transcriptId = transcriptId; }

    public int getModelId() { return modelId; }
    public void setModelId(int modelId) { this.modelId = modelId; }

    public int getTechniqueId() { return techniqueId; }
    public void setTechniqueId(int techniqueId) { this.techniqueId = techniqueId; }

    public boolean isRagEnabled() { return ragEnabled; }
    public void setRagEnabled(boolean ragEnabled) { this.ragEnabled = ragEnabled; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getExecutedAt() { return executedAt; }
    public void setExecutedAt(Timestamp executedAt) { this.executedAt = executedAt; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public LlmModel getModel() { return model; }
    public void setModel(LlmModel model) { this.model = model; }

    public PromptTechnique getTechnique() { return technique; }
    public void setTechnique(PromptTechnique technique) { this.technique = technique; }

    public Transcript getTranscript() { return transcript; }
    public void setTranscript(Transcript transcript) { this.transcript = transcript; }

    @Override
    public String toString() {
        return "Experiment#" + experimentId + " [Status: " + status + ", Model ID: " + modelId + "]";
    }
}
