package edu.utem.ftmk.ui;

import edu.utem.ftmk.model.Transcript;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog to display the full transcript content and basic metadata.
 */
public class TranscriptViewerDialog extends JDialog {

    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font CONTENT_FONT = new Font("Consolas", Font.PLAIN, 13);

    public TranscriptViewerDialog(Window owner, Transcript transcript) {
        super(
                owner,
                "Transcript Viewer - ID " + (transcript != null ? transcript.getTranscriptId() : "-"),
                ModalityType.APPLICATION_MODAL
        );
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(owner);
        initUI(transcript);
    }

    private void initUI(Transcript transcript) {
        JPanel container = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int h = getHeight();
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(30, 30, 30),
                        0, h, new Color(45, 45, 45)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), h);
                g2.dispose();
            }
        };
        container.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        container.setOpaque(false);

        JPanel header = new JPanel(new GridLayout(0, 1));
        header.setOpaque(false);

        String reelId = "-";
        String influencer = "-";

        if (transcript != null && transcript.getReel() != null) {
            reelId = safe(transcript.getReel().getReelIdInstagram());
            influencer = safe(transcript.getReel().getInfluencerName());
        }

        header.add(createLabel("Reel ID: " + reelId));
        header.add(createLabel("Influencer: " + influencer));
        header.add(createLabel("File: " + safe(transcript != null ? transcript.getFileName() : null)));
        header.add(createLabel("Language: " + safe(transcript != null ? transcript.getLanguageMix() : null)));
        header.add(createLabel("Whisper Model: " + safe(transcript != null ? transcript.getWhisperModel() : null)));
        header.add(createLabel("Detected Language: " + safe(transcript != null ? transcript.getDetectedLanguage() : null)));

        String confidenceText = "-";
        if (transcript != null) {
            confidenceText = String.format("%.2f%%", transcript.getLanguageProbability() * 100);
        }
        header.add(createLabel("Confidence: " + confidenceText));

        String consistentText = "-";
        if (transcript != null) {
            consistentText = transcript.isAudioTranscriptConsistent() ? "Yes" : "No";
        }
        header.add(createLabel("Consistent: " + consistentText));

        container.add(header, BorderLayout.NORTH);

        JTextArea contentArea = new JTextArea(transcript != null ? safe(transcript.getContent()) : "");
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setFont(CONTENT_FONT);
        contentArea.setBackground(new Color(25, 25, 25));
        contentArea.setForeground(new Color(0, 230, 115));

        JScrollPane scrollPane = new JScrollPane(contentArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                "Transcript Text"
        ));
        container.add(scrollPane, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        closeBtn.putClientProperty("JButton.buttonType", "roundRect");
        closeBtn.setBackground(new Color(59, 130, 246));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.addActionListener(e -> dispose());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(closeBtn);
        container.add(footer, BorderLayout.SOUTH);

        setContentPane(container);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(TITLE_FONT);
        lbl.setForeground(new Color(180, 180, 180));
        return lbl;
    }

    private String safe(Object obj) {
        return obj == null ? "-" : obj.toString();
    }
}