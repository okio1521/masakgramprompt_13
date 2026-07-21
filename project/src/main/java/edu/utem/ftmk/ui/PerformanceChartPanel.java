package edu.utem.ftmk.ui;

import edu.utem.ftmk.client.WebApiClient;
import edu.utem.ftmk.model.Experiment;
import edu.utem.ftmk.model.LlmModel;
import edu.utem.ftmk.model.PromptTechnique;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A panel that displays a custom-drawn grouped bar chart in Swing,
 * comparing the average execution speed (in seconds) of each LLM model
 * for each prompt engineering technique.
 */
public class PerformanceChartPanel extends JPanel {

    private final MainFrame mainFrame;
    private final WebApiClient apiClient;

    private List<LlmModel> models;
    private List<PromptTechnique> techniques;
    private List<Experiment> experiments;

    private final Map<Integer, Map<Integer, Double>> speedData = new HashMap<>();

    private ChartCanvas chartCanvas;
    private JTable summaryTable;
    private DefaultTableModel tableModel;

    private Color getTechniqueColor(PromptTechnique tech) {
        if (tech == null || tech.getTechniqueName() == null) return Color.GRAY;

        String name = tech.getTechniqueName().toLowerCase();
        if (name.contains("zero")) return new Color(59, 130, 246);
        if (name.contains("few")) return new Color(16, 185, 129);
        if (name.contains("chain")) return new Color(245, 158, 11);
        if (name.contains("struct")) return new Color(139, 92, 246);

        return Color.GRAY;
    }

    public PerformanceChartPanel(MainFrame mainFrame, WebApiClient apiClient) {
        this.mainFrame = mainFrame;
        this.apiClient = apiClient;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initUI();
    }

    private void initUI() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 5));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("LLM Speed Comparison Analytics");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh Charts");
        refreshBtn.putClientProperty("JButton.buttonType", "roundRect");
        refreshBtn.addActionListener(e -> refreshData());
        headerPanel.add(refreshBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        chartCanvas = new ChartCanvas();
        chartCanvas.setPreferredSize(new Dimension(800, 320));

        String[] columns = {"LLM Model", "Zero-Shot", "Few-Shot", "Chain-of-Thought", "Structured-Output"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        summaryTable = new JTable(tableModel);
        summaryTable.setRowHeight(25);
        summaryTable.setShowGrid(true);
        summaryTable.getTableHeader().setReorderingAllowed(false);
        summaryTable.setGridColor(com.formdev.flatlaf.FlatLaf.isLafDark()
                ? new Color(60, 60, 60)
                : new Color(220, 220, 220));

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.RIGHT);

                if (value instanceof Double) {
                    Double d = (Double) value;
                    if (d == 0.0) {
                        setText("N/A");
                        setForeground(Color.GRAY);
                    } else {
                        setText(String.format("%.2fs", d));
                        setForeground(UIManager.getColor("Label.foreground"));
                    }
                }
                return this;
            }
        };

        for (int i = 1; i < columns.length; i++) {
            summaryTable.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
        }

        JScrollPane tableScroll = new JScrollPane(summaryTable);
        tableScroll.setPreferredSize(new Dimension(800, 150));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chartCanvas, tableScroll);
        splitPane.setDividerLocation(340);
        splitPane.setResizeWeight(1.0);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        models = apiClient.getAllModels();
        techniques = apiClient.getAllTechniques();
        experiments = apiClient.getAllExperiments();

        speedData.clear();

        Map<Integer, Map<Integer, List<Double>>> durationLists = new HashMap<>();

        for (Experiment exp : experiments) {
            if ("completed".equalsIgnoreCase(exp.getStatus()) && exp.getDurationMs() != null) {
                int mId = exp.getModelId();
                int tId = exp.getTechniqueId();
                double sec = exp.getDurationMs() / 1000.0;

                durationLists.putIfAbsent(mId, new HashMap<>());
                durationLists.get(mId).putIfAbsent(tId, new ArrayList<>());
                durationLists.get(mId).get(tId).add(sec);
            }
        }

        for (LlmModel m : models) {
            speedData.put(m.getModelId(), new HashMap<>());
            Map<Integer, Double> techMap = speedData.get(m.getModelId());

            for (PromptTechnique t : techniques) {
                List<Double> list = durationLists
                        .getOrDefault(m.getModelId(), new HashMap<>())
                        .getOrDefault(t.getTechniqueId(), null);

                if (list != null && !list.isEmpty()) {
                    double sum = 0;
                    for (double val : list) {
                        sum += val;
                    }
                    techMap.put(t.getTechniqueId(), sum / list.size());
                } else {
                    techMap.put(t.getTechniqueId(), 0.0);
                }
            }
        }

        tableModel.setRowCount(0);
        for (LlmModel m : models) {
            Object[] row = new Object[5];
            row[0] = m.getModelName();

            Map<Integer, Double> techMap = speedData.get(m.getModelId());

            for (int i = 0; i < techniques.size() && i < 4; i++) {
                PromptTechnique tech = techniques.get(i);
                row[i + 1] = techMap.getOrDefault(tech.getTechniqueId(), 0.0);
            }

            tableModel.addRow(row);
        }

        chartCanvas.repaint();
    }

    public void refreshThemeColors() {
        boolean dark = com.formdev.flatlaf.FlatLaf.isLafDark();

        if (chartCanvas != null) {
            chartCanvas.setBackground(dark ? new Color(40, 40, 40) : new Color(248, 249, 250));
            chartCanvas.repaint();
        }

        if (summaryTable != null) {
            summaryTable.setGridColor(dark ? new Color(60, 60, 60) : new Color(220, 220, 220));
        }
    }

    private class ChartCanvas extends JPanel {

        public ChartCanvas() {
            setBackground(com.formdev.flatlaf.FlatLaf.isLafDark()
                    ? new Color(40, 40, 40)
                    : new Color(248, 249, 250));
            setBorder(BorderFactory.createEtchedBorder());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            if (models == null || models.isEmpty() || speedData.isEmpty()) {
                g2.setColor(UIManager.getColor("Label.foreground"));
                g2.drawString("No execution data available. Please run some experiments first.", width / 2 - 180, height / 2);
                g2.dispose();
                return;
            }

            boolean dark = com.formdev.flatlaf.FlatLaf.isLafDark();
            Color axisColor = dark ? new Color(180, 180, 180) : new Color(80, 80, 80);
            Color gridColor = dark ? new Color(65, 65, 65) : new Color(225, 225, 225);
            Color textColor = UIManager.getColor("Label.foreground");

            int paddingLeft = 70;
            int paddingRight = 40;
            int paddingTop = 45;
            int paddingBottom = 50;

            int plotWidth = width - paddingLeft - paddingRight;
            int plotHeight = height - paddingTop - paddingBottom;

            double maxSpeed = 10.0;
            for (Map<Integer, Double> techMap : speedData.values()) {
                for (double speed : techMap.values()) {
                    if (speed > maxSpeed) {
                        maxSpeed = speed;
                    }
                }
            }
            maxSpeed = Math.ceil(maxSpeed / 5.0) * 5.0;

            int gridTicks = 5;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            for (int i = 0; i <= gridTicks; i++) {
                double value = (maxSpeed / gridTicks) * i;
                int y = paddingTop + plotHeight - (int) ((value / maxSpeed) * plotHeight);

                g2.setColor(gridColor);
                g2.drawLine(paddingLeft, y, paddingLeft + plotWidth, y);

                g2.setColor(textColor);
                String label = String.format("%.1fs", value);
                int labelWidth = g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, paddingLeft - labelWidth - 8, y + 4);
            }

            g2.setColor(axisColor);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(paddingLeft, paddingTop + plotHeight, paddingLeft + plotWidth, paddingTop + plotHeight);
            g2.drawLine(paddingLeft, paddingTop, paddingLeft, paddingTop + plotHeight);

            int numModels = models.size();
            int numTechs = Math.min(techniques.size(), 4);
            double groupWidth = (double) plotWidth / numModels;
            double barSpacing = 2.0;

            for (int mIdx = 0; mIdx < numModels; mIdx++) {
                LlmModel m = models.get(mIdx);
                double groupX = paddingLeft + (mIdx * groupWidth);

                g2.setColor(textColor);
                g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                String modelLabel = m.getModelName();

                if (modelLabel.contains("Instruct")) {
                    modelLabel = modelLabel.replace(" Instruct", "");
                }
                if (modelLabel.contains("Gemma-SEA-LION")) {
                    modelLabel = "SEA-LION 4B";
                }

                int modelLabelWidth = g2.getFontMetrics().stringWidth(modelLabel);
                g2.drawString(modelLabel,
                        (int) (groupX + (groupWidth - modelLabelWidth) / 2),
                        paddingTop + plotHeight + 18);

                double availableWidth = groupWidth * 0.85;
                double startX = groupX + (groupWidth - availableWidth) / 2;
                double barWidth = (availableWidth / numTechs) - barSpacing;

                Map<Integer, Double> techMap = speedData.get(m.getModelId());

                for (int tIdx = 0; tIdx < numTechs; tIdx++) {
                    PromptTechnique tech = techniques.get(tIdx);
                    double duration = techMap.getOrDefault(tech.getTechniqueId(), 0.0);

                    if (duration > 0.0) {
                        int barHeight = (int) ((duration / maxSpeed) * plotHeight);
                        int x = (int) (startX + (tIdx * (barWidth + barSpacing)));
                        int y = paddingTop + plotHeight - barHeight;

                        g2.setColor(getTechniqueColor(tech));
                        g2.fillRect(x, y, (int) barWidth, barHeight);

                        g2.setColor(dark ? new Color(255, 255, 255, 50) : new Color(0, 0, 0, 50));
                        g2.drawRect(x, y, (int) barWidth, barHeight);

                        if (barHeight > 18) {
                            g2.setColor(Color.WHITE);
                            g2.setFont(new Font("SansSerif", Font.BOLD, 8));
                            String valStr = String.format("%.1f", duration);
                            int valWidth = g2.getFontMetrics().stringWidth(valStr);
                            g2.drawString(valStr, (int) (x + (barWidth - valWidth) / 2), y + 12);
                        }
                    }
                }
            }

            g2.setColor(textColor);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString("Average LLM Processing Time per Prompt Technique (Lower is Better)",
                    paddingLeft + 10, paddingTop - 22);

            int legendX = width - paddingRight - 450;
            int legendY = paddingTop - 25;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));

            for (int tIdx = 0; tIdx < numTechs; tIdx++) {
                PromptTechnique tech = techniques.get(tIdx);

                g2.setColor(getTechniqueColor(tech));
                g2.fillRect(legendX, legendY, 12, 12);

                g2.setColor(textColor);
                g2.drawRect(legendX, legendY, 12, 12);

                String techName = tech.getTechniqueName();
                g2.drawString(techName, legendX + 16, legendY + 10);

                legendX += 110;
            }

            g2.dispose();
        }
    }
}