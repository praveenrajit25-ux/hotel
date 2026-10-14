package com.hotelmanagement;

import com.hotelmanagement.config.DatabaseConnection;
import com.hotelmanagement.ui.LoginFrame;
import java.awt.*;
import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // Configure high-DPI and smooth font rendering
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            // Set System Look and Feel
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored2) {}
        }

        // Test or inform database connectivity in background
        new Thread(() -> {
            boolean connected = DatabaseConnection.getInstance().testConnection();
            if (!connected) {
                System.err.println("[PMS Startup] Warning: Database connection failed. Please ensure MySQL is running and execute hotel_management.sql");
            } else {
                System.out.println("[PMS Startup] Database connection verified successfully.");
            }
        }).start();

        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
