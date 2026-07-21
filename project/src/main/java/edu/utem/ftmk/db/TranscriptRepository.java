package edu.utem.ftmk.db;

import edu.utem.ftmk.model.Reel;
import edu.utem.ftmk.model.Transcript;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TranscriptRepository {

    private final DatabaseConnection dbConn;

    @Autowired
    public TranscriptRepository(DatabaseConnection dbConn) {
        this.dbConn = dbConn;
    }

    public int addTranscript(Transcript transcript) {
        String sql = "INSERT INTO transcript (reel_id, audio_id, file_name, file_path, file_size_bytes, content, " +
                "language_mix, whisper_model, detected_language, language_probability, audio_transcript_consistent, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, transcript.getReelId());
            ps.setInt(2, transcript.getAudioId());
            ps.setString(3, transcript.getFileName());
            ps.setString(4, transcript.getFilePath());
            ps.setLong(5, transcript.getFileSizeBytes());
            ps.setString(6, transcript.getContent());
            ps.setString(7, transcript.getLanguageMix());
            ps.setString(8, transcript.getWhisperModel());
            ps.setString(9, transcript.getDetectedLanguage());
            ps.setFloat(10, transcript.getLanguageProbability());
            ps.setBoolean(11, transcript.isAudioTranscriptConsistent());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public List<Transcript> findAll() {
        List<Transcript> list = new ArrayList<>();

        String sql = "SELECT t.transcript_id, t.reel_id, t.audio_id, t.file_name, t.file_path, t.file_size_bytes, t.content, " +
                "t.language_mix, t.whisper_model, t.detected_language, t.language_probability, t.audio_transcript_consistent, t.created_at, " +
                "r.reel_id_instagram, r.reel_url, i.instagram_account, i.name AS influencer_name " +
                "FROM transcript t " +
                "INNER JOIN reel r ON t.reel_id = r.reel_id " +
                "INNER JOIN influencer i ON r.influencer_id = i.influencer_id " +
                "ORDER BY t.transcript_id";

        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToTranscript(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public Transcript findById(int id) {
        String sql = "SELECT t.transcript_id, t.reel_id, t.audio_id, t.file_name, t.file_path, t.file_size_bytes, t.content, " +
                "t.language_mix, t.whisper_model, t.detected_language, t.language_probability, t.audio_transcript_consistent, t.created_at, " +
                "r.reel_id_instagram, r.reel_url, i.instagram_account, i.name AS influencer_name " +
                "FROM transcript t " +
                "INNER JOIN reel r ON t.reel_id = r.reel_id " +
                "INNER JOIN influencer i ON r.influencer_id = i.influencer_id " +
                "WHERE t.transcript_id = ?";

        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTranscript(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Transcript mapResultSetToTranscript(ResultSet rs) throws SQLException {
        Transcript t = new Transcript();
        t.setTranscriptId(rs.getInt("transcript_id"));
        t.setReelId(rs.getInt("reel_id"));
        t.setAudioId(rs.getInt("audio_id"));
        t.setFileName(rs.getString("file_name"));
        t.setFilePath(rs.getString("file_path"));
        t.setFileSizeBytes(rs.getLong("file_size_bytes"));
        t.setContent(rs.getString("content"));
        t.setLanguageMix(rs.getString("language_mix"));
        t.setWhisperModel(rs.getString("whisper_model"));
        t.setDetectedLanguage(rs.getString("detected_language"));
        t.setLanguageProbability(rs.getFloat("language_probability"));
        t.setAudioTranscriptConsistent(rs.getBoolean("audio_transcript_consistent"));
        t.setCreatedAt(rs.getTimestamp("created_at"));

        Reel r = new Reel(
                rs.getInt("reel_id"),
                rs.getString("reel_id_instagram"),
                rs.getString("reel_url"),
                rs.getString("instagram_account"),
                rs.getString("influencer_name")
        );
        t.setReel(r);

        return t;
    }
}