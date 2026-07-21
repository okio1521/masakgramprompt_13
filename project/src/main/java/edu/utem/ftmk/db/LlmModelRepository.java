package edu.utem.ftmk.db;

import edu.utem.ftmk.model.LlmModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class LlmModelRepository {

    private final DatabaseConnection dbConn;

    @Autowired
    public LlmModelRepository(DatabaseConnection dbConn) {
        this.dbConn = dbConn;
    }

    public List<LlmModel> findAll() {
        List<LlmModel> list = new ArrayList<>();
        String sql = "SELECT model_id, model_name, model_tag, provider, description FROM llm_model ORDER BY model_id";

        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new LlmModel(
                        rs.getInt("model_id"),
                        rs.getString("model_name"),
                        rs.getString("model_tag"),
                        rs.getString("provider"),
                        rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public LlmModel findById(int id) {
        String sql = "SELECT model_id, model_name, model_tag, provider, description FROM llm_model WHERE model_id = ?";

        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new LlmModel(
                            rs.getInt("model_id"),
                            rs.getString("model_name"),
                            rs.getString("model_tag"),
                            rs.getString("provider"),
                            rs.getString("description")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}