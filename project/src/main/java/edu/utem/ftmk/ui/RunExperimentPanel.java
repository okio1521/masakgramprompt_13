package edu.utem.ftmk.ui;

import edu.utem.ftmk.client.WebApiClient;
import edu.utem.ftmk.model.Experiment;
import edu.utem.ftmk.model.LlmModel;
import edu.utem.ftmk.model.PromptTechnique;
import edu.utem.ftmk.model.Transcript;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class RunExperimentPanel extends JPanel {

    private final MainFrame mainFrame;
    private final WebApiClient apiClient;

    private JComboBox<TranscriptItem> transcriptCombo;
    private JComboBox<LlmModel> modelCombo;
    private JComboBox<PromptTechnique> techniqueCombo;
    private JCheckBox ragCheckbox;
    private JRadioButton runAllRadio;
    private JRadioButton runSelectedRadio;
    private JList<TranscriptItem> transcriptList;
    private JScrollPane transcriptScrollPane;
    private JLabel selectionStatusLabel;

    private JButton runBtn;
    private JButton cancelBtn;
    private JButton viewResultsBtn;
    private JTextArea logArea;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private SwingWorker<Experiment, String> currentWorker;

    private Experiment lastRunExperiment;
    private JPanel configPanel;

    public RunExperimentPanel(MainFrame mainFrame, WebApiClient apiClient) {
        this.mainFrame = mainFrame;
        this.apiClient = apiClient;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initUI();
        loadConfigurationData();
    }

    private void initUI() {
        configPanel = new JPanel(new GridBagLayout());
        configPanel.setBorder(BorderFactory.createTitledBorder("Experiment Configuration"));
        configPanel.setBackground(com.formdev.flatlaf.FlatLaf.isLafDark()
                ? new Color(45, 45, 45)
                : UIManager.getColor("Panel.background"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        configPanel.add(new JLabel("LLM Model (Ollama):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        modelCombo = new JComboBox<>();
        configPanel.add(modelCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.2;
        configPanel.add(new JLabel("Prompt Technique:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        techniqueCombo = new JComboBox<>();
        configPanel.add(techniqueCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.2;
        configPanel.add(new JLabel("Enhancements:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        ragCheckbox = new JCheckBox("Enable Retrieval-Augmented Generation (RAG) [Phase 2]");
        ragCheckbox.setSelected(false);
        configPanel.add(ragCheckbox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.2;
        configPanel.add(new JLabel("Execution Scope:"), gbc);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        radioPanel.setOpaque(false);

        runAllRadio = new JRadioButton("Run All Transcripts");
        runAllRadio.setSelected(true);
        runSelectedRadio = new JRadioButton("Run Selected Transcripts (Max 5)");

        ButtonGroup scopeGroup = new ButtonGroup();
        scopeGroup.add(runAllRadio);
        scopeGroup.add(runSelectedRadio);

        radioPanel.add(runAllRadio);
        radioPanel.add(Box.createHorizontalStrut(20));
        radioPanel.add(runSelectedRadio);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        configPanel.add(radioPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        configPanel.add(new JLabel("Select Transcripts:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        gbc.fill = GridBagConstraints.BOTH;

        transcriptList = new JList<>();
        transcriptList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        transcriptList.setEnabled(false);
        transcriptList.setToolTipText("Hold Ctrl (or Cmd on Mac) to select multiple transcripts");

        transcriptScrollPane = new JScrollPane(transcriptList);
        transcriptScrollPane.setPreferredSize(new Dimension(0, 105));
        configPanel.add(transcriptScrollPane, gbc);

        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.weightx = 0.8;
        gbc.insets = new Insets(0, 15, 5, 15);

        selectionStatusLabel = new JLabel("All transcripts will be processed.");
        configPanel.add(selectionStatusLabel, gbc);

        runAllRadio.addActionListener(e -> updateSelectionStatus());
        runSelectedRadio.addActionListener(e -> updateSelectionStatus());
        transcriptList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectionStatus();
            }
        });

        transcriptCombo = new JComboBox<>();

        add(configPanel, BorderLayout.NORTH);

        JPanel consolePanel = new JPanel(new BorderLayout(5, 5));
        consolePanel.setOpaque(false);
        consolePanel.add(new JLabel("Execution Console Logs:"), BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);

        boolean dark = com.formdev.flatlaf.FlatLaf.isLafDark();
        logArea.setBackground(dark ? new Color(25, 25, 25) : new Color(240, 240, 240));
        logArea.setForeground(dark ? new Color(0, 230, 115) : new Color(0, 102, 51));
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(logArea);
        consolePanel.add(scrollPane, BorderLayout.CENTER);
        add(consolePanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setOpaque(false);

        JPanel statusWrapper = new JPanel(new BorderLayout(5, 5));
        statusWrapper.setOpaque(false);

        statusLabel = new JLabel("Status: Ready to analyze.");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusWrapper.add(statusLabel, BorderLayout.NORTH);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setStringPainted(true);
        progressBar.setString("0%");
        statusWrapper.add(progressBar, BorderLayout.CENTER);

        bottomPanel.add(statusWrapper, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);

        viewResultsBtn = new JButton("View Side-by-Side Comparison");
        viewResultsBtn.putClientProperty("JButton.buttonType", "roundRect");
        viewResultsBtn.setEnabled(false);
        viewResultsBtn.addActionListener(e -> {
            if (lastRunExperiment != null) {
                mainFrame.showComparisonForTranscript(lastRunExperiment.getTranscriptId());
            }
        });
        buttonPanel.add(viewResultsBtn);

        runBtn = new JButton("Execute Analysis");
        runBtn.putClientProperty("JButton.buttonType", "roundRect");
        runBtn.setBackground(new Color(16, 185, 129));
        runBtn.setForeground(Color.WHITE);
        runBtn.addActionListener(e -> startAnalysis());
        buttonPanel.add(runBtn);

        cancelBtn = new JButton("Cancel");
        cancelBtn.putClientProperty("JButton.buttonType", "roundRect");
        cancelBtn.setBackground(new Color(220, 53, 69));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setEnabled(false);
        cancelBtn.addActionListener(e -> {
            if (currentWorker != null && !currentWorker.isDone()) {
                currentWorker.cancel(true);
                statusLabel.setText("Status: Cancellation requested, waiting for operation to stop...");
                cancelBtn.setEnabled(false);
            }
        });
        buttonPanel.add(cancelBtn);

        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void updateSelectionStatus() {
        if (runAllRadio.isSelected()) {
            transcriptList.setEnabled(false);
            selectionStatusLabel.setText("All transcripts will be processed.");
            selectionStatusLabel.setForeground(UIManager.getColor("Label.foreground"));
            runBtn.setEnabled(modelCombo.getSelectedItem() != null && techniqueCombo.getSelectedItem() != null);
        } else {
            transcriptList.setEnabled(true);
            int selectedCount = transcriptList.getSelectedIndices().length;

            if (selectedCount == 0) {
                selectionStatusLabel.setText("Please select 1 to 5 transcripts. (Hold Ctrl/Cmd to select multiple)");
                selectionStatusLabel.setForeground(new Color(245, 158, 11));
                runBtn.setEnabled(false);
            } else if (selectedCount > 5) {
                selectionStatusLabel.setText(String.format(
                        "Selected: %d / 5 (Max 5 allowed - please deselect %d). (Hold Ctrl/Cmd to select multiple)",
                        selectedCount, selectedCount - 5));
                selectionStatusLabel.setForeground(new Color(239, 68, 68));
                runBtn.setEnabled(false);
            } else {
                selectionStatusLabel.setText(String.format(
                        "Selected: %d / 5 transcripts. (Hold Ctrl/Cmd to select multiple)",
                        selectedCount));
                selectionStatusLabel.setForeground(com.formdev.flatlaf.FlatLaf.isLafDark()
                        ? new Color(0, 230, 115)
                        : new Color(0, 102, 51));
                runBtn.setEnabled(modelCombo.getSelectedItem() != null && techniqueCombo.getSelectedItem() != null);
            }
        }
    }

    private void loadConfigurationData() {
        List<Transcript> transcripts = apiClient.getAllTranscripts();
        DefaultListModel<TranscriptItem> listModel = new DefaultListModel<>();
        transcriptCombo.removeAllItems();

        int index = 1;
        for (Transcript t : transcripts) {
            TranscriptItem item = new TranscriptItem(t, index++);
            transcriptCombo.addItem(item);
            listModel.addElement(item);
        }
        transcriptList.setModel(listModel);

        List<LlmModel> models = apiClient.getAllModels();
        modelCombo.removeAllItems();
        for (LlmModel m : models) {
            modelCombo.addItem(m);
        }

        List<PromptTechnique> techniques = apiClient.getAllTechniques();
        techniqueCombo.removeAllItems();
        for (PromptTechnique pt : techniques) {
            techniqueCombo.addItem(pt);
        }

        updateSelectionStatus();
    }

    public void selectTranscript(int transcriptId) {
        for (int i = 0; i < transcriptList.getModel().getSize(); i++) {
            TranscriptItem item = transcriptList.getModel().getElementAt(i);
            if (item.t.getTranscriptId() == transcriptId) {
                transcriptList.setSelectedIndex(i);
                runSelectedRadio.setSelected(true);
                transcriptList.setEnabled(true);
                transcriptList.ensureIndexIsVisible(i);
                updateSelectionStatus();
                break;
            }
        }
    }

    public void configureForRerun(int transcriptId, int modelId, int techniqueId, boolean ragEnabled) {
        for (int i = 0; i < modelCombo.getItemCount(); i++) {
            LlmModel m = modelCombo.getItemAt(i);
            if (m.getModelId() == modelId) {
                modelCombo.setSelectedIndex(i);
                break;
            }
        }

        for (int i = 0; i < techniqueCombo.getItemCount(); i++) {
            PromptTechnique t = techniqueCombo.getItemAt(i);
            if (t.getTechniqueId() == techniqueId) {
                techniqueCombo.setSelectedIndex(i);
                break;
            }
        }

        ragCheckbox.setSelected(ragEnabled);
        runSelectedRadio.setSelected(true);
        selectTranscript(transcriptId);
    }

    private void startAnalysis() {
        List<Transcript> transcripts = new java.util.ArrayList<>();
        if (runSelectedRadio.isSelected()) {
            List<TranscriptItem> selectedItems = transcriptList.getSelectedValuesList();
            for (TranscriptItem item : selectedItems) {
                transcripts.add(item.t);
            }
        } else {
            transcripts = apiClient.getAllTranscripts();
        }

        LlmModel selectedModel = (LlmModel) modelCombo.getSelectedItem();
        PromptTechnique selectedTechnique = (PromptTechnique) techniqueCombo.getSelectedItem();
        boolean ragEnabled = ragCheckbox.isSelected();

        if (selectedModel == null || selectedTechnique == null || transcripts.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please verify model/technique is selected and transcripts are selected.",
                    "Incomplete Configuration",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        modelCombo.setEnabled(false);
        techniqueCombo.setEnabled(false);
        ragCheckbox.setEnabled(false);
        runAllRadio.setEnabled(false);
        runSelectedRadio.setEnabled(false);
        transcriptList.setEnabled(false);
        runBtn.setEnabled(false);
        viewResultsBtn.setEnabled(false);
        cancelBtn.setEnabled(true);

        logArea.setText("");
        progressBar.setIndeterminate(true);
        progressBar.setString("Analyzing...");
        statusLabel.setText("Status: Sending experiment request to server...");

        final List<Transcript> finalTranscripts = transcripts;

        currentWorker = new SwingWorker<Experiment, String>() {
            private int totalRuns = finalTranscripts.size();
            private int currentRun = 0;
            private Experiment lastResult = null;
            private int successCount = 0;

            @Override
            protected Experiment doInBackground() {
                publish("Initializing batch nutritional analysis...");
                publish("Model selected: " + selectedModel.getModelName()
                        + " (" + selectedModel.getModelTag() + ")");
                publish("Prompt technique: " + selectedTechnique.getTechniqueName());
                publish("RAG configuration: " + (ragEnabled ? "ENABLED" : "DISABLED"));
                publish("Total transcripts to process: " + finalTranscripts.size());
                publish("Total planned experiments: " + totalRuns);
                publish("--------------------------------------------");

                for (Transcript transcript : finalTranscripts) {
                    if (isCancelled()) {
                        return lastResult;
                    }

                    currentRun++;
                    int progressPercent = (currentRun * 100) / totalRuns;

                    publish(String.format("\nExperiment %d of %d (%.1f%%)",
                            currentRun, totalRuns, (currentRun * 100.0) / totalRuns));
                    publish("Target transcript: " + transcript.getFileName());
                    publish("Prompt technique: " + selectedTechnique.getTechniqueName());

                    final int runNum = currentRun;
                    final int pct = progressPercent;
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setIndeterminate(false);
                        progressBar.setValue(pct);
                        progressBar.setString("Run " + runNum + " / " + totalRuns + " (" + pct + "%)");
                        statusLabel.setText("Status: Running experiment " + runNum + " of " + totalRuns + "...");
                    });

                    long startTime = System.currentTimeMillis();
                    try {
                        lastResult = apiClient.runExperiment(
                                transcript.getTranscriptId(),
                                selectedModel.getModelId(),
                                selectedTechnique.getTechniqueId(),
                                ragEnabled
                        );

                        long duration = System.currentTimeMillis() - startTime;
                        if (lastResult != null && "completed".equalsIgnoreCase(lastResult.getStatus())) {
                            successCount++;
                            publish("Result: SUCCESS (Duration: " + (duration / 1000.0) + "s)");
                        } else if (lastResult == null || Thread.currentThread().isInterrupted()) {
                            publish("Result: CANCELLED");
                        } else {
                            publish("Result: FAILED (Duration: " + (duration / 1000.0) + "s)");
                        }
                    } catch (Exception e) {
                        if (Thread.currentThread().isInterrupted()) {
                            publish("Result: CANCELLED");
                        } else {
                            publish("Result: ERROR - " + e.getMessage());
                        }
                    }
                }

                return lastResult;
            }

            @Override
            protected void process(List<String> chunks) {
                if (isCancelled()) {
                    return;
                }
                for (String chunk : chunks) {
                    appendLog(chunk);
                }
            }

            @Override
            protected void done() {
                progressBar.setIndeterminate(false);

                if (isCancelled()) {
                    progressBar.setValue(0);
                    progressBar.setString("Cancelled");
                    statusLabel.setText("Status: Cancelled by user.");
                    appendLog("\nBatch execution cancelled by user. Completed "
                            + currentRun + " of " + totalRuns + " runs.\n");
                } else {
                    try {
                        get();
                        progressBar.setValue(100);
                        progressBar.setString("100% Done");
                        statusLabel.setText("Status: Batch completed!");
                        appendLog("\n============================================");
                        appendLog("Batch pipeline run COMPLETED!");
                        appendLog("Total successful experiments: " + successCount + " / " + totalRuns);
                        appendLog("============================================");

                        if (lastResult != null) {
                            lastRunExperiment = lastResult;
                        }
                        viewResultsBtn.setEnabled(lastRunExperiment != null);
                    } catch (Exception ex) {
                        progressBar.setValue(0);
                        progressBar.setString("Error");
                        statusLabel.setText("Status: System error occurred.");
                        appendLog("\nSystem execution error: " + ex.getMessage());
                    }
                }

                modelCombo.setEnabled(true);
                techniqueCombo.setEnabled(true);
                ragCheckbox.setEnabled(true);
                runAllRadio.setEnabled(true);
                runSelectedRadio.setEnabled(true);
                if (runSelectedRadio.isSelected()) {
                    transcriptList.setEnabled(true);
                }
                runBtn.setEnabled(true);
                cancelBtn.setEnabled(false);

                mainFrame.refreshDashboard();
                mainFrame.refreshLogsAndCharts();
            }
        };

        currentWorker.execute();
    }

    private static class TranscriptItem {
        final Transcript t;
        final int index;

        TranscriptItem(Transcript t, int index) {
            this.t = t;
            this.index = index;
        }

        @Override
        public String toString() {
            String name = t.getFileName();
            name = name.replaceFirst("^\\d+_", "");
            return index + ". " + name;
        }
    }

    private void appendLog(String text) {
        if (logArea.getDocument().getLength() > 200_000) {
            logArea.setText("");
        }
        logArea.append(text + "\n");
    }

    public void refreshThemeColors() {
        boolean dark = com.formdev.flatlaf.FlatLaf.isLafDark();
        if (configPanel != null) {
            configPanel.setBackground(dark ? new Color(45, 45, 45) : UIManager.getColor("Panel.background"));
        }
        if (logArea != null) {
            logArea.setBackground(dark ? new Color(25, 25, 25) : new Color(240, 240, 240));
            logArea.setForeground(dark ? new Color(0, 230, 115) : new Color(0, 102, 51));
        }
    }
}