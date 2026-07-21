package edu.utem.ftmk.client;

import com.formdev.flatlaf.FlatDarkLaf;
import edu.utem.ftmk.ui.MainFrame;

import javax.swing.*;

public class SwingClientApplication {

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");

        String serverUrl = System.getProperty("server.url", "http://localhost:8080");

        System.out.println("================================================");
        System.out.println("Starting MasakGramPrompt in PURE CLIENT mode.");
        System.out.println("Connecting to Server REST API at: " + serverUrl);
        System.out.println("================================================");

        WebApiClient apiClient = new WebApiClient(serverUrl);

        
        SwingUtilities.invokeLater(() -> {
            try {
                FlatDarkLaf.setup();

                MainFrame frame = new MainFrame(apiClient);

                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            } catch (Exception e) {
                System.err.println("Failed to start Swing Client GUI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}