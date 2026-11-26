package com.nextstop.dao;

import com.nextstop.model.Activity;
import com.nextstop.model.ActivityCategory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityDAO {

    public void save(Activity activity) throws SQLException {
        String sql = "INSERT INTO activities (trip_id, day_number, title, category, cost) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, activity.getTripId());
            pstmt.setInt(2, activity.getDayNumber());
            pstmt.setString(3, activity.getTitle());
            pstmt.setString(4, activity.getCategory().name());
            pstmt.setDouble(5, activity.getCost());
            pstmt.executeUpdate();

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    activity.setActivityId(keys.getInt(1));
                }
            }
        }
    }

    public List<Activity> findByTripId(String tripId) throws SQLException {
        List<Activity> activities = new ArrayList<>();
        String sql = "SELECT * FROM activities WHERE trip_id = ? ORDER BY day_number ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tripId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    activities.add(new Activity(
                            rs.getInt("activity_id"),
                            rs.getString("trip_id"),
                            rs.getInt("day_number"),
                            rs.getString("title"),
                            ActivityCategory.fromString(rs.getString("category")),
                            rs.getDouble("cost")
                    ));
                }
            }
        }
        return activities;
    }

    public boolean deleteById(int activityId) throws SQLException {
        String sql = "DELETE FROM activities WHERE activity_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, activityId);
            return pstmt.executeUpdate() > 0;
        }
    }
}