package com.hotelmanagement.dao;

import com.hotelmanagement.config.DatabaseConnection;
import com.hotelmanagement.exception.DatabaseException;
import com.hotelmanagement.model.HotelSettings;
import java.sql.*;

public class HotelSettingsDAO {

    public HotelSettings getSettings() {
        String sql = "SELECT * FROM hotel_settings ORDER BY setting_id ASC LIMIT 1";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                HotelSettings s = new HotelSettings();
                s.setSettingId(rs.getInt("setting_id"));
                s.setHotelName(rs.getString("hotel_name"));
                s.setTagline(rs.getString("tagline"));
                s.setAddress(rs.getString("address"));
                s.setPhone(rs.getString("phone"));
                s.setEmail(rs.getString("email"));
                s.setWebsite(rs.getString("website"));
                s.setCurrency(rs.getString("currency"));
                s.setTaxPercentage(rs.getBigDecimal("tax_percentage"));
                Time ci = rs.getTime("checkin_time");
                if (ci != null) s.setCheckinTime(ci.toString());
                Time co = rs.getTime("checkout_time");
                if (co != null) s.setCheckoutTime(co.toString());
                s.setInvoiceFooter(rs.getString("invoice_footer"));
                return s;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load hotel settings", e);
        }
        return new HotelSettings(); // default fallback
    }

    public void updateSettings(HotelSettings s) {
        String sql = "UPDATE hotel_settings SET hotel_name = ?, tagline = ?, address = ?, phone = ?, email = ?, website = ?, currency = ?, tax_percentage = ?, invoice_footer = ? WHERE setting_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getHotelName());
            ps.setString(2, s.getTagline());
            ps.setString(3, s.getAddress());
            ps.setString(4, s.getPhone());
            ps.setString(5, s.getEmail());
            ps.setString(6, s.getWebsite());
            ps.setString(7, s.getCurrency());
            ps.setBigDecimal(8, s.getTaxPercentage());
            ps.setString(9, s.getInvoiceFooter());
            ps.setInt(10, s.getSettingId() != null ? s.getSettingId() : 1);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update hotel settings", e);
        }
    }
}
