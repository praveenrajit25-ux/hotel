package com.hotelmanagement.ui;

import com.hotelmanagement.model.CheckOut;
import com.hotelmanagement.model.Invoice;
import com.hotelmanagement.model.Reservation;
import com.hotelmanagement.model.ReservationStatus;
import com.hotelmanagement.model.User;
import com.hotelmanagement.service.BillingService;
import com.hotelmanagement.service.CheckOutService;
import com.hotelmanagement.service.ReservationService;
import com.hotelmanagement.ui.components.CustomTable;
import com.hotelmanagement.ui.components.Sidebar;
import com.hotelmanagement.util.CurrencyUtil;
import com.hotelmanagement.util.DateUtil;
import com.hotelmanagement.util.DialogUtil;
import com.hotelmanagement.util.UITheme;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class CheckOutPanel extends JPanel {

    private final User currentUser;
    private final CheckOutService checkOutService;
    private final ReservationService reservationService;
    private final BillingService billingService;
    private final Consumer<String> navigator;

    private DefaultTableModel activeTableModel;
    private CustomTable activeTable;
    private List<Reservation> inHouseList;

    private DefaultTableModel historyTableModel;
    private CustomTable historyTable;

    public CheckOutPanel(User currentUser, Consumer<String> navigator) {
        this.currentUser = currentUser;
        this.navigator = navigator;
        this.checkOutService = new CheckOutService();
        this.reservationService = new ReservationService();
        this.billingService = new BillingService();

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshData();
    }

    private void initUI() {
        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1));
        titleBlock.setOpaque(false);
        JLabel titleLabel = new JLabel("Front Desk Express Check-Out");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.PRIMARY_NAVY);

        JLabel subLabel = new JLabel("Audit billing settlement, inspect room condition, return keys, and schedule housekeeping");
        subLabel.setFont(UITheme.FONT_BODY);
        subLabel.setForeground(UITheme.TEXT_MUTED);
        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        JButton billingBtn = new JButton("Open Billing & Payments");
        billingBtn.setFont(UITheme.FONT_BODY);
        billingBtn.setBackground(UITheme.ACCENT_GOLD);
        billingBtn.setForeground(Color.WHITE);
        billingBtn.setFocusPainted(false);
        billingBtn.addActionListener(e -> {
            if (navigator != null) navigator.accept(Sidebar.CARD_BILLING);
        });

        JButton processCheckOutBtn = new JButton("Complete Check-Out");
        processCheckOutBtn.setFont(UITheme.FONT_BODY_BOLD);
        processCheckOutBtn.setBackground(UITheme.WARNING_ORANGE);
        processCheckOutBtn.setForeground(Color.WHITE);
        processCheckOutBtn.setFocusPainted(false);
        processCheckOutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        processCheckOutBtn.addActionListener(e -> processSelectedCheckOut());

        btnRow.add(billingBtn);
        btnRow.add(processCheckOutBtn);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(btnRow, BorderLayout.EAST);

        // Center Content Split (Top: In-House Guests, Bottom: Check-Out History)
        JPanel contentPanel = new JPanel(new GridLayout(2, 1, 0, 16));
        contentPanel.setOpaque(false);

        // 1. In-House Checked-In Guests
        JPanel inHouseCard = new JPanel(new BorderLayout(8, 8));
        inHouseCard.setBackground(UITheme.CARD_BG);
        inHouseCard.setBorder(UITheme.BORDER_PANEL);

        JLabel inHouseTitle = new JLabel("IN-HOUSE GUESTS (CHECKED-IN)");
        inHouseTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        inHouseTitle.setForeground(UITheme.TEXT_MUTED);

        String[] inHouseCols = {"Res ID", "Guest Name", "Phone", "Room #", "Room Type", "Check-In Date", "Check-Out Date", "Invoice #", "Billing Balance"};
        activeTableModel = new DefaultTableModel(inHouseCols, 0);
        activeTable = new CustomTable(activeTableModel);
        JScrollPane activeScroll = CustomTable.createScrollPane(activeTable);

        inHouseCard.add(inHouseTitle, BorderLayout.NORTH);
        inHouseCard.add(activeScroll, BorderLayout.CENTER);

        // 2. Completed Check-Out Logs
        JPanel historyCard = new JPanel(new BorderLayout(8, 8));
        historyCard.setBackground(UITheme.CARD_BG);
        historyCard.setBorder(UITheme.BORDER_PANEL);

        JLabel histTitle = new JLabel("RECENT CHECK-OUT & ROOM RELEASE LOGS");
        histTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        histTitle.setForeground(UITheme.TEXT_MUTED);

        String[] histCols = {"Log ID", "Res ID", "Guest Name", "Room #", "Room Inspected", "Key Returned", "Check-Out Time", "Processed By", "Notes"};
        historyTableModel = new DefaultTableModel(histCols, 0);
        historyTable = new CustomTable(historyTableModel);
        JScrollPane historyScroll = CustomTable.createScrollPane(historyTable);

        historyCard.add(histTitle, BorderLayout.NORTH);
        historyCard.add(historyScroll, BorderLayout.CENTER);

        contentPanel.add(inHouseCard);
        contentPanel.add(historyCard);

        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            try {
                // 1. Load In-house Checked-In reservations
                List<Reservation> allRes = reservationService.getAllReservations();
                inHouseList = new ArrayList<>();
                activeTableModel.setRowCount(0);
                for (Reservation r : allRes) {
                    if (r.getStatus() == ReservationStatus.Checked_In) {
                        inHouseList.add(r);
                        Invoice inv = billingService.getInvoiceByReservationId(r.getReservationId());
                        String invNum = inv != null ? inv.getInvoiceNumber() : "Not Generated";
                        String balance = inv != null ? CurrencyUtil.format(inv.getBalanceAmount()) : "Pending Calc";

                        activeTableModel.addRow(new Object[]{
                                "#" + r.getReservationId(),
                                r.getGuestName(),
                                r.getGuestPhone() != null ? r.getGuestPhone() : "-",
                                r.getRoomNumber(),
                                r.getRoomType(),
                                DateUtil.formatDate(r.getCheckInDate()),
                                DateUtil.formatDate(r.getCheckOutDate()),
                                invNum,
                                balance
                        });
                    }
                }

                // 2. Load Check-Out logs
                List<CheckOut> checkOuts = checkOutService.getAllCheckOuts();
                historyTableModel.setRowCount(0);
                for (CheckOut co : checkOuts) {
                    historyTableModel.addRow(new Object[]{
                            "CO-" + co.getCheckOutId(),
                            "#" + co.getReservationId(),
                            co.getGuestName(),
                            co.getRoomNumber(),
                            co.isRoomInspected() ? "Inspected" : "Pending",
                            co.isKeyReturned() ? "Returned" : "Missing",
                            DateUtil.formatDateTime(co.getCheckOutTime()),
                            co.getProcessedByStaffName() != null ? co.getProcessedByStaffName() : "System",
                            co.getNotes() != null ? co.getNotes() : "-"
                    });
                }
            } catch (Exception ex) {
                DialogUtil.showError(this, "Failed to load checkout data: " + ex.getMessage());
            }
        });
    }

    private void processSelectedCheckOut() {
        int row = activeTable.getSelectedRow();
        if (row < 0 || inHouseList == null || row >= inHouseList.size()) {
            DialogUtil.showWarning(this, "Please select an in-house guest from the top table.");
            return;
        }

        Reservation res = inHouseList.get(row);

        // Check if invoice exists and has balance
        Invoice invoice = billingService.getInvoiceByReservationId(res.getReservationId());
        if (invoice == null) {
            boolean gen = DialogUtil.confirm(this,
                    "No invoice has been created for Reservation #" + res.getReservationId() + " yet.\nWould you like to automatically generate and audit the invoice now?",
                    "Generate Invoice Required");
            if (gen) {
                invoice = billingService.generateOrUpdateInvoice(res.getReservationId(), BigDecimal.ZERO, BigDecimal.ZERO, null);
            } else {
                return;
            }
        }

        if (invoice.getBalanceAmount() != null && invoice.getBalanceAmount().compareTo(BigDecimal.ZERO) > 0) {
            DialogUtil.showWarning(this,
                    "Guest has an outstanding balance of " + CurrencyUtil.format(invoice.getBalanceAmount()) +
                    ".\nPlease navigate to Billing & Payments to collect the payment before checkout.");
            if (navigator != null) {
                navigator.accept(Sidebar.CARD_BILLING);
            }
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Complete Check-Out: " + res.getGuestName(), true);
        dialog.setSize(480, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel resLabel = new JLabel("#" + res.getReservationId() + " (" + res.getGuestName() + ")");
        JLabel roomLabel = new JLabel("Room " + res.getRoomNumber() + " (" + res.getRoomType() + ")");
        JLabel billStatus = new JLabel("Fully Paid ($0.00 Balance)");
        billStatus.setForeground(UITheme.SUCCESS_GREEN);
        billStatus.setFont(UITheme.FONT_BODY_BOLD);

        JCheckBox inspectCheck = new JCheckBox("Room Inspected & Clear of Damages", true);
        JCheckBox keyCheck = new JCheckBox("Key Cards Returned to Front Desk", true);
        JTextField notesField = new JTextField();

        form.add(new JLabel("Reservation / Guest:")); form.add(resLabel);
        form.add(new JLabel("Room Vacated:")); form.add(roomLabel);
        form.add(new JLabel("Billing Status:")); form.add(billStatus);
        form.add(new JLabel("Room Inspection:")); form.add(inspectCheck);
        form.add(new JLabel("Key Handover:")); form.add(keyCheck);
        form.add(new JLabel("Check-out Notes:")); form.add(notesField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton confirmBtn = new JButton("Confirm Check-Out & Release Room");
        confirmBtn.setBackground(UITheme.WARNING_ORANGE);
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.addActionListener(e -> {
            try {
                int staffId = currentUser != null ? currentUser.getUserId() : null;
                checkOutService.processCheckOut(
                        res.getReservationId(),
                        inspectCheck.isSelected(),
                        keyCheck.isSelected(),
                        notesField.getText().trim(),
                        staffId
                );

                DialogUtil.showSuccess(dialog, "Check-out completed! Room " + res.getRoomNumber() + " is now set to 'Cleaning' for housekeeping.");
                dialog.dispose();
                refreshData();
            } catch (Exception ex) {
                DialogUtil.showError(dialog, ex.getMessage());
            }
        });

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(cancelBtn);
        btnPanel.add(confirmBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
