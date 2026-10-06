package com.hotelmanagement.ui;

import com.hotelmanagement.model.Guest;
import com.hotelmanagement.model.IdProofType;
import com.hotelmanagement.model.Reservation;
import com.hotelmanagement.model.User;
import com.hotelmanagement.service.GuestService;
import com.hotelmanagement.ui.components.CustomTable;
import com.hotelmanagement.util.DateUtil;
import com.hotelmanagement.util.DialogUtil;
import com.hotelmanagement.util.UITheme;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class GuestManagementPanel extends JPanel {

    private final User currentUser;
    private final GuestService guestService;

    private JTextField searchField;
    private DefaultTableModel tableModel;
    private CustomTable guestTable;
    private List<Guest> currentGuestList;

    public GuestManagementPanel(User currentUser) {
        this.currentUser = currentUser;
        this.guestService = new GuestService();

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshGuests();
    }

    private void initUI() {
        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1));
        titleBlock.setOpaque(false);
        JLabel titleLabel = new JLabel("Guest Directory & CRM");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.PRIMARY_NAVY);

        JLabel subLabel = new JLabel("Manage guest profiles, identification records, contact info, and booking history");
        subLabel.setFont(UITheme.FONT_BODY);
        subLabel.setForeground(UITheme.TEXT_MUTED);
        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);

        JButton addGuestBtn = new JButton("+ Register New Guest");
        addGuestBtn.setFont(UITheme.FONT_BODY_BOLD);
        addGuestBtn.setBackground(UITheme.PRIMARY_NAVY);
        addGuestBtn.setForeground(Color.WHITE);
        addGuestBtn.setFocusPainted(false);
        addGuestBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addGuestBtn.addActionListener(e -> showAddGuestDialog());

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(addGuestBtn, BorderLayout.EAST);

        // Center Area: Search Bar + Table + Action Bar
        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.setOpaque(false);

        // Search Bar
        JPanel searchCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        searchCard.setBackground(UITheme.CARD_BG);
        searchCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        searchField = new JTextField(25);
        searchField.setPreferredSize(new Dimension(240, 32));
        searchField.setBorder(UITheme.BORDER_INPUT);

        JButton searchBtn = new JButton("Search Guests");
        searchBtn.setFont(UITheme.FONT_BODY_BOLD);
        searchBtn.setBackground(UITheme.PRIMARY_NAVY);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.addActionListener(e -> applySearch());

        JButton resetBtn = new JButton("Reset");
        resetBtn.setFont(UITheme.FONT_BODY);
        resetBtn.setBackground(Color.WHITE);
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            refreshGuests();
        });

        searchCard.add(new JLabel("Search (Name / Phone / Email / ID Proof / Guest ID):"));
        searchCard.add(searchField);
        searchCard.add(searchBtn);
        searchCard.add(resetBtn);

        // Table
        String[] columns = {"Guest ID", "Full Name", "Phone", "Email", "ID Type", "ID Number", "Nationality", "Registered On"};
        tableModel = new DefaultTableModel(columns, 0);
        guestTable = new CustomTable(tableModel);
        JScrollPane tableScroll = CustomTable.createScrollPane(guestTable);

        // Bottom Action Controls
        JPanel bottomActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottomActionPanel.setBackground(UITheme.CARD_BG);
        bottomActionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1),
                new EmptyBorder(6, 12, 6, 12)
        ));

        JButton editBtn = new JButton("Edit Profile");
        editBtn.setFont(UITheme.FONT_BODY);
        editBtn.setBackground(Color.WHITE);
        editBtn.addActionListener(e -> editSelectedGuest());

        JButton historyBtn = new JButton("View Booking History");
        historyBtn.setFont(UITheme.FONT_BODY_BOLD);
        historyBtn.setBackground(UITheme.INFO_BLUE);
        historyBtn.setForeground(Color.WHITE);
        historyBtn.addActionListener(e -> viewGuestHistory());

        JButton deleteBtn = new JButton("Delete Guest");
        deleteBtn.setFont(UITheme.FONT_BODY);
        deleteBtn.setBackground(new Color(254, 242, 242));
        deleteBtn.setForeground(UITheme.DANGER_RED);
        deleteBtn.addActionListener(e -> deleteSelectedGuest());

        bottomActionPanel.add(new JLabel("Selected Guest Actions: "));
        bottomActionPanel.add(editBtn);
        bottomActionPanel.add(historyBtn);
        bottomActionPanel.add(deleteBtn);

        centerPanel.add(searchCard, BorderLayout.NORTH);
        centerPanel.add(tableScroll, BorderLayout.CENTER);
        centerPanel.add(bottomActionPanel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void refreshGuests() {
        applySearch();
    }

    private void applySearch() {
        String kw = searchField.getText().trim();
        try {
            currentGuestList = guestService.searchGuests(kw);
            tableModel.setRowCount(0);
            for (Guest g : currentGuestList) {
                tableModel.addRow(new Object[]{
                        "#" + g.getGuestId(),
                        g.getFullName(),
                        g.getPhone(),
                        g.getEmail() != null ? g.getEmail() : "-",
                        g.getIdProofType(),
                        g.getIdProofNumber(),
                        g.getNationality() != null ? g.getNationality() : "USA",
                        DateUtil.formatDateTime(g.getRegistrationDate())
                });
            }
        } catch (Exception ex) {
            DialogUtil.showError(this, "Failed to search guests: " + ex.getMessage());
        }
    }

    private Guest getSelectedGuest() {
        int row = guestTable.getSelectedRow();
        if (row < 0 || currentGuestList == null || row >= currentGuestList.size()) {
            DialogUtil.showWarning(this, "Please select a guest from the table.");
            return null;
        }
        return currentGuestList.get(row);
    }

    private void showAddGuestDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register New Guest", true);
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(8, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField nameField = new JTextField();
        JComboBox<String> genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        JTextField dobField = new JTextField("1990-01-01");
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField addressField = new JTextField();
        JComboBox<IdProofType> idTypeCombo = new JComboBox<>(IdProofType.values());
        JTextField idNumField = new JTextField();

        form.add(new JLabel("Full Name *:")); form.add(nameField);
        form.add(new JLabel("Gender:")); form.add(genderCombo);
        form.add(new JLabel("Date of Birth (YYYY-MM-DD):")); form.add(dobField);
        form.add(new JLabel("Phone Number *:")); form.add(phoneField);
        form.add(new JLabel("Email Address *:")); form.add(emailField);
        form.add(new JLabel("Street / City Address:")); form.add(addressField);
        form.add(new JLabel("ID Proof Type *:")); form.add(idTypeCombo);
        form.add(new JLabel("ID Proof Number *:")); form.add(idNumField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Register Guest");
        saveBtn.setBackground(UITheme.PRIMARY_NAVY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            try {
                Guest g = new Guest();
                g.setFullName(nameField.getText().trim());
                g.setGender((String) genderCombo.getSelectedItem());
                if (!dobField.getText().trim().isEmpty()) {
                    g.setDateOfBirth(LocalDate.parse(dobField.getText().trim()));
                }
                g.setPhone(phoneField.getText().trim());
                g.setEmail(emailField.getText().trim());
                g.setAddress(addressField.getText().trim());
                g.setIdProofType((IdProofType) idTypeCombo.getSelectedItem());
                g.setIdProofNumber(idNumField.getText().trim());
                g.setNationality("USA");

                int id = guestService.registerGuest(g);
                DialogUtil.showSuccess(dialog, "Guest registered successfully with ID #" + id);
                dialog.dispose();
                refreshGuests();
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

    private void editSelectedGuest() {
        Guest g = getSelectedGuest();
        if (g == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Guest: " + g.getFullName(), true);
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(8, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField nameField = new JTextField(g.getFullName());
        JComboBox<String> genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        genderCombo.setSelectedItem(g.getGender() != null ? g.getGender() : "Male");
        JTextField dobField = new JTextField(g.getDateOfBirth() != null ? g.getDateOfBirth().toString() : "");
        JTextField phoneField = new JTextField(g.getPhone());
        JTextField emailField = new JTextField(g.getEmail());
        JTextField addressField = new JTextField(g.getAddress() != null ? g.getAddress() : "");
        JComboBox<IdProofType> idTypeCombo = new JComboBox<>(IdProofType.values());
        idTypeCombo.setSelectedItem(g.getIdProofType());
        JTextField idNumField = new JTextField(g.getIdProofNumber());

        form.add(new JLabel("Full Name *:")); form.add(nameField);
        form.add(new JLabel("Gender:")); form.add(genderCombo);
        form.add(new JLabel("Date of Birth (YYYY-MM-DD):")); form.add(dobField);
        form.add(new JLabel("Phone Number *:")); form.add(phoneField);
        form.add(new JLabel("Email Address *:")); form.add(emailField);
        form.add(new JLabel("Street / City Address:")); form.add(addressField);
        form.add(new JLabel("ID Proof Type *:")); form.add(idTypeCombo);
        form.add(new JLabel("ID Proof Number *:")); form.add(idNumField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Update Profile");
        saveBtn.setBackground(UITheme.PRIMARY_NAVY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            try {
                g.setFullName(nameField.getText().trim());
                g.setGender((String) genderCombo.getSelectedItem());
                if (!dobField.getText().trim().isEmpty()) {
                    g.setDateOfBirth(LocalDate.parse(dobField.getText().trim()));
                }
                g.setPhone(phoneField.getText().trim());
                g.setEmail(emailField.getText().trim());
                g.setAddress(addressField.getText().trim());
                g.setIdProofType((IdProofType) idTypeCombo.getSelectedItem());
                g.setIdProofNumber(idNumField.getText().trim());

                guestService.updateGuest(g);
                DialogUtil.showSuccess(dialog, "Guest profile updated successfully!");
                dialog.dispose();
                refreshGuests();
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

    private void viewGuestHistory() {
        Guest g = getSelectedGuest();
        if (g == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Booking History - " + g.getFullName(), true);
        dialog.setSize(750, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel topInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        topInfo.setBackground(UITheme.CARD_BG);
        topInfo.setBorder(new EmptyBorder(10, 10, 10, 10));
        topInfo.add(new JLabel("Guest: " + g.getFullName() + " (Phone: " + g.getPhone() + ", Email: " + g.getEmail() + ")"));

        String[] cols = {"Res ID", "Room #", "Room Type", "Check-In", "Check-Out", "Guests", "Status", "Special Requests"};
        DefaultTableModel hModel = new DefaultTableModel(cols, 0);
        CustomTable hTable = new CustomTable(hModel);
        JScrollPane scroll = CustomTable.createScrollPane(hTable);

        List<Reservation> history = guestService.getGuestReservationHistory(g.getGuestId());
        for (Reservation r : history) {
            hModel.addRow(new Object[]{
                    "#" + r.getReservationId(),
                    r.getRoomNumber(),
                    r.getRoomType(),
                    DateUtil.formatDate(r.getCheckInDate()),
                    DateUtil.formatDate(r.getCheckOutDate()),
                    r.getNumberOfGuests(),
                    r.getStatus().getDisplayName(),
                    r.getSpecialRequests() != null ? r.getSpecialRequests() : "-"
            });
        }

        dialog.add(topInfo, BorderLayout.NORTH);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void deleteSelectedGuest() {
        Guest g = getSelectedGuest();
        if (g == null) return;

        boolean confirm = DialogUtil.confirm(this,
                "Are you sure you want to delete guest record: " + g.getFullName() + "?",
                "Confirm Guest Deletion");
        if (confirm) {
            try {
                guestService.deleteGuest(g.getGuestId());
                DialogUtil.showSuccess(this, "Guest record removed.");
                refreshGuests();
            } catch (Exception ex) {
                DialogUtil.showError(this, ex.getMessage());
            }
        }
    }
}
