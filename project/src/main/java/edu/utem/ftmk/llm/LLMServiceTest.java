package edu.utem.ftmk.llm;

/**
 * Manual smoke test for verifying that all configured local LLMs
 * are reachable through LLMService.
 *
 * This is not a JUnit test. Run it manually as a Java application.
 */
public class LLMServiceTest {

    public static void main(String[] args) {
        LLMService service = new LLMService();

        String testPrompt = "Name one common cooking ingredient. Reply in one sentence.";

        String[] models = {
                LLMService.LLAMA_3_2,
                LLMService.PHI_4_MINI,
                LLMService.QWEN_2_5,
                LLMService.GEMMA_SEA_LION,
                LLMService.MED_GEMMA
        };

        for (String model : models) {
            System.out.println("--------------------------------------------------");
            System.out.println("Model    : " + model);
            System.out.println("Timeout  : " + LLMService.getTimeoutForModel(model).getSeconds() + "s");
            System.out.print("Response : ");

            try {
                String response = service.prompt(model, testPrompt);
                System.out.println(response);
            } catch (Exception e) {
                System.out.println("ERROR - " + e.getMessage());
            }
        }

        System.out.println("--------------------------------------------------");
        System.out.println("Test complete.");
    }
}