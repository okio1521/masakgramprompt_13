package edu.utem.ftmk.ui;

import edu.utem.ftmk.client.WebApiClient;
import edu.utem.ftmk.model.Experiment;

import javax.swing.*;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * A panel that displays the execution logs/history of LLM experiments,
 * showing whether each run succeeded/failed, execution time, models, techniques, etc.
 */
public class ExecutionLogsPanel extends JPanel {

    private final MainFrame mainFrame;
    private final WebApiClient apiClient;

    private JTable logsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton rerunBtn;
    private JButton refreshBtn;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ExecutionLogsPanel(MainFrame mainFrame, WebApiClient apiClient) {
        this.mainFrame = mainFrame;
        this.apiClient = apiClient;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initUI();
    }

    private void initUI() {
        JPanel topBar = new JPanel(new BorderLayout(15, 10));
        topBar.setOpaque(false);

        JLabel titleLabel = new JLabel("LLM Experiment History & Execution Logs");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        topBar.add(titleLabel, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel searchLabel = new JLabel("Filter logs: ");
        searchLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(0, 0, 0, 10);
        filterPanel.add(searchLabel, gbc);

        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Search by model, technique, transcript, status...");
        searchField.addCaretListener(e -> filterLogsTable());
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 15);
        filterPanel.add(searchField, gbc);

        refreshBtn = new JButton("Refresh Logs");
        refreshBtn.putClientProperty("JButton.buttonType", "roundRect");
        refreshBtn.addActionListener(e -> refreshData());
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        filterPanel.add(refreshBtn, gbc);

        topBar.add(filterPanel, BorderLayout.CENTER);
        add(topBar, BorderLayout.NORTH);

        String[] columns = {
                "Exp ID", "Executed At", "Status", "Duration",
                "LLM Model", "Prompt Technique", "RAG?", "Transcript File"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class;
                if (columnIndex == 3) return Double.class;
                return Object.class;
            }
        };

        logsTable = new JTable(tableModel);
        logsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        logsTable.setRowHeight(28);
        logsTable.setShowGrid(true);
        logsTable.setGridColor(com.formdev.flatlaf.FlatLaf.isLafDark()
                ? new Color(60, 60, 60)
                : new Color(220, 220, 220));
        logsTable.getTableHeader().setReorderingAllowed(false);

        logsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] columnWidths = {60, 160, 110, 80, 160, 140, 60, 320};
        for (int i = 0; i < columnWidths.length; i++) {
            logsTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }

        TableRowSorter<DefaultTableModel> defaultSorter = new TableRowSorter<>(tableModel);
        logsTable.setRowSorter(defaultSorter);
        defaultSorter.setSortKeys(java.util.List.of(
                new javax.swing.RowSorter.SortKey(0, javax.swing.SortOrder.DESCENDING)
        ));

        logsTable.getColumnModel().getColumn(2).setCellRenderer(new StatusRenderer());
        logsTable.getColumnModel().getColumn(3).setCellRenderer(new DurationRenderer());

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < logsTable.getColumnCount(); i++) {
            if (i != 2 && i != 3) {
                logsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        JScrollPane scrollPane = new JScrollPane(logsTable);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        bottomBar.setOpaque(false);

        rerunBtn = new JButton("Test Again / Rerun Config");
        rerunBtn.putClientProperty("JButton.buttonType", "roundRect");
        rerunBtn.setBackground(new Color(59, 130, 246));
        rerunBtn.setForeground(Color.WHITE);
        rerunBtn.addActionListener(e -> rerunSelectedExperiment());
        bottomBar.add(rerunBtn);

        add(bottomBar, BorderLayout.SOUTH);

        refreshData();
    }

    public void refreshData() {
        tableModel.setRowCount(0);

        List<Experiment> list = apiClient.getAllExperiments();
        for (Experiment exp : list) {
            String timeStr = exp.getExecutedAt() != null
                    ? dateFormat.format(exp.getExecutedAt())
                    : (exp.getCreatedAt() != null ? dateFormat.format(exp.getCreatedAt()) : "Pending");

            Double durSec = null;
            if (exp.getDurationMs() != null) {
                durSec = exp.getDurationMs() / 1000.0;
            }

            tableModel.addRow(new Object[]{
                    exp.getExperimentId(),
                    timeStr,
                    exp.getStatus() != null ? exp.getStatus().toUpperCase() : "PENDING",
                    durSec,
                    exp.getModel() != null ? exp.getModel().getModelName() : "Unknown Model (" + exp.getModelId() + ")",
                    exp.getTechnique() != null ? exp.getTechnique().getTechniqueName() : "Unknown Tech (" + exp.getTechniqueId() + ")",
                    exp.isRagEnabled() ? "Yes" : "No",
                    exp.getTranscript() != null ? exp.getTranscript().getFileName() : "Unknown (" + exp.getTranscriptId() + ")"
            });
        }
    }

    private void filterLogsTable() {
        String query = searchField.getText();

        @SuppressWarnings("unchecked")
        TableRowSorter<DefaultTableModel> sorter =
                (TableRowSorter<DefaultTableModel>) logsTable.getRowSorter();

        if (sorter == null) {
            sorter = new TableRowSorter<>(tableModel);
            logsTable.setRowSorter(sorter);
            sorter.setSortKeys(java.util.List.of(
                    new javax.swing.RowSorter.SortKey(0, javax.swing.SortOrder.DESCENDING)
            ));
        }

        if (query.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
        }
    }

    private void rerunSelectedExperiment() {
        int selectedRow = logsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select an experiment from the logs table first.",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int modelRow = logsTable.convertRowIndexToModel(selectedRow);
        int expId = (int) tableModel.getValueAt(modelRow, 0);

        Experiment exp = apiClient.getExperimentById(expId);
        if (exp == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Experiment not found.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        mainFrame.rerunExperiment(exp);
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(getFont().deriveFont(Font.BOLD));

            String status = value != null ? value.toString() : "PENDING";
            if ("COMPLETED".equalsIgnoreCase(status) || "SUCCESS".equalsIgnoreCase(status)) {
                setForeground(new Color(16, 185, 129));
            } else if ("FAILED".equalsIgnoreCase(status)) {
                setForeground(new Color(239, 68, 68));
            } else if ("CANCELLED".equalsIgnoreCase(status) || "CANCELED".equalsIgnoreCase(status)) {
                setForeground(new Color(245, 158, 11));
            } else if ("RUNNING".equalsIgnoreCase(status)) {
                setForeground(new Color(59, 130, 246));
            } else {
                setForeground(Color.GRAY);
            }

            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }

            return this;
        }
    }

    private static class DurationRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setHorizontalAlignment(SwingConstants.CENTER);

            if (value == null) {
                setText("-");
            } else {
                Double sec = (Double) value;
                setText(String.format("%.2fs", sec));
            }

            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }

            return this;
        }
    }
}