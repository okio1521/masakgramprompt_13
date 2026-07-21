package edu.utem.ftmk.model;

/**
 * Domain model representing one row from the prompt_technique table.
 * Stores the four prompt engineering techniques and their file paths.
 */
public class PromptTechnique {

    private int    techniqueId;
    private String techniqueName;       // e.g. "zero-shot"
    private String systemPromptFile;    // e.g. "prompts/zero_shot_system.txt"
    private String userPromptFile;      // e.g. "prompts/zero_shot_user.txt"
    private String promptVersion;
    private String description;

    public PromptTechnique() {}

    public PromptTechnique(int techniqueId, String techniqueName,
                           String systemPromptFile, String userPromptFile,
                           String promptVersion, String description) {
        this.techniqueId      = techniqueId;
        this.techniqueName    = techniqueName;
        this.systemPromptFile = systemPromptFile;
        this.userPromptFile   = userPromptFile;
        this.promptVersion    = promptVersion;
        this.description      = description;
    }

    // ----------------------------------------------------------------
    // Getters and Setters
    // ----------------------------------------------------------------

    public int getTechniqueId()                { return techniqueId; }
    public void setTechniqueId(int v)          { this.techniqueId = v; }

    public String getTechniqueName()           { return techniqueName; }
    public void setTechniqueName(String v)     { this.techniqueName = v; }

    public String getSystemPromptFile()        { return systemPromptFile; }
    public void setSystemPromptFile(String v)  { this.systemPromptFile = v; }

    public String getUserPromptFile()          { return userPromptFile; }
    public void setUserPromptFile(String v)    { this.userPromptFile = v; }

    public String getPromptVersion()           { return promptVersion; }
    public void setPromptVersion(String v)     { this.promptVersion = v; }

    public String getDescription()             { return description; }
    public void setDescription(String v)       { this.description = v; }

    @Override
    public String toString() {
        return techniqueName + " (v" + promptVersion + ")";
    }
}
