package com.hotelmanagement.ui;

import com.hotelmanagement.model.*;
import com.hotelmanagement.service.GuestService;
import com.hotelmanagement.service.ReservationService;
import com.hotelmanagement.service.RoomService;
import com.hotelmanagement.ui.components.CustomTable;
import com.hotelmanagement.ui.components.Sidebar;
import com.hotelmanagement.util.CurrencyUtil;
import com.hotelmanagement.util.DateUtil;
import com.hotelmanagement.util.DialogUtil;
import com.hotelmanagement.util.UITheme;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class ReservationManagementPanel extends JPanel {

    private final User currentUser;
    private final ReservationService reservationService;
    private final GuestService guestService;
    private final RoomService roomService;
    private final Consumer<String> navigator;

    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private JTextField fromDateField;
    private JTextField toDateField;

    private DefaultTableModel tableModel;
    private CustomTable resTable;
    private List<Reservation> currentList;

    public ReservationManagementPanel(User currentUser, Consumer<String> navigator) {
        this.currentUser = currentUser;
        this.navigator = navigator;
        this.reservationService = new ReservationService();
        this.guestService = new GuestService();
        this.roomService = new RoomService();

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshReservations();
    }

    private void initUI() {
        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1));
        titleBlock.setOpaque(false);
        JLabel titleLabel = new JLabel("Reservation & Booking Management");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.PRIMARY_NAVY);

        JLabel subLabel = new JLabel("Manage guest bookings, dates, room allocations, cancellations, and check-in workflows");
        subLabel.setFont(UITheme.FONT_BODY);
        subLabel.setForeground(UITheme.TEXT_MUTED);
        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);

        JButton addResBtn = new JButton("+ Create New Booking");
        addResBtn.setFont(UITheme.FONT_BODY_BOLD);
        addResBtn.setBackground(UITheme.PRIMARY_NAVY);
        addResBtn.setForeground(Color.WHITE);
        addResBtn.setFocusPainted(false);
        addResBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addResBtn.addActionListener(e -> showCreateReservationDialog());

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(addResBtn, BorderLayout.EAST);

        // Center Area: Filter Bar + Table + Action Controls
        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.setOpaque(false);

        // Filter Bar
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterCard.setBackground(UITheme.CARD_BG);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        searchField = new JTextField(12);
        searchField.setPreferredSize(new Dimension(140, 32));
        searchField.setBorder(UITheme.BORDER_INPUT);

        statusFilterCombo = new JComboBox<>(new String[]{"All Statuses", "Confirmed", "Checked-In", "Checked-Out", "Cancelled"});
        statusFilterCombo.setPreferredSize(new Dimension(130, 32));

        fromDateField = new JTextField(8);
        fromDateField.setPreferredSize(new Dimension(90, 32));
        fromDateField.setBorder(UITheme.BORDER_INPUT);

        toDateField = new JTextField(8);
        toDateField.setPreferredSize(new Dimension(90, 32));
        toDateField.setBorder(UITheme.BORDER_INPUT);

        JButton searchBtn = new JButton("Filter");
        searchBtn.setFont(UITheme.FONT_BODY_BOLD);
        searchBtn.setBackground(UITheme.PRIMARY_NAVY);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.addActionListener(e -> applyFilter());

        JButton resetBtn = new JButton("Reset");
        resetBtn.setFont(UITheme.FONT_BODY);
        resetBtn.setBackground(Color.WHITE);
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            statusFilterCombo.setSelectedIndex(0);
            fromDateField.setText("");
            toDateField.setText("");
            refreshReservations();
        });

        filterCard.add(new JLabel("Search (Guest / Room / ID):"));
        filterCard.add(searchField);
        filterCard.add(new JLabel("Status:"));
        filterCard.add(statusFilterCombo);
        filterCard.add(new JLabel("From (YYYY-MM-DD):"));
        filterCard.add(fromDateField);
        filterCard.add(new JLabel("To:"));
        filterCard.add(toDateField);
        filterCard.add(searchBtn);
        filterCard.add(resetBtn);

        // Table
        String[] columns = {"ID", "Guest Name", "Phone", "Room", "Type", "Check-In", "Check-Out", "Nights", "Guests", "Status", "Special Requests"};
        tableModel = new DefaultTableModel(columns, 0);
        resTable = new CustomTable(tableModel);
        JScrollPane tableScroll = CustomTable.createScrollPane(resTable);

        // Bottom Action Bar
        JPanel bottomActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottomActionPanel.setBackground(UITheme.CARD_BG);
        bottomActionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1),
                new EmptyBorder(6, 12, 6, 12)
        ));

        JButton checkInDirectBtn = new JButton("Proceed to Check-In");
        checkInDirectBtn.setFont(UITheme.FONT_BODY_BOLD);
        checkInDirectBtn.setBackground(UITheme.SUCCESS_GREEN);
        checkInDirectBtn.setForeground(Color.WHITE);
        checkInDirectBtn.addActionListener(e -> proceedToFrontDeskCheckIn());

        JButton billingDirectBtn = new JButton("View / Issue Bill");
        billingDirectBtn.setFont(UITheme.FONT_BODY);
        billingDirectBtn.setBackground(UITheme.ACCENT_GOLD);
        billingDirectBtn.setForeground(Color.WHITE);
        billingDirectBtn.addActionListener(e -> proceedToBilling());

        JButton cancelResBtn = new JButton("Cancel Reservation");
        cancelResBtn.setFont(UITheme.FONT_BODY);
        cancelResBtn.setBackground(new Color(254, 242, 242));
        cancelResBtn.setForeground(UITheme.DANGER_RED);
        cancelResBtn.addActionListener(e -> cancelSelectedReservation());

        bottomActionPanel.add(new JLabel("Selected Booking Actions: "));
        bottomActionPanel.add(checkInDirectBtn);
        bottomActionPanel.add(billingDirectBtn);
        bottomActionPanel.add(cancelResBtn);

        centerPanel.add(filterCard, BorderLayout.NORTH);
        centerPanel.add(tableScroll, BorderLayout.CENTER);
        centerPanel.add(bottomActionPanel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void refreshReservations() {
        applyFilter();
    }

    private void applyFilter() {
        String kw = searchField.getText().trim();
        String st = statusFilterCombo.getSelectedItem().toString();
        if (st.startsWith("All")) st = "All";

        LocalDate from = null;
        if (!fromDateField.getText().trim().isEmpty()) {
            try { from = LocalDate.parse(fromDateField.getText().trim()); } catch (Exception ignored) {}
        }
        LocalDate to = null;
        if (!toDateField.getText().trim().isEmpty()) {
            try { to = LocalDate.parse(toDateField.getText().trim()); } catch (Exception ignored) {}
        }

        try {
            currentList = reservationService.searchReservations(kw, st, from, to);
            tableModel.setRowCount(0);
            for (Reservation r : currentList) {
                long nights = ChronoUnit.DAYS.between(r.getCheckInDate(), r.getCheckOutDate());
                tableModel.addRow(new Object[]{
                        "#" + r.getReservationId(),
                        r.getGuestName(),
                        r.getGuestPhone() != null ? r.getGuestPhone() : "-",
                        r.getRoomNumber(),
                        r.getRoomType(),
                        DateUtil.formatDate(r.getCheckInDate()),
                        DateUtil.formatDate(r.getCheckOutDate()),
                        nights + " Night(s)",
                        r.getNumberOfGuests(),
                        r.getStatus().getDisplayName(),
                        r.getSpecialRequests() != null ? r.getSpecialRequests() : "-"
                });
            }
        } catch (Exception ex) {
            DialogUtil.showError(this, "Failed to search reservations: " + ex.getMessage());
        }
    }

    public Reservation getSelectedReservation() {
        int row = resTable.getSelectedRow();
        if (row < 0 || currentList == null || row >= currentList.size()) {
            DialogUtil.showWarning(this, "Please select a reservation from the table.");
            return null;
        }
        return currentList.get(row);
    }

    private void showCreateReservationDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create New Reservation", true);
        dialog.setSize(560, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Guest selector combo
        List<Guest> guests = guestService.getAllGuests();
        JComboBox<GuestComboItem> guestCombo = new JComboBox<>();
        for (Guest g : guests) {
            guestCombo.addItem(new GuestComboItem(g));
        }

        JTextField checkInField = new JTextField(LocalDate.now().toString());
        JTextField checkOutField = new JTextField(LocalDate.now().plusDays(2).toString());
        JSpinner guestSpin = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));

        // Dynamic Room selector combo
        JComboBox<RoomComboItem> roomCombo = new JComboBox<>();
        JButton refreshRoomsBtn = new JButton("Find Available Rooms");

        Runnable updateAvailableRooms = () -> {
            try {
                LocalDate cin = LocalDate.parse(checkInField.getText().trim());
                LocalDate cout = LocalDate.parse(checkOutField.getText().trim());
                int gcount = (Integer) guestSpin.getValue();
                List<Room> avail = roomService.findAvailableRooms(cin, cout, null, gcount, null);
                roomCombo.removeAllItems();
                for (Room rm : avail) {
                    roomCombo.addItem(new RoomComboItem(rm));
                }
                if (avail.isEmpty()) {
                    DialogUtil.showWarning(dialog, "No rooms are available for these dates and guest capacity.");
                }
            } catch (Exception ex) {
                DialogUtil.showError(dialog, "Date error: " + ex.getMessage());
            }
        };

        refreshRoomsBtn.addActionListener(e -> updateAvailableRooms.run());

        JTextField specialReqField = new JTextField();
        JLabel estPriceLabel = new JLabel("$0.00");
        estPriceLabel.setFont(UITheme.FONT_BODY_BOLD);
        estPriceLabel.setForeground(UITheme.ACCENT_GOLD);

        form.add(new JLabel("Select Guest *:"));
        form.add(guestCombo);
        form.add(new JLabel("Check-In Date (YYYY-MM-DD) *:"));
        form.add(checkInField);
        form.add(new JLabel("Check-Out Date (YYYY-MM-DD) *:"));
        form.add(checkOutField);
        form.add(new JLabel("Number of Guests *:"));
        form.add(guestSpin);
        form.add(new JLabel("Available Rooms:"));
        JPanel roomPickRow = new JPanel(new BorderLayout(4, 0));
        roomPickRow.add(roomCombo, BorderLayout.CENTER);
        roomPickRow.add(refreshRoomsBtn, BorderLayout.EAST);
        form.add(roomPickRow);
        form.add(new JLabel("Special Requests:"));
        form.add(specialReqField);
        form.add(new JLabel("Est. Total Room Charges:"));
        form.add(estPriceLabel);

        // Load initial rooms
        updateAvailableRooms.run();

        // Calculate estimate when room changes
        roomCombo.addActionListener(e -> {
            RoomComboItem item = (RoomComboItem) roomCombo.getSelectedItem();
            if (item != null && item.room != null) {
                try {
                    LocalDate cin = LocalDate.parse(checkInField.getText().trim());
                    LocalDate cout = LocalDate.parse(checkOutField.getText().trim());
                    long n = ChronoUnit.DAYS.between(cin, cout);
                    if (n <= 0) n = 1;
                    BigDecimal total = item.room.getPricePerNight().multiply(BigDecimal.valueOf(n));
                    estPriceLabel.setText(CurrencyUtil.format(total) + " (" + n + " nights @ " + CurrencyUtil.format(item.room.getPricePerNight()) + ")");
                } catch (Exception ignored) {}
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Confirm Booking");
        saveBtn.setBackground(UITheme.PRIMARY_NAVY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            try {
                GuestComboItem gItem = (GuestComboItem) guestCombo.getSelectedItem();
                RoomComboItem rItem = (RoomComboItem) roomCombo.getSelectedItem();
                if (gItem == null) throw new IllegalArgumentException("Please select a guest.");
                if (rItem == null) throw new IllegalArgumentException("Please select an available room.");

                Reservation r = new Reservation();
                r.setGuestId(gItem.guest.getGuestId());
                r.setRoomId(rItem.room.getRoomId());
                r.setCheckInDate(LocalDate.parse(checkInField.getText().trim()));
                r.setCheckOutDate(LocalDate.parse(checkOutField.getText().trim()));
                r.setNumberOfGuests((Integer) guestSpin.getValue());
                r.setStatus(ReservationStatus.Confirmed);
                r.setSpecialRequests(specialReqField.getText().trim());
                r.setCreatedByUserId(currentUser != null ? currentUser.getUserId() : null);

                int resId = reservationService.createReservation(r);
                DialogUtil.showSuccess(dialog, "Reservation #" + resId + " created successfully for Room " + rItem.room.getRoomNumber());
                dialog.dispose();
                refreshReservations();
            } catch (Exception ex) {
                DialogUtil.showError(dialog, ex.getMessage());
            }
        });

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void cancelSelectedReservation() {
        Reservation r = getSelectedReservation();
        if (r == null) return;

        boolean confirm = DialogUtil.confirm(this,
                "Are you sure you want to cancel Reservation #" + r.getReservationId() + " for " + r.getGuestName() + "?",
                "Confirm Cancellation");
        if (confirm) {
            try {
                reservationService.cancelReservation(r.getReservationId());
                DialogUtil.showSuccess(this, "Reservation #" + r.getReservationId() + " cancelled and room released.");
                refreshReservations();
            } catch (Exception ex) {
                DialogUtil.showError(this, ex.getMessage());
            }
        }
    }

    private void proceedToFrontDeskCheckIn() {
        Reservation r = getSelectedReservation();
        if (r == null) return;
        if (navigator != null) {
            navigator.accept(Sidebar.CARD_CHECK_IN);
        }
    }

    private void proceedToBilling() {
        Reservation r = getSelectedReservation();
        if (r == null) return;
        if (navigator != null) {
            navigator.accept(Sidebar.CARD_BILLING);
        }
    }

    // Helper Combobox Wrappers
    private static class GuestComboItem {
        final Guest guest;
        GuestComboItem(Guest g) { this.guest = g; }
        @Override
        public String toString() { return "#" + guest.getGuestId() + " - " + guest.getFullName() + " (" + guest.getPhone() + ")"; }
    }

    private static class RoomComboItem {
        final Room room;
        RoomComboItem(Room r) { this.room = r; }
        @Override
        public String toString() { return "Room " + room.getRoomNumber() + " - " + room.getRoomType() + " ($" + room.getPricePerNight() + "/nt, Cap: " + room.getCapacity() + ")"; }
    }
}
