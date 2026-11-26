package com.nextstop.dao;

import com.nextstop.model.Trip;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TripDAO {

    public void save(Trip trip) throws SQLException {
        String sql = "INSERT INTO trips (trip_id, user_id, destination, duration_days, total_budget) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, trip.getTripId());
            pstmt.setInt(2, trip.getUserId());
            pstmt.setString(3, trip.getDestination());
            pstmt.setInt(4, trip.getDurationDays());
            pstmt.setDouble(5, trip.getTotalBudget());
            pstmt.executeUpdate();
        }
    }

    public List<Trip> findByUserId(int userId) throws SQLException {
        List<Trip> trips = new ArrayList<>();
        String sql = "SELECT * FROM trips WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    trips.add(new Trip(
                            rs.getString("trip_id"),
                            rs.getInt("user_id"),
                            rs.getString("destination"),
                            rs.getInt("duration_days"),
                            rs.getDouble("total_budget")
                    ));
                }
            }
        }
        return trips;
    }

    public boolean existsById(String tripId) throws SQLException {
        String sql = "SELECT 1 FROM trips WHERE trip_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tripId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean deleteByIdAndUser(String tripId, int userId) throws SQLException {
        String sql = "DELETE FROM trips WHERE trip_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tripId);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        }
    }
}