package edu.utem.ftmk.prompt;

import edu.utem.ftmk.model.PromptTechnique;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Compiles LLM prompt prompts by combining templates and replacing transcript placeholders.
 */
@Component
public class PromptEngine {

    private final PromptLoader promptLoader;

    @Autowired
    public PromptEngine(PromptLoader promptLoader) {
        this.promptLoader = promptLoader;
    }

    /**
     * Compiles system and user prompts for a given prompt technique and transcript text.
     */
    public CompiledPrompt compile(PromptTechnique technique, String transcriptText) {
        String systemTemplate = promptLoader.loadPrompt(technique.getSystemPromptFile());
        String userTemplate = promptLoader.loadPrompt(technique.getUserPromptFile());

        // Replace the transcript placeholder
        String compiledUserPrompt = userTemplate.replace("{{TRANSCRIPT}}", transcriptText);

        return new CompiledPrompt(systemTemplate, compiledUserPrompt);
    }

    /**
     * Class representing a compiled pair of system and user prompts.
     */
    public static class CompiledPrompt {
        private final String systemPrompt;
        private final String userPrompt;

        public CompiledPrompt(String systemPrompt, String userPrompt) {
            this.systemPrompt = systemPrompt;
            this.userPrompt = userPrompt;
        }

        public String getSystemPrompt() {
            return systemPrompt;
        }

        public String getUserPrompt() {
            return userPrompt;
        }
    }
}
