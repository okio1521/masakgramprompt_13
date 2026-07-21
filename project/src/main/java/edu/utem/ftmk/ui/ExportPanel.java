package edu.utem.ftmk.ui;

import edu.utem.ftmk.client.WebApiClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExportPanel extends JPanel {

    private final MainFrame mainFrame;
    private final WebApiClient apiClient;

    private final Map<String, String> layersList = new LinkedHashMap<>();
    private JLabel titleLabel;
    private JLabel subLabel;
    private final java.util.List<JPanel> rowPanels = new java.util.ArrayList<>();
    private final java.util.List<JLabel> nameLabels = new java.util.ArrayList<>();
    private final java.util.List<JLabel> descLabels = new java.util.ArrayList<>();

    public ExportPanel(MainFrame mainFrame, WebApiClient apiClient) {
        this.mainFrame = mainFrame;
        this.apiClient = apiClient;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        layersList.put("layer1a_exact_match", "Exact Match Layer (layer1a_exact_match.csv)");
        layersList.put("layer1b_text_similarity", "Text Similarity Layer (layer1b_text_similarity.csv)");
        layersList.put("layer2a_numeric_quantity", "Numeric Quantity Layer (layer2a_numeric_quantity.csv)");
        layersList.put("layer2b_numeric_nutrition", "Numeric Nutrition Layer (layer2b_numeric_nutrition.csv)");
        layersList.put("layer2c_nutrition_totals", "Nutrition Totals Layer (layer2c_nutrition_totals.csv)");
        layersList.put("layer3a_json_validity", "JSON Validity Layer (layer3a_json_validity.csv)");
        layersList.put("layer3b_hallucination", "Hallucination Layer (layer3b_hallucination.csv)");
        layersList.put("layer3c_ingredient_detection", "Ingredient Detection Layer (layer3c_ingredient_detection.csv)");
        layersList.put("layer4_human_evaluation", "Human Evaluation Layer (layer4_human_evaluation.csv) [Placeholder]");
        layersList.put("layer5_condition_scores", "Condition Scores Layer (layer5_condition_scores.csv)");

        initUI();
    }

    private void initUI() {
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setOpaque(false);

        boolean dark = com.formdev.flatlaf.FlatLaf.isLafDark();

        titleLabel = new JLabel("Database CSV Exporter Dashboard");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(dark ? Color.WHITE : Color.BLACK);

        JPanel titleAndButtonPanel = new JPanel(new BorderLayout(10, 5));
        titleAndButtonPanel.setOpaque(false);
        titleAndButtonPanel.add(titleLabel, BorderLayout.WEST);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonsPanel.setOpaque(false);

        JButton exportAllBtn = new JButton("Export All to 'evaluation/'");
        exportAllBtn.putClientProperty("JButton.buttonType", "roundRect");
        exportAllBtn.setBackground(new Color(16, 185, 129));
        exportAllBtn.setForeground(Color.WHITE);
        exportAllBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        exportAllBtn.addActionListener(e -> triggerExportAll());
        buttonsPanel.add(exportAllBtn);

        JButton runEvalBtn = new JButton("Run Python Evaluation");
        runEvalBtn.putClientProperty("JButton.buttonType", "roundRect");
        runEvalBtn.setBackground(new Color(59, 130, 246));
        runEvalBtn.setForeground(Color.WHITE);
        runEvalBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        runEvalBtn.addActionListener(e -> runPythonEvaluation());
        buttonsPanel.add(runEvalBtn);

        titleAndButtonPanel.add(buttonsPanel, BorderLayout.EAST);
        topPanel.add(titleAndButtonPanel, BorderLayout.NORTH);

        subLabel = new JLabel("Download any of the 10 evaluation layers from the server.");
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subLabel.setForeground(dark ? new Color(180, 180, 180) : new Color(80, 80, 80));
        topPanel.add(subLabel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        int index = 0;
        int total = layersList.size();
        for (Map.Entry<String, String> entry : layersList.entrySet()) {
            listPanel.add(createTableExportRow(entry.getKey(), entry.getValue()));
            index++;
            if (index < total) {
                JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
                sep.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
                listPanel.add(sep);
            }
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createTableExportRow(String fileKey, String description) {
        boolean dark = com.formdev.flatlaf.FlatLaf.isLafDark();

        JPanel row = new JPanel(new BorderLayout(15, 10));
        row.setBackground(dark ? new Color(45, 45, 45) : UIManager.getColor("Panel.background"));
        row.setBorder(new EmptyBorder(10, 15, 10, 15));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        rowPanels.add(row);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        textPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(fileKey.toUpperCase().replace("_", " "));
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        nameLabel.setForeground(dark ? Color.WHITE : Color.BLACK);
        textPanel.add(nameLabel);
        nameLabels.add(nameLabel);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        descLabel.setForeground(dark ? new Color(170, 170, 170) : new Color(100, 100, 100));
        textPanel.add(descLabel);
        descLabels.add(descLabel);

        row.add(textPanel, BorderLayout.CENTER);

        JButton exportBtn = new JButton("Export to CSV");
        exportBtn.putClientProperty("JButton.buttonType", "roundRect");
        exportBtn.setBackground(new Color(59, 130, 246));
        exportBtn.setForeground(Color.WHITE);
        exportBtn.addActionListener(e -> triggerExport(fileKey));

        row.add(exportBtn, BorderLayout.EAST);
        return row;
    }

    private void triggerExport(String fileKey) {
        new File("evaluation").mkdirs();

        JFileChooser chooser = new JFileChooser(new File("evaluation"));
        chooser.setDialogTitle("Save CSV File");
        chooser.setSelectedFile(new File(fileKey + ".csv"));

        int userSelection = chooser.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File targetFile = chooser.getSelectedFile();

        new SwingWorker<Void, Void>() {
            private String errorMessage;

            @Override
            protected Void doInBackground() {
                try (FileWriter writer = new FileWriter(targetFile)) {
                    String csv = apiClient.exportLayerToCsv(fileKey);
                    writer.write(csv);
                } catch (Exception ex) {
                    errorMessage = ex.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                if (errorMessage == null) {
                    JOptionPane.showMessageDialog(
                            ExportPanel.this,
                            "Successfully exported " + fileKey.toUpperCase() + " data to:\n" + targetFile.getAbsolutePath(),
                            "Export Succeeded",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                            ExportPanel.this,
                            "Export failed: " + errorMessage,
                            "Export Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void triggerExportAll() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "This will export all evaluation layers into the 'evaluation' folder.\nDo you want to proceed?",
                "Export All Layers",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        new File("evaluation").mkdirs();

        new SwingWorker<Void, Void>() {
            private final java.util.List<String> failedLayers = new java.util.ArrayList<>();
            private final java.util.List<String> successLayers = new java.util.ArrayList<>();

            @Override
            protected Void doInBackground() {
                for (String fileKey : layersList.keySet()) {
                    File targetFile = new File("evaluation", fileKey + ".csv");
                    try (FileWriter writer = new FileWriter(targetFile)) {
                        String csv = apiClient.exportLayerToCsv(fileKey);
                        writer.write(csv);
                        successLayers.add(fileKey);
                    } catch (Exception ex) {
                        failedLayers.add(fileKey + " (" + ex.getMessage() + ")");
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                if (failedLayers.isEmpty()) {
                    int runScript = JOptionPane.showConfirmDialog(
                            ExportPanel.this,
                            "Successfully exported all layers.\nWould you like to run the Python evaluation script now?",
                            "Export Succeeded",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );
                    if (runScript == JOptionPane.YES_OPTION) {
                        runPythonEvaluation();
                    }
                } else {
                    JOptionPane.showMessageDialog(
                            ExportPanel.this,
                            "Export completed with errors.\n\nSuccessful (" + successLayers.size() + "):\n"
                                    + String.join(", ", successLayers)
                                    + "\n\nFailed (" + failedLayers.size() + "):\n"
                                    + String.join("\n", failedLayers),
                            "Export Completed with Errors",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void runPythonEvaluation() {
        JDialog dialog = new JDialog(mainFrame, "Python Evaluation Results", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(this);

        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        textArea.setEditable(false);

        boolean dark = com.formdev.flatlaf.FlatLaf.isLafDark();
        textArea.setBackground(dark ? new Color(30, 30, 30) : Color.WHITE);
        textArea.setForeground(dark ? new Color(220, 220, 220) : Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(textArea);
        dialog.add(scrollPane, BorderLayout.CENTER);

        JLabel statusLabel = new JLabel(" Running Python evaluation script... Please wait.");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialog.add(statusLabel, BorderLayout.NORTH);

        new SwingWorker<String, Void>() {
            private int exitCode = -1;
            private String errorMessage;

            @Override
            protected String doInBackground() {
                try {
                    ProcessBuilder pb;
                    if (System.getProperty("os.name").toLowerCase().contains("win")) {
                        pb = new ProcessBuilder("cmd.exe", "/c", "py evaluate.py");
                    } else {
                        pb = new ProcessBuilder("python3", "evaluate.py");
                    }

                    pb.directory(new File("evaluation"));
                    pb.redirectErrorStream(true);

                    Process process = pb.start();
                    StringBuilder output = new StringBuilder();

                    try (java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            output.append(line).append("\n");
                        }
                    }

                    exitCode = process.waitFor();
                    return output.toString();
                } catch (Exception ex) {
                    errorMessage = ex.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                if (errorMessage != null) {
                    textArea.setText("Error running python evaluation: " + errorMessage);
                    statusLabel.setText(" Error occurred during execution.");
                    statusLabel.setForeground(Color.RED);
                    return;
                }

                try {
                    String result = get();
                    textArea.setText(result != null ? result : "");
                    textArea.setCaretPosition(0);

                    if (exitCode == 0) {
                        statusLabel.setText(" Evaluation completed successfully!");
                        statusLabel.setForeground(new Color(16, 185, 129));
                    } else {
                        statusLabel.setText(" Evaluation failed with exit code " + exitCode + ". See output below.");
                        statusLabel.setForeground(Color.RED);
                    }
                } catch (Exception ex) {
                    textArea.setText("Error running python evaluation: " + ex.getMessage());
                    statusLabel.setText(" Error occurred during execution.");
                    statusLabel.setForeground(Color.RED);
                }
            }
        }.execute();

        dialog.setVisible(true);
    }

    public void refreshThemeColors() {
        boolean dark = com.formdev.flatlaf.FlatLaf.isLafDark();

        if (titleLabel != null) {
            titleLabel.setForeground(dark ? Color.WHITE : Color.BLACK);
        }
        if (subLabel != null) {
            subLabel.setForeground(dark ? new Color(180, 180, 180) : new Color(80, 80, 80));
        }
        for (JPanel row : rowPanels) {
            row.setBackground(dark ? new Color(45, 45, 45) : UIManager.getColor("Panel.background"));
        }
        for (JLabel lbl : nameLabels) {
            lbl.setForeground(dark ? Color.WHITE : Color.BLACK);
        }
        for (JLabel lbl : descLabels) {
            lbl.setForeground(dark ? new Color(170, 170, 170) : new Color(100, 100, 100));
        }
    }
}