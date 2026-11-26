package com.nextstop.dao;

import com.nextstop.model.PackingItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PackingItemDAO {

    public void save(PackingItem item) throws SQLException {
        String sql = "INSERT INTO packing_items (trip_id, item_name, quantity, is_packed) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, item.getTripId());
            pstmt.setString(2, item.getItemName());
            pstmt.setInt(3, item.getQuantity());
            pstmt.setBoolean(4, item.isPacked());
            pstmt.executeUpdate();

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    item.setItemId(keys.getInt(1));
                }
            }
        }
    }

    public List<PackingItem> findByTripId(String tripId) throws SQLException {
        List<PackingItem> items = new ArrayList<>();
        String sql = "SELECT * FROM packing_items WHERE trip_id = ? ORDER BY item_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tripId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new PackingItem(
                            rs.getInt("item_id"),
                            rs.getString("trip_id"),
                            rs.getString("item_name"),
                            rs.getInt("quantity"),
                            rs.getBoolean("is_packed")
                    ));
                }
            }
        }
        return items;
    }

    public boolean markPacked(int itemId, boolean packed) throws SQLException {
        String sql = "UPDATE packing_items SET is_packed = ? WHERE item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, packed);
            pstmt.setInt(2, itemId);
            return pstmt.executeUpdate() > 0;
        }
    }
}