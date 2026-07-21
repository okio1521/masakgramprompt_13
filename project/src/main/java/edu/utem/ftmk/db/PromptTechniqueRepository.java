package edu.utem.ftmk.db;

import edu.utem.ftmk.model.PromptTechnique;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PromptTechniqueRepository {

    private final DatabaseConnection dbConn;

    @Autowired
    public PromptTechniqueRepository(DatabaseConnection dbConn) {
        this.dbConn = dbConn;
    }

    public List<PromptTechnique> findAll() {
        List<PromptTechnique> list = new ArrayList<>();
        String sql = "SELECT technique_id, technique_name, system_prompt_file, user_prompt_file, prompt_version, description " +
                "FROM prompt_technique ORDER BY technique_id";

        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new PromptTechnique(
                        rs.getInt("technique_id"),
                        rs.getString("technique_name"),
                        rs.getString("system_prompt_file"),
                        rs.getString("user_prompt_file"),
                        rs.getString("prompt_version"),
                        rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public PromptTechnique findById(int id) {
        String sql = "SELECT technique_id, technique_name, system_prompt_file, user_prompt_file, prompt_version, description " +
                "FROM prompt_technique WHERE technique_id = ?";

        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new PromptTechnique(
                            rs.getInt("technique_id"),
                            rs.getString("technique_name"),
                            rs.getString("system_prompt_file"),
                            rs.getString("user_prompt_file"),
                            rs.getString("prompt_version"),
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