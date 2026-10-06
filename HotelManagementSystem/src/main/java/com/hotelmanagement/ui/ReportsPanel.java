package com.hotelmanagement.ui;

import com.hotelmanagement.model.DashboardStats;
import com.hotelmanagement.model.HotelSettings;
import com.hotelmanagement.model.User;
import com.hotelmanagement.service.DashboardService;
import com.hotelmanagement.service.HotelSettingsService;
import com.hotelmanagement.service.ReportService;
import com.hotelmanagement.ui.components.CustomTable;
import com.hotelmanagement.util.CurrencyUtil;
import com.hotelmanagement.util.DialogUtil;
import com.hotelmanagement.util.PDFExporter;
import com.hotelmanagement.util.UITheme;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class ReportsPanel extends JPanel {

    private final User currentUser;
    private final ReportService reportService;
    private final DashboardService dashboardService;
    private final HotelSettingsService settingsService;

    private JComboBox<String> reportTypeCombo;
    private JTextField startDateField;
    private JTextField endDateField;

    private DefaultTableModel tableModel;
    private CustomTable reportTable;
    private JLabel summaryLabel;

    public ReportsPanel(User currentUser) {
        this.currentUser = currentUser;
        this.reportService = new ReportService();
        this.dashboardService = new DashboardService();
        this.settingsService = new HotelSettingsService();

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        generateReport();
    }

    private void initUI() {
        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1));
        titleBlock.setOpaque(false);
        JLabel titleLabel = new JLabel("Financial & Management Reports");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.PRIMARY_NAVY);

        JLabel subLabel = new JLabel("Audit revenue streams, occupancy statistics, service performance, and operational KPIs");
        subLabel.setFont(UITheme.FONT_BODY);
        subLabel.setForeground(UITheme.TEXT_MUTED);
        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);

        JButton exportBtn = new JButton("Export Report to File");
        exportBtn.setFont(UITheme.FONT_BODY_BOLD);
        exportBtn.setBackground(UITheme.PRIMARY_NAVY);
        exportBtn.setForeground(Color.WHITE);
        exportBtn.setFocusPainted(false);
        exportBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportBtn.addActionListener(e -> exportCurrentReport());

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(exportBtn, BorderLayout.EAST);

        // Center Area: Filter Control Bar + Summary Panel + Report Table
        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.setOpaque(false);

        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterCard.setBackground(UITheme.CARD_BG);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        reportTypeCombo = new JComboBox<>(new String[]{
                "Monthly Revenue & Financial Summary",
                "Occupancy by Room Category",
                "Top Hotel Services Revenue",
                "Live Room Inventory Status Breakdown"
        });
        reportTypeCombo.setPreferredSize(new Dimension(280, 32));

        startDateField = new JTextField(LocalDate.now().minusMonths(6).toString(), 8);
        startDateField.setPreferredSize(new Dimension(90, 32));
        endDateField = new JTextField(LocalDate.now().toString(), 8);
        endDateField.setPreferredSize(new Dimension(90, 32));

        JButton runBtn = new JButton("Generate Report");
        runBtn.setFont(UITheme.FONT_BODY_BOLD);
        runBtn.setBackground(UITheme.INFO_BLUE);
        runBtn.setForeground(Color.WHITE);
        runBtn.addActionListener(e -> generateReport());

        filterCard.add(new JLabel("Report Type:"));
        filterCard.add(reportTypeCombo);
        filterCard.add(new JLabel("From (YYYY-MM-DD):"));
        filterCard.add(startDateField);
        filterCard.add(new JLabel("To:"));
        filterCard.add(endDateField);
        filterCard.add(runBtn);

        // Summary Bar
        JPanel summaryCard = new JPanel(new BorderLayout(8, 8));
        summaryCard.setBackground(UITheme.CARD_BG);
        summaryCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1),
                new EmptyBorder(10, 14, 10, 14)
        ));
        summaryLabel = new JLabel("Generating report analytics...");
        summaryLabel.setFont(UITheme.FONT_BODY_BOLD);
        summaryLabel.setForeground(UITheme.PRIMARY_NAVY);
        summaryCard.add(summaryLabel, BorderLayout.CENTER);

        // Table
        tableModel = new DefaultTableModel();
        reportTable = new CustomTable(tableModel);
        JScrollPane tableScroll = CustomTable.createScrollPane(reportTable);

        JPanel tableContainer = new JPanel(new BorderLayout(8, 8));
        tableContainer.setOpaque(false);
        tableContainer.add(summaryCard, BorderLayout.NORTH);
        tableContainer.add(tableScroll, BorderLayout.CENTER);

        centerPanel.add(filterCard, BorderLayout.NORTH);
        centerPanel.add(tableContainer, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void generateReport() {
        int typeIdx = reportTypeCombo.getSelectedIndex();
        LocalDate start = null;
        LocalDate end = null;
        try {
            if (!startDateField.getText().trim().isEmpty()) start = LocalDate.parse(startDateField.getText().trim());
            if (!endDateField.getText().trim().isEmpty()) end = LocalDate.parse(endDateField.getText().trim());
        } catch (Exception ignored) {}

        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        try {
            switch (typeIdx) {
                case 0 -> { // Monthly Revenue
                    tableModel.addColumn("Billing Month");
                    tableModel.addColumn("Invoices Count");
                    tableModel.addColumn("Room Revenue ($)");
                    tableModel.addColumn("Service Revenue ($)");
                    tableModel.addColumn("Tax Collected ($)");
                    tableModel.addColumn("Total Gross Revenue ($)");

                    List<Map<String, Object>> data = reportService.getMonthlyRevenueReport(start, end);
                    double totalRev = 0;
                    int totalInv = 0;

                    for (Map<String, Object> row : data) {
                        double rev = ((Number) row.get("total_revenue")).doubleValue();
                        totalRev += rev;
                        totalInv += ((Number) row.get("invoice_count")).intValue();

                        tableModel.addRow(new Object[]{
                                row.get("month"),
                                row.get("invoice_count"),
                                CurrencyUtil.format((Number) row.get("room_revenue")),
                                CurrencyUtil.format((Number) row.get("service_revenue")),
                                CurrencyUtil.format((Number) row.get("tax_collected")),
                                CurrencyUtil.format(rev)
                        });
                    }
                    summaryLabel.setText("Monthly Revenue Summary: " + totalInv + " Invoices Settled  |  Total Gross Revenue: " + CurrencyUtil.format(totalRev));
                }
                case 1 -> { // Occupancy by Room Type
                    tableModel.addColumn("Room Type");
                    tableModel.addColumn("Total Rooms");
                    tableModel.addColumn("Currently Occupied");
                    tableModel.addColumn("Total Completed Bookings");
                    tableModel.addColumn("Estimated Occupancy Rate (%)");

                    List<Map<String, Object>> data = reportService.getOccupancyByRoomType();
                    for (Map<String, Object> row : data) {
                        int total = ((Number) row.get("total_rooms")).intValue();
                        int occ = ((Number) row.get("occupied_count")).intValue();
                        double rate = total > 0 ? ((double) occ / total) * 100.0 : 0.0;

                        tableModel.addRow(new Object[]{
                                row.get("room_type"),
                                total,
                                occ,
                                row.get("booking_count"),
                                String.format("%.1f%%", rate)
                        });
                    }
                    summaryLabel.setText("Room Category Occupancy Audit Report");
                }
                case 2 -> { // Top Services
                    tableModel.addColumn("Service Name");
                    tableModel.addColumn("Category");
                    tableModel.addColumn("Times Ordered (Quantity)");
                    tableModel.addColumn("Total Revenue Generated ($)");

                    List<Map<String, Object>> data = reportService.getTopServicesReport(50);
                    double totalServRev = 0;
                    for (Map<String, Object> row : data) {
                        double rev = ((Number) row.get("total_revenue")).doubleValue();
                        totalServRev += rev;
                        tableModel.addRow(new Object[]{
                                row.get("service_name"),
                                row.get("category"),
                                row.get("times_ordered"),
                                CurrencyUtil.format(rev)
                        });
                    }
                    summaryLabel.setText("Top Performing Guest Services  |  Total Service Revenue: " + CurrencyUtil.format(totalServRev));
                }
                case 3 -> { // Live Room Status Breakdown
                    tableModel.addColumn("Operational Status");
                    tableModel.addColumn("Room Count");
                    tableModel.addColumn("Percentage of Total (%)");

                    DashboardStats stats = dashboardService.getStats();
                    int total = Math.max(1, stats.getTotalRooms());

                    tableModel.addRow(new Object[]{"Available", stats.getAvailableRooms(), String.format("%.1f%%", (stats.getAvailableRooms() * 100.0) / total)});
                    tableModel.addRow(new Object[]{"Occupied", stats.getOccupiedRooms(), String.format("%.1f%%", (stats.getOccupiedRooms() * 100.0) / total)});
                    tableModel.addRow(new Object[]{"Reserved", stats.getReservedRooms(), String.format("%.1f%%", (stats.getReservedRooms() * 100.0) / total)});
                    tableModel.addRow(new Object[]{"Cleaning", stats.getCleaningRooms(), String.format("%.1f%%", (stats.getCleaningRooms() * 100.0) / total)});
                    tableModel.addRow(new Object[]{"Maintenance", stats.getMaintenanceRooms(), String.format("%.1f%%", (stats.getMaintenanceRooms() * 100.0) / total)});

                    summaryLabel.setText("Total Fleet: " + stats.getTotalRooms() + " Rooms  |  Current Occupancy: " + String.format("%.1f%%", (stats.getOccupiedRooms() * 100.0) / total));
                }
            }
        } catch (Exception ex) {
            DialogUtil.showError(this, "Failed to build report: " + ex.getMessage());
        }
    }

    private void exportCurrentReport() {
        try {
            String title = (String) reportTypeCombo.getSelectedItem();
            HotelSettings settings = settingsService.getSettings();

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(title.replace(" ", "_") + "_Export.txt"));
            int choice = fileChooser.showSaveDialog(this);
            if (choice == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                PDFExporter.exportTableReport(title, tableModel, summaryLabel.getText(), settings, file);
                DialogUtil.showSuccess(this, "Report exported successfully to:\n" + file.getAbsolutePath());
            }
        } catch (Exception ex) {
            DialogUtil.showError(this, "Export failed: " + ex.getMessage());
        }
    }
}
