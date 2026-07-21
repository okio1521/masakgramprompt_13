package edu.utem.ftmk.db;

import edu.utem.ftmk.model.Experiment;
import edu.utem.ftmk.model.LlmModel;
import edu.utem.ftmk.model.PromptTechnique;
import edu.utem.ftmk.model.Reel;
import edu.utem.ftmk.model.Transcript;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ExperimentRepository {

    private final DatabaseConnection dbConn;

    @Autowired
    public ExperimentRepository(DatabaseConnection dbConn) {
        this.dbConn = dbConn;
    }

    public int save(Experiment exp) {
        String sql = "INSERT INTO experiment (transcript_id, model_id, technique_id, rag_enabled, status, executed_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, exp.getTranscriptId());
            ps.setInt(2, exp.getModelId());
            ps.setInt(3, exp.getTechniqueId());
            ps.setBoolean(4, exp.isRagEnabled());
            ps.setString(5, exp.getStatus());
            ps.setTimestamp(6, exp.getExecutedAt());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    exp.setExperimentId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public void updateStatus(int experimentId, String status, Timestamp executedAt, Integer durationMs) {
        String sql = "UPDATE experiment SET status = ?, executed_at = ?, duration_ms = ? WHERE experiment_id = ?";

        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setTimestamp(2, executedAt);

            if (durationMs != null) {
                ps.setInt(3, durationMs);
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setInt(4, experimentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Experiment findByCondition(int transcriptId, int modelId, int techniqueId, boolean ragEnabled) {
        String sql = "SELECT experiment_id, transcript_id, model_id, technique_id, rag_enabled, status, executed_at, created_at, duration_ms " +
                "FROM experiment WHERE transcript_id = ? AND model_id = ? AND technique_id = ? AND rag_enabled = ?";

        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, transcriptId);
            ps.setInt(2, modelId);
            ps.setInt(3, techniqueId);
            ps.setBoolean(4, ragEnabled);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBasicExperiment(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Experiment> findAll() {
        List<Experiment> list = new ArrayList<>();

        String sql = "SELECT e.experiment_id, e.transcript_id, e.model_id, e.technique_id, e.rag_enabled, e.status, e.executed_at, e.created_at, e.duration_ms, " +
                "m.model_name, m.model_tag, m.provider, m.description AS model_desc, " +
                "t.technique_name, t.system_prompt_file, t.user_prompt_file, t.prompt_version, t.description AS tech_desc, " +
                "tr.file_name, tr.content, r.reel_id, r.reel_id_instagram, r.reel_url, i.instagram_account, i.name AS influencer_name " +
                "FROM experiment e " +
                "INNER JOIN llm_model m ON e.model_id = m.model_id " +
                "INNER JOIN prompt_technique t ON e.technique_id = t.technique_id " +
                "INNER JOIN transcript tr ON e.transcript_id = tr.transcript_id " +
                "INNER JOIN reel r ON tr.reel_id = r.reel_id " +
                "INNER JOIN influencer i ON r.influencer_id = i.influencer_id " +
                "ORDER BY e.executed_at DESC, e.experiment_id DESC";

        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapDetailedExperiment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public Experiment findById(int id) {
        String sql = "SELECT e.experiment_id, e.transcript_id, e.model_id, e.technique_id, e.rag_enabled, e.status, e.executed_at, e.created_at, e.duration_ms, " +
                "m.model_name, m.model_tag, m.provider, m.description AS model_desc, " +
                "t.technique_name, t.system_prompt_file, t.user_prompt_file, t.prompt_version, t.description AS tech_desc, " +
                "tr.transcript_id, tr.file_name, tr.content, r.reel_id, r.reel_id_instagram, r.reel_url, i.instagram_account, i.name AS influencer_name " +
                "FROM experiment e " +
                "INNER JOIN llm_model m ON e.model_id = m.model_id " +
                "INNER JOIN prompt_technique t ON e.technique_id = t.technique_id " +
                "INNER JOIN transcript tr ON e.transcript_id = tr.transcript_id " +
                "INNER JOIN reel r ON tr.reel_id = r.reel_id " +
                "INNER JOIN influencer i ON r.influencer_id = i.influencer_id " +
                "WHERE e.experiment_id = ?";

        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapDetailedExperiment(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Experiment mapBasicExperiment(ResultSet rs) throws SQLException {
        Experiment exp = new Experiment();
        exp.setExperimentId(rs.getInt("experiment_id"));
        exp.setTranscriptId(rs.getInt("transcript_id"));
        exp.setModelId(rs.getInt("model_id"));
        exp.setTechniqueId(rs.getInt("technique_id"));
        exp.setRagEnabled(rs.getBoolean("rag_enabled"));
        exp.setStatus(rs.getString("status"));
        exp.setExecutedAt(rs.getTimestamp("executed_at"));
        exp.setCreatedAt(rs.getTimestamp("created_at"));

        int dur = rs.getInt("duration_ms");
        exp.setDurationMs(rs.wasNull() ? null : dur);

        return exp;
    }

    private Experiment mapDetailedExperiment(ResultSet rs) throws SQLException {
        Experiment exp = mapBasicExperiment(rs);

        LlmModel model = new LlmModel(
                rs.getInt("model_id"),
                rs.getString("model_name"),
                rs.getString("model_tag"),
                rs.getString("provider"),
                rs.getString("model_desc")
        );
        exp.setModel(model);

        PromptTechnique tech = new PromptTechnique(
                rs.getInt("technique_id"),
                rs.getString("technique_name"),
                rs.getString("system_prompt_file"),
                rs.getString("user_prompt_file"),
                rs.getString("prompt_version"),
                rs.getString("tech_desc")
        );
        exp.setTechnique(tech);

        Transcript trans = new Transcript();
        trans.setTranscriptId(rs.getInt("transcript_id"));
        trans.setFileName(rs.getString("file_name"));
        trans.setContent(rs.getString("content"));

        Reel reel = new Reel(
                rs.getInt("reel_id"),
                rs.getString("reel_id_instagram"),
                rs.getString("reel_url"),
                rs.getString("instagram_account"),
                rs.getString("influencer_name")
        );
        trans.setReel(reel);

        exp.setTranscript(trans);
        return exp;
    }
}