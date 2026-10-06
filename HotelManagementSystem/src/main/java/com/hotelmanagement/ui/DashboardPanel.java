package com.hotelmanagement.ui;

import com.hotelmanagement.model.DashboardStats;
import com.hotelmanagement.model.Reservation;
import com.hotelmanagement.model.User;
import com.hotelmanagement.service.DashboardService;
import com.hotelmanagement.service.ReservationService;
import com.hotelmanagement.ui.components.CustomTable;
import com.hotelmanagement.ui.components.Sidebar;
import com.hotelmanagement.ui.components.StatCard;
import com.hotelmanagement.util.CurrencyUtil;
import com.hotelmanagement.util.DateUtil;
import com.hotelmanagement.util.UITheme;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class DashboardPanel extends JPanel {

    private final User currentUser;
    private final DashboardService dashboardService;
    private final ReservationService reservationService;
    private final Consumer<String> navigator;

    private StatCard totalRoomsCard;
    private StatCard availableRoomsCard;
    private StatCard checkInsCard;
    private StatCard checkOutsCard;
    private StatCard revenueCard;
    private StatCard activeReservationsCard;

    private DefaultTableModel recentTableModel;
    private JLabel statusSummaryLabel;

    public DashboardPanel(User currentUser, Consumer<String> navigator) {
        this.currentUser = currentUser;
        this.navigator = navigator;
        this.dashboardService = new DashboardService();
        this.reservationService = new ReservationService();

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshData();
    }

    private void initUI() {
        // Top Header Title & Refresh Button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1));
        titleBlock.setOpaque(false);
        JLabel titleLabel = new JLabel("Operations Dashboard");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.PRIMARY_NAVY);

        JLabel subLabel = new JLabel("Real-time occupancy, revenue analytics, and front-desk metrics");
        subLabel.setFont(UITheme.FONT_BODY);
        subLabel.setForeground(UITheme.TEXT_MUTED);
        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);

        JButton refreshBtn = new JButton("Refresh Analytics");
        refreshBtn.setFont(UITheme.FONT_BODY_BOLD);
        refreshBtn.setBackground(Color.WHITE);
        refreshBtn.setForeground(UITheme.PRIMARY_NAVY);
        refreshBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1),
                new EmptyBorder(8, 16, 8, 16)
        ));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshData());

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);

        // Center Content with ScrollPane
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // 1. KPI Metric Stat Cards Grid (2x3)
        JPanel statsGrid = new JPanel(new GridLayout(2, 3, 14, 14));
        statsGrid.setOpaque(false);
        statsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        totalRoomsCard = new StatCard("Total Rooms", "0", "Occupancy: 0%", UITheme.PRIMARY_NAVY);
        availableRoomsCard = new StatCard("Available Rooms", "0", "Ready for check-in", UITheme.SUCCESS_GREEN);
        checkInsCard = new StatCard("Today's Check-Ins", "0", "Scheduled arrivals", UITheme.INFO_BLUE);
        checkOutsCard = new StatCard("Today's Check-Outs", "0", "Scheduled departures", UITheme.WARNING_ORANGE);
        revenueCard = new StatCard("Total Revenue", "$0.00", "Today: $0.00", UITheme.ACCENT_GOLD);
        activeReservationsCard = new StatCard("Active Bookings", "0", "Confirmed & in-house", new Color(124, 58, 237));

        statsGrid.add(totalRoomsCard);
        statsGrid.add(availableRoomsCard);
        statsGrid.add(checkInsCard);
        statsGrid.add(checkOutsCard);
        statsGrid.add(revenueCard);
        statsGrid.add(activeReservationsCard);

        contentPanel.add(statsGrid);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 18)));

        // 2. Quick Actions Panel
        JPanel quickActionsCard = new JPanel(new BorderLayout(10, 10));
        quickActionsCard.setBackground(UITheme.CARD_BG);
        quickActionsCard.setBorder(UITheme.BORDER_PANEL);
        quickActionsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel qaTitle = new JLabel("QUICK FRONT DESK ACTIONS");
        qaTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        qaTitle.setForeground(UITheme.TEXT_MUTED);
        quickActionsCard.add(qaTitle, BorderLayout.NORTH);

        JPanel qaRow = new JPanel(new GridLayout(1, 6, 10, 0));
        qaRow.setOpaque(false);

        qaRow.add(createActionBtn("+ New Booking", Sidebar.CARD_RESERVATIONS, UITheme.PRIMARY_NAVY));
        qaRow.add(createActionBtn("Front Desk Check-In", Sidebar.CARD_CHECK_IN, UITheme.INFO_BLUE));
        qaRow.add(createActionBtn("Express Check-Out", Sidebar.CARD_CHECK_OUT, UITheme.WARNING_ORANGE));
        qaRow.add(createActionBtn("Billing & Invoices", Sidebar.CARD_BILLING, UITheme.ACCENT_GOLD));
        qaRow.add(createActionBtn("Register Guest", Sidebar.CARD_GUESTS, UITheme.SUCCESS_GREEN));
        qaRow.add(createActionBtn("Room Inventory", Sidebar.CARD_ROOMS, UITheme.SECONDARY_NAVY));

        quickActionsCard.add(qaRow, BorderLayout.CENTER);
        contentPanel.add(quickActionsCard);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 18)));

        // 3. Room Inventory Breakdown Bar
        JPanel roomBreakdownCard = new JPanel(new BorderLayout(8, 8));
        roomBreakdownCard.setBackground(UITheme.CARD_BG);
        roomBreakdownCard.setBorder(UITheme.BORDER_PANEL);
        roomBreakdownCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel rbTitle = new JLabel("LIVE ROOM STATUS BREAKDOWN");
        rbTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rbTitle.setForeground(UITheme.TEXT_MUTED);

        statusSummaryLabel = new JLabel("Loading room metrics...");
        statusSummaryLabel.setFont(UITheme.FONT_BODY_BOLD);
        statusSummaryLabel.setForeground(UITheme.PRIMARY_NAVY);

        roomBreakdownCard.add(rbTitle, BorderLayout.NORTH);
        roomBreakdownCard.add(statusSummaryLabel, BorderLayout.CENTER);

        contentPanel.add(roomBreakdownCard);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 18)));

        // 4. Recent Active Reservations Table
        JPanel tableCard = new JPanel(new BorderLayout(8, 8));
        tableCard.setBackground(UITheme.CARD_BG);
        tableCard.setBorder(UITheme.BORDER_PANEL);

        JLabel tableTitle = new JLabel("RECENT RESERVATIONS & ARRIVALS");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableTitle.setForeground(UITheme.TEXT_MUTED);

        String[] columns = {"ID", "Guest Name", "Phone", "Room", "Type", "Check-In", "Check-Out", "Guests", "Status"};
        recentTableModel = new DefaultTableModel(columns, 0);
        CustomTable recentTable = new CustomTable(recentTableModel);
        JScrollPane tableScroll = CustomTable.createScrollPane(recentTable);
        tableScroll.setPreferredSize(new Dimension(0, 220));

        tableCard.add(tableTitle, BorderLayout.NORTH);
        tableCard.add(tableScroll, BorderLayout.CENTER);

        contentPanel.add(tableCard);

        JScrollPane mainScrollPane = new JScrollPane(contentPanel);
        mainScrollPane.setBorder(null);
        mainScrollPane.getViewport().setBackground(UITheme.BG_LIGHT);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(headerPanel, BorderLayout.NORTH);
        add(mainScrollPane, BorderLayout.CENTER);
    }

    private JButton createActionBtn(String title, String targetCard, Color bg) {
        JButton btn = new JButton(title);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 8, 8, 8));
        btn.addActionListener(e -> {
            if (navigator != null) {
                navigator.accept(targetCard);
            }
        });
        return btn;
    }

    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            try {
                DashboardStats stats = dashboardService.getStats();

                int total = stats.getTotalRooms();
                int occupied = stats.getOccupiedRooms();
                double occRate = total > 0 ? ((double) occupied / total) * 100.0 : 0.0;

                totalRoomsCard.setValue(String.valueOf(total));
                totalRoomsCard.setSubtitle(String.format("Occupancy Rate: %.1f%%", occRate));

                availableRoomsCard.setValue(String.valueOf(stats.getAvailableRooms()));
                checkInsCard.setValue(String.valueOf(stats.getTodayCheckIns()));
                checkOutsCard.setValue(String.valueOf(stats.getTodayCheckOuts()));

                revenueCard.setValue(CurrencyUtil.format(stats.getTotalRevenue()));
                revenueCard.setSubtitle("Today's Rev: " + CurrencyUtil.format(stats.getTodayRevenue()));

                activeReservationsCard.setValue(String.valueOf(stats.getActiveReservations()));

                // Update Room Status Breakdown
                String breakdownText = String.format(
                        "<html>Available: <b style='color:#16a34a;'>%d</b>  |  " +
                        "Occupied: <b style='color:#2563eb;'>%d</b>  |  " +
                        "Reserved: <b style='color:#d97706;'>%d</b>  |  " +
                        "Cleaning: <b style='color:#0284c7;'>%d</b>  |  " +
                        "Maintenance: <b style='color:#dc2626;'>%d</b></html>",
                        stats.getAvailableRooms(), stats.getOccupiedRooms(), stats.getReservedRooms(),
                        stats.getCleaningRooms(), stats.getMaintenanceRooms()
                );
                statusSummaryLabel.setText(breakdownText);

                // Update Recent Bookings
                recentTableModel.setRowCount(0);
                List<Reservation> resList = reservationService.getAllReservations();
                int count = 0;
                for (Reservation r : resList) {
                    if (count++ >= 8) break;
                    recentTableModel.addRow(new Object[]{
                            "#" + r.getReservationId(),
                            r.getGuestName(),
                            r.getGuestPhone() != null ? r.getGuestPhone() : "-",
                            r.getRoomNumber(),
                            r.getRoomType(),
                            DateUtil.formatDate(r.getCheckInDate()),
                            DateUtil.formatDate(r.getCheckOutDate()),
                            r.getNumberOfGuests(),
                            r.getStatus().getDisplayName()
                    });
                }
            } catch (Exception ex) {
                // non-fatal handling
            }
        });
    }
}
