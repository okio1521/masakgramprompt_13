package edu.utem.ftmk;

import edu.utem.ftmk.client.WebApiClient;
import edu.utem.ftmk.ui.MainFrame;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.swing.*;

/**
 * Launches the Swing GUI after the Spring context has started.
 * The Swing client connects to the backend through WebApiClient.
 */
@Component
public class SwingAppRunner implements CommandLineRunner {

    @Value("${app.mode:combined}")
    private String appMode;

    @Value("${server.port:8080}")
    private String serverPort;

    @Override
    public void run(String... args) {
        if ("server".equalsIgnoreCase(appMode)) {
            System.out.println("=================================================");
            System.out.println("App Mode: SERVER. Spring REST API is active.");
            System.out.println("Swing GUI has been disabled for server-only mode.");
            System.out.println("=================================================");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            String serverUrl = "http://localhost:" + serverPort;

            WebApiClient apiClient = new WebApiClient(serverUrl);
            
            MainFrame frame = new MainFrame(apiClient);
            frame.start();
        });
    }
}