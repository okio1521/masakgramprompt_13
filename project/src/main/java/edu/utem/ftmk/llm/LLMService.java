package edu.utem.ftmk.llm;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * LLMService provides a reusable interface for sending prompts to locally
 * hosted Large Language Models (LLMs) via Ollama.
 *
 * Ollama runs as a background server on the developer's machine and exposes
 * a REST API on localhost:11434. This class uses LangChain4j's OllamaChatModel
 * to communicate with that server.
 *
 * Supported models:
 * - Llama 3.2 3B
 * - Phi-4-mini
 * - Qwen 2.5 3B
 * - Gemma SEA-LION v4 4B
 * - MedGemma 4B
 */
@Service
public class LLMService {

    private static final String DEFAULT_OLLAMA_BASE_URL = "http://localhost:11434";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(1200);

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl = DEFAULT_OLLAMA_BASE_URL;

    @Value("${ollama.timeout-seconds:1200}")
    private int timeoutSeconds = 1200;

    public static final String LLAMA_3_2 = "llama3.2:3b";
    public static final String PHI_4_MINI = "phi4-mini";
    public static final String QWEN_2_5 = "qwen2.5:3b";
    public static final String GEMMA_SEA_LION = "aisingapore/Gemma-SEA-LION-v4-4B-VL";
    public static final String MED_GEMMA = "medgemma:4b";

    private static final Map<String, Duration> MODEL_TIMEOUTS;

    static {
        MODEL_TIMEOUTS = new HashMap<>();
        MODEL_TIMEOUTS.put(LLAMA_3_2, Duration.ofSeconds(1200));
        MODEL_TIMEOUTS.put(PHI_4_MINI, Duration.ofSeconds(1200));
        MODEL_TIMEOUTS.put(QWEN_2_5, Duration.ofSeconds(1200));
        MODEL_TIMEOUTS.put(GEMMA_SEA_LION, Duration.ofSeconds(1200));
        MODEL_TIMEOUTS.put(MED_GEMMA, Duration.ofSeconds(1200));
    }

    public static Duration getTimeoutForModel(String modelName) {
        return MODEL_TIMEOUTS.getOrDefault(modelName, DEFAULT_TIMEOUT);
    }

    public ChatModel buildModel(String modelName) {
        String baseUrl = (ollamaBaseUrl != null && !ollamaBaseUrl.isEmpty())
                ? ollamaBaseUrl
                : DEFAULT_OLLAMA_BASE_URL;

        Duration timeout = timeoutSeconds > 0
                ? Duration.ofSeconds(timeoutSeconds)
                : getTimeoutForModel(modelName);

        return OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .timeout(timeout)
                .temperature(0.0)
                .numPredict(4096)
                .numCtx(8192)
                .build();
    }

    public String prompt(String modelName, String userPrompt) {
        ChatModel model = buildModel(modelName);
        return model.chat(userPrompt);
    }

    public String promptWithSystem(String modelName, String systemPrompt, String userPrompt) {
        ChatModel model = buildModel(modelName);
        String safeSystemPrompt = systemPrompt != null ? systemPrompt : "";
        String safeUserPrompt = userPrompt != null ? userPrompt : "";
        String combined = safeSystemPrompt + "\n\n" + safeUserPrompt;
        return model.chat(combined);
    }
}