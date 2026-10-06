package com.hotelmanagement.ui.components;

import com.hotelmanagement.model.User;
import com.hotelmanagement.model.UserRole;
import com.hotelmanagement.util.UITheme;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Sidebar extends JPanel {

    public static final String CARD_DASHBOARD = "DASHBOARD";
    public static final String CARD_ROOMS = "ROOMS";
    public static final String CARD_GUESTS = "GUESTS";
    public static final String CARD_RESERVATIONS = "RESERVATIONS";
    public static final String CARD_CHECK_IN = "CHECK_IN";
    public static final String CARD_CHECK_OUT = "CHECK_OUT";
    public static final String CARD_BILLING = "BILLING";
    public static final String CARD_SERVICES = "SERVICES";
    public static final String CARD_STAFF = "STAFF";
    public static final String CARD_REPORTS = "REPORTS";
    public static final String CARD_SETTINGS = "SETTINGS";

    private final User currentUser;
    private final Consumer<String> onNavigate;
    private final List<SidebarButton> navButtons = new ArrayList<>();
    private String activeCard = CARD_DASHBOARD;

    public Sidebar(User currentUser, Consumer<String> onNavigate) {
        this.currentUser = currentUser;
        this.onNavigate = onNavigate;

        setPreferredSize(new Dimension(240, 0));
        setBackground(UITheme.PRIMARY_NAVY);
        setLayout(new BorderLayout());

        // Header Monogram & Logo Banner
        JPanel headerPanel = new JPanel(new BorderLayout(10, 5));
        headerPanel.setBackground(UITheme.PRIMARY_NAVY);
        headerPanel.setBorder(new EmptyBorder(24, 20, 20, 20));

        JLabel logoIcon = new JLabel("GL", SwingConstants.CENTER);
        logoIcon.setPreferredSize(new Dimension(42, 42));
        logoIcon.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoIcon.setForeground(UITheme.PRIMARY_NAVY);
        logoIcon.setOpaque(true);
        logoIcon.setBackground(UITheme.ACCENT_GOLD);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);

        JLabel titleLbl = new JLabel("Grand Luxe");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLbl.setForeground(Color.WHITE);

        JLabel subTitleLbl = new JLabel("HOTEL PMS v2.0");
        subTitleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        subTitleLbl.setForeground(UITheme.ACCENT_GOLD);

        titleBlock.add(titleLbl);
        titleBlock.add(subTitleLbl);

        headerPanel.add(logoIcon, BorderLayout.WEST);
        headerPanel.add(titleBlock, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);

        // Navigation Menu List
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(UITheme.PRIMARY_NAVY);
        menuPanel.setBorder(new EmptyBorder(10, 12, 10, 12));

        addNavItem(menuPanel, "Dashboard", CARD_DASHBOARD);
        addNavItem(menuPanel, "Room Inventory", CARD_ROOMS);
        addNavItem(menuPanel, "Guest Directory", CARD_GUESTS);
        addNavItem(menuPanel, "Reservations", CARD_RESERVATIONS);
        addNavItem(menuPanel, "Front Desk Check-In", CARD_CHECK_IN);
        addNavItem(menuPanel, "Express Check-Out", CARD_CHECK_OUT);
        addNavItem(menuPanel, "Billing & Invoices", CARD_BILLING);
        addNavItem(menuPanel, "Hotel Services", CARD_SERVICES);

        // Role-based restrictions
        boolean isAdmin = currentUser != null && currentUser.getRole() == UserRole.Admin;
        boolean isManager = currentUser != null && (currentUser.getRole() == UserRole.Admin || currentUser.getRole() == UserRole.Manager);

        if (isAdmin) {
            addNavItem(menuPanel, "Staff Management", CARD_STAFF);
        }
        if (isManager) {
            addNavItem(menuPanel, "Analytics & Reports", CARD_REPORTS);
        }
        if (isAdmin) {
            addNavItem(menuPanel, "Hotel Settings", CARD_SETTINGS);
        }

        JScrollPane scrollPane = new JScrollPane(menuPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(UITheme.PRIMARY_NAVY);
        scrollPane.getViewport().setBackground(UITheme.PRIMARY_NAVY);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);

        // Footer System Info
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(UITheme.SECONDARY_NAVY);
        footerPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel sessionLabel = new JLabel("Logged as: " + (currentUser != null ? currentUser.getUsername() : ""));
        sessionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sessionLabel.setForeground(new Color(148, 163, 184));
        footerPanel.add(sessionLabel, BorderLayout.CENTER);

        add(footerPanel, BorderLayout.SOUTH);

        // Highlight initial item
        setActiveCard(CARD_DASHBOARD);
    }

    private void addNavItem(JPanel container, String text, String cardKey) {
        SidebarButton btn = new SidebarButton(text, cardKey);
        navButtons.add(btn);
        container.add(btn);
        container.add(Box.createRigidArea(new Dimension(0, 4)));
    }

    public void setActiveCard(String cardKey) {
        this.activeCard = cardKey;
        for (SidebarButton btn : navButtons) {
            btn.setActive(btn.cardKey.equals(cardKey));
        }
    }

    private class SidebarButton extends JPanel {
        private final String cardKey;
        private final JLabel label;
        private boolean isActive = false;

        public SidebarButton(String text, String cardKey) {
            this.cardKey = cardKey;
            setLayout(new BorderLayout());
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            setPreferredSize(new Dimension(216, 38));
            setBackground(UITheme.PRIMARY_NAVY);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 14, 8, 14));

            label = new JLabel(text);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            label.setForeground(new Color(203, 213, 225));
            add(label, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!isActive) {
                        setBackground(UITheme.SIDEBAR_HOVER);
                        label.setForeground(Color.WHITE);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!isActive) {
                        setBackground(UITheme.PRIMARY_NAVY);
                        label.setForeground(new Color(203, 213, 225));
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    setActiveCard(cardKey);
                    if (onNavigate != null) {
                        onNavigate.accept(cardKey);
                    }
                }
            });
        }

        public void setActive(boolean active) {
            this.isActive = active;
            if (active) {
                setBackground(UITheme.SECONDARY_NAVY);
                label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                label.setForeground(UITheme.ACCENT_GOLD);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, UITheme.ACCENT_GOLD),
                        new EmptyBorder(8, 10, 8, 14)
                ));
            } else {
                setBackground(UITheme.PRIMARY_NAVY);
                label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                label.setForeground(new Color(203, 213, 225));
                setBorder(new EmptyBorder(8, 14, 8, 14));
            }
            repaint();
        }
    }
}
