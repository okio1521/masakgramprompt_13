package edu.utem.ftmk.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import edu.utem.ftmk.client.WebApiClient;
import edu.utem.ftmk.model.Experiment;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final WebApiClient apiClient;
    
    private JTabbedPane tabbedPane;
    private DashboardPanel dashboardPanel;
    private RunExperimentPanel runExperimentPanel;
    private NutritionalFactSheetPanel nutritionalFactSheetPanel;
    private ExportPanel exportPanel;
    private ExecutionLogsPanel executionLogsPanel;
    private PerformanceChartPanel performanceChartPanel;

    private JMenuBar menuBar;
    private boolean isDarkMode = true;

    public MainFrame(WebApiClient apiClient) {
        this.apiClient = apiClient;
        
        FlatDarkLaf.setup();

        setTitle("MasakGramPrompt — Nutritional LLM Analysis System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        menuBar = new JMenuBar();

        JMenu viewMenu = new JMenu("View");
        JMenuItem toggleThemeItem = new JMenuItem("Toggle Light/Dark Theme");
        toggleThemeItem.addActionListener(e -> toggleTheme());
        viewMenu.add(toggleThemeItem);
        menuBar.add(viewMenu);

        setJMenuBar(menuBar);

        tabbedPane = new JTabbedPane();
        tabbedPane.setTabPlacement(JTabbedPane.LEFT);

        dashboardPanel = new DashboardPanel(this, apiClient);
        tabbedPane.addTab("Dashboard", dashboardPanel);

        runExperimentPanel = new RunExperimentPanel(this, apiClient);
        tabbedPane.addTab("Run Experiment", runExperimentPanel);

        nutritionalFactSheetPanel = new NutritionalFactSheetPanel(this, apiClient);
        tabbedPane.addTab("Comparison Facts", nutritionalFactSheetPanel);

        exportPanel = new ExportPanel(this, apiClient);
        tabbedPane.addTab("CSV Exporter", exportPanel);

        executionLogsPanel = new ExecutionLogsPanel(this, apiClient);
        tabbedPane.addTab("Execution Logs", executionLogsPanel);

        performanceChartPanel = new PerformanceChartPanel(this, apiClient);
        tabbedPane.addTab("Performance Charts", performanceChartPanel);

        getContentPane().add(tabbedPane, BorderLayout.CENTER);
    }

    public void showRunExperimentForTranscript(int transcriptId) {
        runExperimentPanel.selectTranscript(transcriptId);
        tabbedPane.setSelectedComponent(runExperimentPanel);
    }

    public void showComparisonForTranscript(int transcriptId) {
        nutritionalFactSheetPanel.selectTranscriptAndExperiment(transcriptId);
        tabbedPane.setSelectedComponent(nutritionalFactSheetPanel);
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        try {
            if (isDarkMode) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
            }

            SwingUtilities.updateComponentTreeUI(this);

            if (dashboardPanel != null) dashboardPanel.reloadData();
            if (runExperimentPanel != null) runExperimentPanel.refreshThemeColors();
            if (nutritionalFactSheetPanel != null) nutritionalFactSheetPanel.refreshThemeColors();
            if (exportPanel != null) exportPanel.refreshThemeColors();
            if (executionLogsPanel != null) executionLogsPanel.refreshData();
            if (performanceChartPanel != null) {
                performanceChartPanel.refreshThemeColors();
                performanceChartPanel.refreshData();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void start() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }

    public void refreshDashboard() {
        if (dashboardPanel != null) {
            dashboardPanel.reloadData();
        }
    }

    public void rerunExperiment(Experiment exp) {
        runExperimentPanel.configureForRerun(
                exp.getTranscriptId(),
                exp.getModelId(),
                exp.getTechniqueId(),
                exp.isRagEnabled()
        );
        tabbedPane.setSelectedComponent(runExperimentPanel);
    }

    public void refreshLogsAndCharts() {
        if (executionLogsPanel != null) {
            executionLogsPanel.refreshData();
        }
        if (performanceChartPanel != null) {
            performanceChartPanel.refreshData();
        }
    }

    public WebApiClient getApiClient() {
        return apiClient;
    }


}