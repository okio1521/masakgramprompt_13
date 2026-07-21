package edu.utem.ftmk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class.
 * Configured to boot the REST backend on the default port 8080.
 */
@SpringBootApplication
public class MasakGramApplication {

    public static void main(String[] args) {
        // Run with headless disabled so Swing can start if run in the same VM, or separately.
        System.setProperty("java.awt.headless", "false");
        SpringApplication.run(MasakGramApplication.class, args);
    }
}
