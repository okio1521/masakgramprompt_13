package edu.utem.ftmk.ui;

import edu.utem.ftmk.client.WebApiClient;
import edu.utem.ftmk.model.Experiment;
import edu.utem.ftmk.model.Transcript;

import javax.swing.*;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final MainFrame mainFrame;
    private final WebApiClient apiClient;

    private JTable transcriptTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel totalReelsLabel;
    private JLabel completedExpLabel;

    public DashboardPanel(MainFrame mainFrame, WebApiClient apiClient) {
        this.mainFrame = mainFrame;
        this.apiClient = apiClient;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initUI();
    }

    private void initUI() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        statsPanel.setOpaque(false);

        JPanel card1 = createStatCard(
                "Total Transcripts (Faster-Whisper)",
                totalReelsLabel = new JLabel("0"),
                new Color(59, 130, 246)
        );
        JPanel card2 = createStatCard(
                "Experiments Executed",
                completedExpLabel = new JLabel("0"),
                new Color(16, 185, 129)
        );
        statsPanel.add(card1);
        statsPanel.add(card2);
        add(statsPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);

        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search Transcripts:"), BorderLayout.WEST);

        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText",
                "Type influencer, reel ID, or content keyword...");
        searchField.addCaretListener(e -> filterTable());
        searchPanel.add(searchField, BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh Data");
        refreshBtn.addActionListener(e -> refreshData());
        searchPanel.add(refreshBtn, BorderLayout.EAST);

        centerPanel.add(searchPanel, BorderLayout.NORTH);

        String[] columns = {
                "ID", "Instagram Reel ID", "Influencer",
                "Whisper Model", "Confidence", "Consistent?", "File Name"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        transcriptTable = new JTable(tableModel);
        transcriptTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transcriptTable.setRowHeight(30);
        transcriptTable.setShowGrid(true);
        transcriptTable.setGridColor(new Color(60, 60, 60));
        transcriptTable.getTableHeader().setReorderingAllowed(false);

        transcriptTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && transcriptTable.getSelectedRow() != -1) {
                    int selectedRow = transcriptTable.getSelectedRow();
                    int modelRow = transcriptTable.convertRowIndexToModel(selectedRow);
                    int transcriptId = (int) tableModel.getValueAt(modelRow, 0);
                    openTranscriptViewer(transcriptId);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(transcriptTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actionPanel.setOpaque(false);

        JButton viewComparisonBtn = new JButton("View Side-by-Side Comparison");
        viewComparisonBtn.putClientProperty("JButton.buttonType", "roundRect");
        viewComparisonBtn.addActionListener(e -> navigateToComparison());
        actionPanel.add(viewComparisonBtn);

        JButton runExpBtn = new JButton("Configure & Run LLM Experiment");
        runExpBtn.putClientProperty("JButton.buttonType", "roundRect");
        runExpBtn.setBackground(new Color(59, 130, 246));
        runExpBtn.setForeground(Color.WHITE);
        runExpBtn.addActionListener(e -> navigateToRunExperiment());
        actionPanel.add(runExpBtn);

        add(actionPanel, BorderLayout.SOUTH);

        refreshData();
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor);
                g2.fillRect(0, 0, 8, getHeight());
                g2.dispose();
            }
        };

        card.setBorder(new EmptyBorder(15, 20, 15, 20));
        card.setBackground(new Color(45, 45, 45));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        titleLabel.setForeground(new Color(180, 180, 180));
        card.add(titleLabel, BorderLayout.NORTH);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        valueLabel.setForeground(Color.WHITE);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    public void reloadData() {
        refreshData();
    }

    private void refreshData() {
        new SwingWorker<Void, Void>() {
            private List<Transcript> transcripts;
            private List<Experiment> experiments;
            private String errorMessage;

            @Override
            protected Void doInBackground() {
                try {
                    transcripts = apiClient.getAllTranscripts();
                    experiments = apiClient.getAllExperiments();
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(
                            DashboardPanel.this,
                            "Failed to load dashboard data: " + errorMessage,
                            "Load Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                tableModel.setRowCount(0);

                totalReelsLabel.setText(String.valueOf(transcripts.size()));
                long completedCount = experiments.stream()
                        .filter(exp -> "completed".equalsIgnoreCase(exp.getStatus()))
                        .count();
                completedExpLabel.setText(String.valueOf(completedCount));

                for (Transcript t : transcripts) {
                    String reelId = "-";
                    String influencer = "-";

                    if (t.getReel() != null) {
                        reelId = t.getReel().getReelIdInstagram();
                        String handle = t.getReel().getInfluencerHandle() == null ? "" : t.getReel().getInfluencerHandle();
                        String name = t.getReel().getInfluencerName() == null ? "-" : t.getReel().getInfluencerName();
                        influencer = handle.isBlank() ? name : name + " (@" + handle + ")";
                    }

                    tableModel.addRow(new Object[]{
                            t.getTranscriptId(),
                            reelId,
                            influencer,
                            t.getWhisperModel(),
                            String.format("%.2f%%", t.getLanguageProbability() * 100),
                            t.isAudioTranscriptConsistent() ? "Yes" : "No",
                            t.getFileName()
                    });
                }
            }
        }.execute();
    }

    private void filterTable() {
        String query = searchField.getText();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        transcriptTable.setRowSorter(sorter);

        if (query.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
        }
    }

    private void navigateToRunExperiment() {
        int selectedRow = transcriptTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a transcript from the table first.",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int modelRow = transcriptTable.convertRowIndexToModel(selectedRow);
        int transcriptId = (int) tableModel.getValueAt(modelRow, 0);
        mainFrame.showRunExperimentForTranscript(transcriptId);
    }

    private void navigateToComparison() {
        int selectedRow = transcriptTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a transcript first.",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int modelRow = transcriptTable.convertRowIndexToModel(selectedRow);
        int transcriptId = (int) tableModel.getValueAt(modelRow, 0);
        mainFrame.showComparisonForTranscript(transcriptId);
    }

    private void openTranscriptViewer(int transcriptId) {
        new SwingWorker<Transcript, Void>() {
            private String errorMessage;

            @Override
            protected Transcript doInBackground() {
                try {
                    return apiClient.getTranscriptById(transcriptId);
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(
                            DashboardPanel.this,
                            "Failed to load transcript: " + errorMessage,
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                try {
                    Transcript transcript = get();
                    if (transcript == null) {
                        JOptionPane.showMessageDialog(
                                DashboardPanel.this,
                                "Transcript not found.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }

                    TranscriptViewerDialog dialog =
                            new TranscriptViewerDialog(
                                    SwingUtilities.getWindowAncestor(DashboardPanel.this),
                                    transcript
                            );
                    dialog.setVisible(true);

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            DashboardPanel.this,
                            "Failed to open transcript viewer: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }
}