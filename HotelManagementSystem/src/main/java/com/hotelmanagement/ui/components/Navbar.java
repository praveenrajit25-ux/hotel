package com.hotelmanagement.ui.components;

import com.hotelmanagement.model.User;
import com.hotelmanagement.util.UITheme;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Navbar extends JPanel {

    private final JLabel clockLabel;
    private final JLabel userGreeting;
    private final JLabel roleBadge;
    private final JButton logoutButton;

    public Navbar(User currentUser, ActionListener onLogout) {
        setLayout(new BorderLayout());
        setBackground(UITheme.CARD_BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_LIGHT),
                new EmptyBorder(10, 24, 10, 24)
        ));

        // Left: Hotel Brand / System Title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftPanel.setOpaque(false);

        JLabel brandLabel = new JLabel("GRAND LUXE HOTEL & RESORT");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        brandLabel.setForeground(UITheme.PRIMARY_NAVY);

        JLabel tagLabel = new JLabel("|  Management System");
        tagLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tagLabel.setForeground(UITheme.TEXT_MUTED);

        leftPanel.add(brandLabel);
        leftPanel.add(tagLabel);
        add(leftPanel, BorderLayout.WEST);

        // Right: Clock, User Greeting, Role Badge, Logout Button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        rightPanel.setOpaque(false);

        clockLabel = new JLabel();
        clockLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clockLabel.setForeground(UITheme.TEXT_MUTED);
        updateClock();

        // Timer for clock updates every second
        Timer timer = new Timer(1000, e -> updateClock());
        timer.start();

        userGreeting = new JLabel(currentUser != null ? currentUser.getFullName() : "Staff");
        userGreeting.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userGreeting.setForeground(UITheme.PRIMARY_NAVY);

        String roleName = currentUser != null ? currentUser.getRole().name() : "STAFF";
        roleBadge = new JLabel(" " + roleName + " ");
        roleBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        roleBadge.setOpaque(true);
        roleBadge.setBackground(new Color(238, 242, 255));
        roleBadge.setForeground(new Color(79, 70, 229));
        roleBadge.setBorder(BorderFactory.createLineBorder(new Color(199, 210, 254), 1));

        logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        logoutButton.setBackground(new Color(254, 242, 242));
        logoutButton.setForeground(UITheme.DANGER_RED);
        logoutButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(254, 202, 202), 1),
                new EmptyBorder(4, 12, 4, 12)
        ));
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(onLogout);

        rightPanel.add(clockLabel);
        rightPanel.add(new JSeparator(SwingConstants.VERTICAL));
        rightPanel.add(userGreeting);
        rightPanel.add(roleBadge);
        rightPanel.add(logoutButton);

        add(rightPanel, BorderLayout.EAST);
    }

    private void updateClock() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy | HH:mm:ss");
        clockLabel.setText(LocalDateTime.now().format(dtf));
    }
}
