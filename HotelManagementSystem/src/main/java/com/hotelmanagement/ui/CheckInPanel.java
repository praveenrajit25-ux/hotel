package com.hotelmanagement.ui;

import com.hotelmanagement.model.CheckIn;
import com.hotelmanagement.model.Reservation;
import com.hotelmanagement.model.ReservationStatus;
import com.hotelmanagement.model.User;
import com.hotelmanagement.service.CheckInService;
import com.hotelmanagement.service.ReservationService;
import com.hotelmanagement.ui.components.CustomTable;
import com.hotelmanagement.util.DateUtil;
import com.hotelmanagement.util.DialogUtil;
import com.hotelmanagement.util.UITheme;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class CheckInPanel extends JPanel {

    private final User currentUser;
    private final CheckInService checkInService;
    private final ReservationService reservationService;

    private DefaultTableModel pendingTableModel;
    private CustomTable pendingTable;
    private List<Reservation> pendingList;

    private DefaultTableModel historyTableModel;
    private CustomTable historyTable;

    public CheckInPanel(User currentUser) {
        this.currentUser = currentUser;
        this.checkInService = new CheckInService();
        this.reservationService = new ReservationService();

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
        JLabel titleLabel = new JLabel("Front Desk Guest Check-In");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.PRIMARY_NAVY);

        JLabel subLabel = new JLabel("Verify guest identity, assign room keys, and activate guest room occupancy");
        subLabel.setFont(UITheme.FONT_BODY);
        subLabel.setForeground(UITheme.TEXT_MUTED);
        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);

        JButton processCheckInBtn = new JButton("Process Check-In for Selected");
        processCheckInBtn.setFont(UITheme.FONT_BODY_BOLD);
        processCheckInBtn.setBackground(UITheme.SUCCESS_GREEN);
        processCheckInBtn.setForeground(Color.WHITE);
        processCheckInBtn.setFocusPainted(false);
        processCheckInBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        processCheckInBtn.addActionListener(e -> processSelectedCheckIn());

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(processCheckInBtn, BorderLayout.EAST);

        // Center Split: Top (Pending Arrivals) and Bottom (Recent Check-In History)
        JPanel contentPanel = new JPanel(new GridLayout(2, 1, 0, 16));
        contentPanel.setOpaque(false);

        // 1. Pending Arrivals Panel
        JPanel pendingCard = new JPanel(new BorderLayout(8, 8));
        pendingCard.setBackground(UITheme.CARD_BG);
        pendingCard.setBorder(UITheme.BORDER_PANEL);

        JLabel pendingTitle = new JLabel("ARRIVALS READY FOR CHECK-IN (CONFIRMED RESERVATIONS)");
        pendingTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pendingTitle.setForeground(UITheme.TEXT_MUTED);

        String[] pendingCols = {"Reservation ID", "Guest Name", "Phone", "Room #", "Room Type", "Check-In Date", "Check-Out Date", "Guests", "Special Requests"};
        pendingTableModel = new DefaultTableModel(pendingCols, 0);
        pendingTable = new CustomTable(pendingTableModel);
        JScrollPane pendingScroll = CustomTable.createScrollPane(pendingTable);

        pendingCard.add(pendingTitle, BorderLayout.NORTH);
        pendingCard.add(pendingScroll, BorderLayout.CENTER);

        // 2. Recent Check-in Activity Log
        JPanel historyCard = new JPanel(new BorderLayout(8, 8));
        historyCard.setBackground(UITheme.CARD_BG);
        historyCard.setBorder(UITheme.BORDER_PANEL);

        JLabel historyTitle = new JLabel("RECENT FRONT DESK CHECK-IN LOGS");
        historyTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        historyTitle.setForeground(UITheme.TEXT_MUTED);

        String[] histCols = {"Log ID", "Res ID", "Guest Name", "Room #", "Key Card Issued", "ID Verified", "Check-In Time", "Processed By", "Notes"};
        historyTableModel = new DefaultTableModel(histCols, 0);
        historyTable = new CustomTable(historyTableModel);
        JScrollPane historyScroll = CustomTable.createScrollPane(historyTable);

        historyCard.add(historyTitle, BorderLayout.NORTH);
        historyCard.add(historyScroll, BorderLayout.CENTER);

        contentPanel.add(pendingCard);
        contentPanel.add(historyCard);

        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            try {
                // 1. Load Confirmed reservations
                List<Reservation> allRes = reservationService.getAllReservations();
                pendingList = new ArrayList<>();
                pendingTableModel.setRowCount(0);
                for (Reservation r : allRes) {
                    if (r.getStatus() == ReservationStatus.Confirmed) {
                        pendingList.add(r);
                        pendingTableModel.addRow(new Object[]{
                                "#" + r.getReservationId(),
                                r.getGuestName(),
                                r.getGuestPhone() != null ? r.getGuestPhone() : "-",
                                r.getRoomNumber(),
                                r.getRoomType(),
                                DateUtil.formatDate(r.getCheckInDate()),
                                DateUtil.formatDate(r.getCheckOutDate()),
                                r.getNumberOfGuests(),
                                r.getSpecialRequests() != null ? r.getSpecialRequests() : "-"
                        });
                    }
                }

                // 2. Load Check-In logs
                List<CheckIn> checkIns = checkInService.getAllCheckIns();
                historyTableModel.setRowCount(0);
                for (CheckIn ci : checkIns) {
                    historyTableModel.addRow(new Object[]{
                            "CI-" + ci.getCheckInId(),
                            "#" + ci.getReservationId(),
                            ci.getGuestName(),
                            ci.getRoomNumber(),
                            ci.getRoomKeyIssued(),
                            ci.isVerifiedIdProof() ? "Yes" : "No",
                            DateUtil.formatDateTime(ci.getCheckInTime()),
                            ci.getProcessedByStaffName() != null ? ci.getProcessedByStaffName() : "System",
                            ci.getNotes() != null ? ci.getNotes() : "-"
                    });
                }
            } catch (Exception ex) {
                DialogUtil.showError(this, "Failed to load check-in data: " + ex.getMessage());
            }
        });
    }

    private void processSelectedCheckIn() {
        int row = pendingTable.getSelectedRow();
        if (row < 0 || pendingList == null || row >= pendingList.size()) {
            DialogUtil.showWarning(this, "Please select an arrival reservation from the top table.");
            return;
        }

        Reservation res = pendingList.get(row);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Guest Check-In: " + res.getGuestName(), true);
        dialog.setSize(460, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel resLabel = new JLabel("#" + res.getReservationId() + " (" + res.getGuestName() + ")");
        JLabel roomLabel = new JLabel("Room " + res.getRoomNumber() + " (" + res.getRoomType() + ")");
        JTextField keyField = new JTextField("Card Key #" + res.getRoomNumber());
        JCheckBox idCheckBox = new JCheckBox("Confirmed Physical ID / Passport Match", true);
        JTextField notesField = new JTextField();

        form.add(new JLabel("Reservation / Guest:")); form.add(resLabel);
        form.add(new JLabel("Room Assigned:")); form.add(roomLabel);
        form.add(new JLabel("Room Key Identifier:")); form.add(keyField);
        form.add(new JLabel("Identity Verification:")); form.add(idCheckBox);
        form.add(new JLabel("Check-in Notes:")); form.add(notesField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton confirmBtn = new JButton("Complete Check-In");
        confirmBtn.setBackground(UITheme.SUCCESS_GREEN);
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.addActionListener(e -> {
            try {
                if (!idCheckBox.isSelected()) {
                    boolean proceed = DialogUtil.confirm(dialog, "Warning: ID Proof is not marked as verified. Proceed anyway?", "ID Verification");
                    if (!proceed) return;
                }

                int staffId = currentUser != null ? currentUser.getUserId() : null;
                checkInService.processCheckIn(
                        res.getReservationId(),
                        keyField.getText().trim(),
                        idCheckBox.isSelected(),
                        notesField.getText().trim(),
                        staffId
                );

                DialogUtil.showSuccess(dialog, "Guest " + res.getGuestName() + " checked in successfully to Room " + res.getRoomNumber() + "!");
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
