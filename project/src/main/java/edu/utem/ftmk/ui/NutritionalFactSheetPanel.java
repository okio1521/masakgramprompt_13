package edu.utem.ftmk.ui;

import edu.utem.ftmk.client.WebApiClient;
import edu.utem.ftmk.model.Experiment;
import edu.utem.ftmk.model.IngredientResult;
import edu.utem.ftmk.model.NutritionResult;
import edu.utem.ftmk.model.Transcript;

import javax.swing.*;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class NutritionalFactSheetPanel extends JPanel {

    private final MainFrame mainFrame;
    private final WebApiClient apiClient;

    private JComboBox<TranscriptItem> transcriptCombo;
    private JComboBox<ExperimentItem> experimentCombo;

    private JTextPane groundTruthLabel;
    private JTextPane llmLabel;

    private JTable comparisonTable;
    private DefaultTableModel comparisonModel;
    private JPanel topBar;

    public NutritionalFactSheetPanel(MainFrame mainFrame, WebApiClient apiClient) {
        this.mainFrame = mainFrame;
        this.apiClient = apiClient;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initUI();
        refreshData();
    }

    private void initUI() {
        topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topBar.setBackground(com.formdev.flatlaf.FlatLaf.isLafDark()
                ? new Color(45, 45, 45)
                : UIManager.getColor("Panel.background"));

        topBar.add(new JLabel("Transcript / Reel:"));
        transcriptCombo = new JComboBox<>();
        transcriptCombo.setPreferredSize(new Dimension(300, 25));
        transcriptCombo.addActionListener(e -> onTranscriptSelected());
        topBar.add(transcriptCombo);

        topBar.add(new JLabel("Experiment Condition:"));
        experimentCombo = new JComboBox<>();
        experimentCombo.setPreferredSize(new Dimension(350, 25));
        experimentCombo.addActionListener(e -> onExperimentSelected());
        topBar.add(experimentCombo);

        add(topBar, BorderLayout.NORTH);

        JPanel labelsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        labelsPanel.setOpaque(false);

        groundTruthLabel = createFactSheetPane("GROUND TRUTH NUTRITION FACT SHEET");
        llmLabel = createFactSheetPane("LLM EXTRACTED NUTRITIONAL RESULT");

        labelsPanel.add(new JScrollPane(groundTruthLabel));
        labelsPanel.add(new JScrollPane(llmLabel));

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setOpaque(false);

        JPanel tableHeaderPanel = new JPanel(new BorderLayout(10, 0));
        tableHeaderPanel.setOpaque(false);
        tableHeaderPanel.add(new JLabel("Ingredient Extractions & Nutrition Comparison:"), BorderLayout.WEST);

        JButton resetFilterBtn = new JButton("Reset Filter");
        resetFilterBtn.addActionListener(e -> {
            TableRowSorter<DefaultTableModel> sorter =
                    (TableRowSorter<DefaultTableModel>) comparisonTable.getRowSorter();
            if (sorter != null) {
                sorter.setRowFilter(null);
            }
        });
        tableHeaderPanel.add(resetFilterBtn, BorderLayout.EAST);
        bottomPanel.add(tableHeaderPanel, BorderLayout.NORTH);

        String[] cols = {
                "Ingredient Name (Original)", "Translation (EN)", "Qty", "Unit",
                "Weight (g)", "Calories (kcal)", "Protein (g)", "Carbs (g)", "Fat (g)", "Source"
        };

        comparisonModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        comparisonTable = new JTable(comparisonModel);
        comparisonTable.setRowHeight(25);
        comparisonTable.getTableHeader().setReorderingAllowed(false);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(comparisonModel);
        comparisonTable.setRowSorter(sorter);

        comparisonTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = comparisonTable.getSelectedRow();
                    if (row != -1) {
                        int modelRow = comparisonTable.convertRowIndexToModel(row);
                        String origName = (String) comparisonModel.getValueAt(modelRow, 0);
                        String enName = (String) comparisonModel.getValueAt(modelRow, 1);
                        filterTable(origName, enName);
                    }
                }
            }
        });

        comparisonTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object val, boolean isSelected, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(table, val, isSelected, hasFocus, r, c);

                if (c == 2) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                String source = (String) table.getValueAt(r, 9);
                boolean dark = com.formdev.flatlaf.FlatLaf.isLafDark();

                if ("LLM (HALLUCINATED)".equalsIgnoreCase(source)) {
                    if (dark) {
                        comp.setBackground(new Color(90, 30, 30));
                        comp.setForeground(Color.WHITE);
                    } else {
                        comp.setBackground(new Color(255, 204, 204));
                        comp.setForeground(Color.BLACK);
                    }
                } else if ("LLM".equalsIgnoreCase(source)) {
                    if (dark) {
                        comp.setBackground(new Color(40, 50, 45));
                        comp.setForeground(Color.WHITE);
                    } else {
                        comp.setBackground(new Color(204, 255, 204));
                        comp.setForeground(Color.BLACK);
                    }
                } else {
                    if (dark) {
                        comp.setBackground(new Color(45, 45, 45));
                        comp.setForeground(Color.WHITE);
                    } else {
                        comp.setBackground(Color.WHITE);
                        comp.setForeground(Color.BLACK);
                    }
                }

                if (isSelected) {
                    comp.setBackground(table.getSelectionBackground());
                    comp.setForeground(table.getSelectionForeground());
                }

                return comp;
            }
        });

        bottomPanel.add(new JScrollPane(comparisonTable), BorderLayout.CENTER);

        JTabbedPane sheetTabbedPane = new JTabbedPane();
        sheetTabbedPane.addTab("Nutrition Fact Sheets", labelsPanel);
        sheetTabbedPane.addTab("Ingredient Details & Comparison", bottomPanel);

        add(sheetTabbedPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        new SwingWorker<List<Transcript>, Void>() {
            private String errorMessage;

            @Override
            protected List<Transcript> doInBackground() {
                try {
                    return apiClient.getAllTranscripts();
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                    return new ArrayList<>();
                }
            }

            @Override
            protected void done() {
                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(
                            NutritionalFactSheetPanel.this,
                            "Failed to load transcripts: " + errorMessage,
                            "Load Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                try {
                    List<Transcript> transcripts = get();
                    loadTranscripts(transcripts);
                    if (transcriptCombo.getItemCount() > 0) {
                        transcriptCombo.setSelectedIndex(0);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            NutritionalFactSheetPanel.this,
                            "Failed to populate transcripts: " + e.getMessage(),
                            "Load Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void filterTable(String origName, String enName) {
        TableRowSorter<DefaultTableModel> sorter =
                (TableRowSorter<DefaultTableModel>) comparisonTable.getRowSorter();
        if (sorter == null) return;

        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        if (origName != null && !origName.trim().isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(origName.trim()), 0));
        }
        if (enName != null && !enName.trim().isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(enName.trim()), 1));
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.orFilter(filters));
        }
    }

    private JTextPane createFactSheetPane(String title) {
        JTextPane pane = new JTextPane();
        pane.setContentType("text/html");
        pane.setEditable(false);

        boolean dark = com.formdev.flatlaf.FlatLaf.isLafDark();
        pane.setBackground(dark ? new Color(35, 35, 35) : new Color(245, 245, 245));
        pane.setMargin(new Insets(15, 15, 15, 15));

        String textColor = dark ? "white" : "black";
        pane.setText("<html><body style='font-family:sans-serif; color:" + textColor
                + ";'><h3>" + title
                + "</h3><p>Select a transcript and completed experiment above to view facts.</p></body></html>");
        return pane;
    }

    public void loadTranscripts(List<Transcript> transcripts) {
        transcriptCombo.removeAllItems();
        int index = 1;
        for (Transcript t : transcripts) {
            transcriptCombo.addItem(new TranscriptItem(t, index++));
        }
    }

    public void selectTranscriptAndExperiment(int transcriptId) {
        for (int i = 0; i < transcriptCombo.getItemCount(); i++) {
            TranscriptItem item = transcriptCombo.getItemAt(i);
            if (item.t.getTranscriptId() == transcriptId) {
                transcriptCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void onTranscriptSelected() {
        TranscriptItem item = (TranscriptItem) transcriptCombo.getSelectedItem();
        if (item == null) return;

        try {
            List<Experiment> allExps = apiClient.getAllExperiments();
            experimentCombo.removeAllItems();

            for (Experiment exp : allExps) {
                if (exp.getTranscriptId() == item.t.getTranscriptId()
                        && "completed".equalsIgnoreCase(exp.getStatus())) {
                    experimentCombo.addItem(new ExperimentItem(exp));
                }
            }

            if (experimentCombo.getItemCount() == 0) {
                boolean dark = com.formdev.flatlaf.FlatLaf.isLafDark();
                String textColor = dark ? "white" : "black";

                groundTruthLabel.setText("<html><body style='font-family:sans-serif; color:" + textColor
                        + ";'><h3>GROUND TRUTH NUTRITION</h3><p>Ground truth available, but no completed LLM experiments exist yet for this transcript.</p></body></html>");
                llmLabel.setText("<html><body style='font-family:sans-serif; color:" + textColor
                        + ";'><h3>LLM ANALYSIS RESULT</h3><p>Run an experiment first using the 'Run Experiment' tab.</p></body></html>");
                comparisonModel.setRowCount(0);
            } else {
                experimentCombo.setSelectedIndex(0);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to load experiments: " + e.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void onExperimentSelected() {
        TranscriptItem transItem = (TranscriptItem) transcriptCombo.getSelectedItem();
        ExperimentItem expItem = (ExperimentItem) experimentCombo.getSelectedItem();
        boolean dark = com.formdev.flatlaf.FlatLaf.isLafDark();
        String textColor = dark ? "white" : "black";

        if (transItem == null) {
            groundTruthLabel.setText("<html><body style='font-family:sans-serif; color:" + textColor
                    + ";'><h3>GROUND TRUTH NUTRITION FACT SHEET</h3><p>Select a transcript and completed experiment above to view facts.</p></body></html>");
            llmLabel.setText("<html><body style='font-family:sans-serif; color:" + textColor
                    + ";'><h3>LLM EXTRACTED NUTRITIONAL RESULT</h3><p>Select a transcript and completed experiment above to view facts.</p></body></html>");
            comparisonModel.setRowCount(0);
            return;
        }

        if (expItem == null) {
            groundTruthLabel.setText("<html><body style='font-family:sans-serif; color:" + textColor
                    + ";'><h3>GROUND TRUTH NUTRITION</h3><p>Ground truth available, but no completed LLM experiments exist yet for this transcript.</p></body></html>");
            llmLabel.setText("<html><body style='font-family:sans-serif; color:" + textColor
                    + ";'><h3>LLM ANALYSIS RESULT</h3><p>Run an experiment first using the 'Run Experiment' tab.</p></body></html>");
            comparisonModel.setRowCount(0);
            return;
        }

        try {
            NutritionResult gtResult = apiClient.getGroundTruthByTranscriptId(transItem.t.getTranscriptId());
            NutritionResult llmResult = apiClient.getNutritionResultByExperimentId(expItem.exp.getExperimentId());

            if (llmResult != null) {
                llmResult.setIngredients(apiClient.getIngredientResultsByResultId(llmResult.getResultId()));
            }

            displayNutritionLabel(groundTruthLabel, "GROUND TRUTH NUTRITION SHEET", gtResult);
            displayNutritionLabel(llmLabel, "LLM ANALYSIS SHEET (" + safeExperimentLabel(expItem.exp) + ")", llmResult);

            populateIngredientComparison(gtResult, llmResult);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to load comparison data: " + e.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String safeExperimentLabel(Experiment exp) {
        if (exp == null) return "Unknown Model";
        if (exp.getModel() != null && exp.getModel().getModelName() != null) {
            return exp.getModel().getModelName();
        }
        return "Model " + exp.getModelId();
    }

    private void displayNutritionLabel(JTextPane pane, String title, NutritionResult nr) {
        boolean dark = com.formdev.flatlaf.FlatLaf.isLafDark();
        String textColor = dark ? "white" : "black";
        String borderColor = dark ? "white" : "black";

        if (nr == null) {
            pane.setText("<html><body style='font-family:sans-serif; color:" + textColor
                    + ";'><h3>" + title + "</h3><p>Data not found.</p></body></html>");
            return;
        }

        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family:sans-serif; color:").append(textColor).append("; margin:10px;'>");
        html.append("<h2 style='margin-bottom:2px;'>").append(title).append("</h2>");
        html.append("<div style='border-top:5px solid ").append(borderColor).append("; margin-top:2px; margin-bottom:5px;'></div>");
        html.append("<b>Recipe Name:</b> ").append(nr.getRecipeName() != null ? nr.getRecipeName() : "N/A").append("<br/>");
        html.append("<b>Servings Estimated:</b> ").append(nr.getServingsEstimated()).append("<br/>");
        html.append("<div style='border-top:3px solid ").append(borderColor).append("; margin-top:5px; margin-bottom:5px;'></div>");
        html.append("<span style='font-size:12px;'>Amount Per Serving</span><br/>");
        html.append("<div style='display:flex; justify-content:space-between; font-size:16px;'><b>Calories:</b> <b>")
                .append(formatFloat(nr.getServingCalories())).append(" kcal</b></div>");
        html.append("<div style='border-top:1px solid ").append(borderColor).append("; margin-top:5px; margin-bottom:5px;'></div>");

        html.append("<table style='width:100%; border-collapse:collapse; color:").append(textColor).append(";'>");
        html.append("<tr><td><b>Total Fat</b></td><td style='text-align:right;'><b>").append(formatFloat(nr.getServingTotalFatG())).append(" g</b></td></tr>");
        html.append("<tr><td>&nbsp;&nbsp;&nbsp;&nbsp;Saturated Fat</td><td style='text-align:right;'>").append(formatFloat(nr.getServingSaturatedFatG())).append(" g</td></tr>");
        html.append("<tr><td><b>Cholesterol</b></td><td style='text-align:right;'><b>").append(formatFloat(nr.getServingCholesterolMg())).append(" mg</b></td></tr>");
        html.append("<tr><td><b>Sodium</b></td><td style='text-align:right;'><b>").append(formatFloat(nr.getServingSodiumMg())).append(" mg</b></td></tr>");
        html.append("<tr><td><b>Total Carbohydrates</b></td><td style='text-align:right;'><b>").append(formatFloat(nr.getServingCarbohydrateG())).append(" g</b></td></tr>");
        html.append("<tr><td>&nbsp;&nbsp;&nbsp;&nbsp;Dietary Fiber</td><td style='text-align:right;'>").append(formatFloat(nr.getServingFiberG())).append(" g</td></tr>");
        html.append("<tr><td>&nbsp;&nbsp;&nbsp;&nbsp;Total Sugars</td><td style='text-align:right;'>").append(formatFloat(nr.getServingSugarsG())).append(" g</td></tr>");
        html.append("<tr><td><b>Protein</b></td><td style='text-align:right;'><b>").append(formatFloat(nr.getServingProteinG())).append(" g</b></td></tr>");
        html.append("</table>");

        html.append("<div style='border-top:3px solid ").append(borderColor).append("; margin-top:5px; margin-bottom:5px;'></div>");
        html.append("<table style='width:100%; border-collapse:collapse; color:").append(textColor).append("; font-size:11px;'>");
        html.append("<tr><td>Vitamin D: ").append(formatFloat(nr.getServingVitaminDMcg())).append(" mcg</td><td style='text-align:right;'>Calcium: ").append(formatFloat(nr.getServingCalciumMg())).append(" mg</td></tr>");
        html.append("<tr><td>Iron: ").append(formatFloat(nr.getServingIronMg())).append(" mg</td><td style='text-align:right;'>Potassium: ").append(formatFloat(nr.getServingPotassiumMg())).append(" mg</td></tr>");
        html.append("</table>");
        html.append("</body></html>");

        pane.setText(html.toString());
    }

    private void populateIngredientComparison(NutritionResult gt, NutritionResult llm) {
        comparisonModel.setRowCount(0);

        if (gt != null && gt.getIngredients() != null) {
            for (IngredientResult ing : gt.getIngredients()) {
                comparisonModel.addRow(new Object[]{
                        ing.getNameOriginal(),
                        ing.getNameEn(),
                        formatFloat(ing.getQuantityValue()),
                        ing.getUnitOriginal(),
                        formatFloat(ing.getEstimatedWeightG()),
                        formatFloat(ing.getCalories()),
                        formatFloat(ing.getProteinG()),
                        formatFloat(ing.getTotalCarbohydrateG()),
                        formatFloat(ing.getTotalFatG()),
                        "GT"
                });
            }
        }

        if (llm != null && llm.getIngredients() != null) {
            for (IngredientResult ing : llm.getIngredients()) {
                boolean hallucinated = ing.getIsHallucinated() != null && ing.getIsHallucinated();
                comparisonModel.addRow(new Object[]{
                        ing.getNameOriginal(),
                        ing.getNameEn(),
                        formatFloat(ing.getQuantityValue()),
                        ing.getUnitOriginal(),
                        formatFloat(ing.getEstimatedWeightG()),
                        formatFloat(ing.getCalories()),
                        formatFloat(ing.getProteinG()),
                        formatFloat(ing.getTotalCarbohydrateG()),
                        formatFloat(ing.getTotalFatG()),
                        hallucinated ? "LLM (HALLUCINATED)" : "LLM"
                });
            }
        }
    }

    private String formatFloat(Float f) {
        if (f == null) return "0.0";
        return String.format("%.1f", f);
    }

    public void refreshThemeColors() {
        boolean dark = com.formdev.flatlaf.FlatLaf.isLafDark();
        Color bg = dark ? new Color(35, 35, 35) : new Color(245, 245, 245);

        groundTruthLabel.setBackground(bg);
        llmLabel.setBackground(bg);

        if (topBar != null) {
            topBar.setBackground(dark ? new Color(45, 45, 45) : UIManager.getColor("Panel.background"));
        }

        onExperimentSelected();
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
            return index + ". " + t.getFileName();
        }
    }

    private static class ExperimentItem {
        final Experiment exp;

        ExperimentItem(Experiment exp) {
            this.exp = exp;
        }

        @Override
        public String toString() {
            String modelName = exp.getModel() != null && exp.getModel().getModelName() != null
                    ? exp.getModel().getModelName()
                    : "Model " + exp.getModelId();

            String techniqueName = exp.getTechnique() != null && exp.getTechnique().getTechniqueName() != null
                    ? exp.getTechnique().getTechniqueName()
                    : "Technique " + exp.getTechniqueId();

            return modelName + " (" + techniqueName + ")" + (exp.isRagEnabled() ? " + RAG" : "");
        }
    }
}
