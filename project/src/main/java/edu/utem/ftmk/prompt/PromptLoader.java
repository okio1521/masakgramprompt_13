package edu.utem.ftmk.prompt;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Loads prompt template contents from classpath resources.
 */
@Component
public class PromptLoader {

    /**
     * Reads a resource file from classpath and returns its content as a String.
     * Handles both relative prompts/ paths and absolute resources.
     */
    public String loadPrompt(String relativePath) {
        try {
            ClassPathResource resource = new ClassPathResource(relativePath);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load prompt resource at path: " + relativePath, e);
        }
    }
}
