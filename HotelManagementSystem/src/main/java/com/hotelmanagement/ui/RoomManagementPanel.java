package com.hotelmanagement.ui;

import com.hotelmanagement.model.Room;
import com.hotelmanagement.model.RoomStatus;
import com.hotelmanagement.model.RoomType;
import com.hotelmanagement.model.User;
import com.hotelmanagement.service.RoomService;
import com.hotelmanagement.ui.components.CustomTable;
import com.hotelmanagement.util.CurrencyUtil;
import com.hotelmanagement.util.DialogUtil;
import com.hotelmanagement.util.UITheme;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class RoomManagementPanel extends JPanel {

    private final User currentUser;
    private final RoomService roomService;

    private JTextField searchField;
    private JComboBox<String> typeFilterCombo;
    private JComboBox<String> statusFilterCombo;
    private JSpinner floorSpinner;

    private DefaultTableModel tableModel;
    private CustomTable roomTable;
    private List<Room> currentRoomList;

    public RoomManagementPanel(User currentUser) {
        this.currentUser = currentUser;
        this.roomService = new RoomService();

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshRooms();
    }

    private void initUI() {
        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1));
        titleBlock.setOpaque(false);
        JLabel titleLabel = new JLabel("Room Inventory & Housekeeping");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.PRIMARY_NAVY);

        JLabel subLabel = new JLabel("Manage hotel rooms, inventory statuses, pricing, and live availability");
        subLabel.setFont(UITheme.FONT_BODY);
        subLabel.setForeground(UITheme.TEXT_MUTED);
        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);

        JPanel actionBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtnRow.setOpaque(false);

        JButton checkAvailBtn = new JButton("Check Availability");
        checkAvailBtn.setFont(UITheme.FONT_BODY_BOLD);
        checkAvailBtn.setBackground(UITheme.INFO_BLUE);
        checkAvailBtn.setForeground(Color.WHITE);
        checkAvailBtn.setFocusPainted(false);
        checkAvailBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        checkAvailBtn.addActionListener(e -> showAvailabilityCheckerDialog());

        JButton addRoomBtn = new JButton("+ Add New Room");
        addRoomBtn.setFont(UITheme.FONT_BODY_BOLD);
        addRoomBtn.setBackground(UITheme.PRIMARY_NAVY);
        addRoomBtn.setForeground(Color.WHITE);
        addRoomBtn.setFocusPainted(false);
        addRoomBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addRoomBtn.addActionListener(e -> showAddRoomDialog());

        actionBtnRow.add(checkAvailBtn);
        actionBtnRow.add(addRoomBtn);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(actionBtnRow, BorderLayout.EAST);

        // Center Panel: Filter Bar + Table + Action Bar
        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.setOpaque(false);

        // Filter Bar
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        filterCard.setBackground(UITheme.CARD_BG);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        searchField = new JTextField(10);
        searchField.setPreferredSize(new Dimension(130, 32));
        searchField.setBorder(UITheme.BORDER_INPUT);

        typeFilterCombo = new JComboBox<>(new String[]{"All Types", "Single", "Double", "Deluxe", "Suite", "Executive"});
        typeFilterCombo.setPreferredSize(new Dimension(130, 32));

        statusFilterCombo = new JComboBox<>(new String[]{"All Statuses", "Available", "Reserved", "Occupied", "Cleaning", "Maintenance"});
        statusFilterCombo.setPreferredSize(new Dimension(140, 32));

        floorSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));
        floorSpinner.setPreferredSize(new Dimension(60, 32));

        JButton searchBtn = new JButton("Filter");
        searchBtn.setFont(UITheme.FONT_BODY_BOLD);
        searchBtn.setBackground(UITheme.PRIMARY_NAVY);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.addActionListener(e -> applyFilter());

        JButton resetBtn = new JButton("Reset");
        resetBtn.setFont(UITheme.FONT_BODY);
        resetBtn.setBackground(Color.WHITE);
        resetBtn.setForeground(UITheme.TEXT_DARK);
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            typeFilterCombo.setSelectedIndex(0);
            statusFilterCombo.setSelectedIndex(0);
            floorSpinner.setValue(0);
            refreshRooms();
        });

        filterCard.add(new JLabel("Room # / Keyword:"));
        filterCard.add(searchField);
        filterCard.add(new JLabel("Type:"));
        filterCard.add(typeFilterCombo);
        filterCard.add(new JLabel("Status:"));
        filterCard.add(statusFilterCombo);
        filterCard.add(new JLabel("Floor (0=All):"));
        filterCard.add(floorSpinner);
        filterCard.add(searchBtn);
        filterCard.add(resetBtn);

        // Table
        String[] columns = {"ID", "Room #", "Type", "Floor", "Price/Night", "Capacity", "Status", "Description"};
        tableModel = new DefaultTableModel(columns, 0);
        roomTable = new CustomTable(tableModel);
        JScrollPane tableScroll = CustomTable.createScrollPane(roomTable);

        // Table Action Controls (Bottom Bar)
        JPanel bottomActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottomActionPanel.setBackground(UITheme.CARD_BG);
        bottomActionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1),
                new EmptyBorder(6, 12, 6, 12)
        ));

        JButton editBtn = new JButton("Edit Room Details");
        editBtn.setFont(UITheme.FONT_BODY);
        editBtn.setBackground(Color.WHITE);
        editBtn.addActionListener(e -> editSelectedRoom());

        JButton statusChangeBtn = new JButton("Update Status");
        statusChangeBtn.setFont(UITheme.FONT_BODY);
        statusChangeBtn.setBackground(Color.WHITE);
        statusChangeBtn.addActionListener(e -> changeStatusSelectedRoom());

        JButton deleteBtn = new JButton("Delete Room");
        deleteBtn.setFont(UITheme.FONT_BODY);
        deleteBtn.setBackground(new Color(254, 242, 242));
        deleteBtn.setForeground(UITheme.DANGER_RED);
        deleteBtn.addActionListener(e -> deleteSelectedRoom());

        bottomActionPanel.add(new JLabel("Selected Room Actions: "));
        bottomActionPanel.add(editBtn);
        bottomActionPanel.add(statusChangeBtn);
        bottomActionPanel.add(deleteBtn);

        centerPanel.add(filterCard, BorderLayout.NORTH);
        centerPanel.add(tableScroll, BorderLayout.CENTER);
        centerPanel.add(bottomActionPanel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void refreshRooms() {
        applyFilter();
    }

    private void applyFilter() {
        String kw = searchField.getText().trim();
        String type = typeFilterCombo.getSelectedItem().toString();
        if (type.startsWith("All")) type = "All";
        String status = statusFilterCombo.getSelectedItem().toString();
        if (status.startsWith("All")) status = "All";
        int floor = (Integer) floorSpinner.getValue();

        try {
            currentRoomList = roomService.searchRooms(kw, type, status, floor);
            tableModel.setRowCount(0);
            for (Room r : currentRoomList) {
                tableModel.addRow(new Object[]{
                        r.getRoomId(),
                        r.getRoomNumber(),
                        r.getRoomType().name(),
                        r.getFloor(),
                        CurrencyUtil.format(r.getPricePerNight()),
                        r.getCapacity() + " Guest(s)",
                        r.getStatus().name(),
                        r.getDescription() != null ? r.getDescription() : "-"
                });
            }
        } catch (Exception ex) {
            DialogUtil.showError(this, "Failed to load rooms: " + ex.getMessage());
        }
    }

    private Room getSelectedRoom() {
        int row = roomTable.getSelectedRow();
        if (row < 0 || currentRoomList == null || row >= currentRoomList.size()) {
            DialogUtil.showWarning(this, "Please select a room from the table.");
            return null;
        }
        return currentRoomList.get(row);
    }

    private void showAddRoomDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Room", true);
        dialog.setSize(420, 460);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField numField = new JTextField();
        JComboBox<RoomType> typeCombo = new JComboBox<>(RoomType.values());
        JSpinner floorSpin = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        JTextField priceField = new JTextField("120.00");
        JSpinner capSpin = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
        JTextField descField = new JTextField();

        form.add(new JLabel("Room Number:"));
        form.add(numField);
        form.add(new JLabel("Room Type:"));
        form.add(typeCombo);
        form.add(new JLabel("Floor:"));
        form.add(floorSpin);
        form.add(new JLabel("Price / Night ($):"));
        form.add(priceField);
        form.add(new JLabel("Capacity (Guests):"));
        form.add(capSpin);
        form.add(new JLabel("Description / View:"));
        form.add(descField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save Room");
        saveBtn.setBackground(UITheme.PRIMARY_NAVY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            try {
                Room r = new Room();
                r.setRoomNumber(numField.getText().trim());
                r.setRoomType((RoomType) typeCombo.getSelectedItem());
                r.setFloor((Integer) floorSpin.getValue());
                r.setPricePerNight(new BigDecimal(priceField.getText().trim()));
                r.setCapacity((Integer) capSpin.getValue());
                r.setDescription(descField.getText().trim());
                r.setStatus(RoomStatus.Available);

                roomService.addRoom(r);
                DialogUtil.showSuccess(dialog, "Room " + r.getRoomNumber() + " added successfully!");
                dialog.dispose();
                refreshRooms();
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

    private void editSelectedRoom() {
        Room r = getSelectedRoom();
        if (r == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Room " + r.getRoomNumber(), true);
        dialog.setSize(420, 460);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField numField = new JTextField(r.getRoomNumber());
        JComboBox<RoomType> typeCombo = new JComboBox<>(RoomType.values());
        typeCombo.setSelectedItem(r.getRoomType());
        JSpinner floorSpin = new JSpinner(new SpinnerNumberModel((int) r.getFloor(), 1, 50, 1));
        JTextField priceField = new JTextField(r.getPricePerNight().toString());
        JSpinner capSpin = new JSpinner(new SpinnerNumberModel((int) r.getCapacity(), 1, 10, 1));
        JTextField descField = new JTextField(r.getDescription() != null ? r.getDescription() : "");

        form.add(new JLabel("Room Number:"));
        form.add(numField);
        form.add(new JLabel("Room Type:"));
        form.add(typeCombo);
        form.add(new JLabel("Floor:"));
        form.add(floorSpin);
        form.add(new JLabel("Price / Night ($):"));
        form.add(priceField);
        form.add(new JLabel("Capacity (Guests):"));
        form.add(capSpin);
        form.add(new JLabel("Description / View:"));
        form.add(descField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Update Room");
        saveBtn.setBackground(UITheme.PRIMARY_NAVY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            try {
                r.setRoomNumber(numField.getText().trim());
                r.setRoomType((RoomType) typeCombo.getSelectedItem());
                r.setFloor((Integer) floorSpin.getValue());
                r.setPricePerNight(new BigDecimal(priceField.getText().trim()));
                r.setCapacity((Integer) capSpin.getValue());
                r.setDescription(descField.getText().trim());

                roomService.updateRoom(r);
                DialogUtil.showSuccess(dialog, "Room updated successfully!");
                dialog.dispose();
                refreshRooms();
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

    private void changeStatusSelectedRoom() {
        Room r = getSelectedRoom();
        if (r == null) return;

        RoomStatus current = r.getStatus();
        RoomStatus newStatus = (RoomStatus) JOptionPane.showInputDialog(
                this,
                "Select new operational status for Room " + r.getRoomNumber() + ":",
                "Update Room Status",
                JOptionPane.QUESTION_MESSAGE,
                null,
                RoomStatus.values(),
                current
        );

        if (newStatus != null && newStatus != current) {
            try {
                roomService.updateRoomStatus(r.getRoomId(), newStatus);
                DialogUtil.showSuccess(this, "Room " + r.getRoomNumber() + " marked as " + newStatus.name());
                refreshRooms();
            } catch (Exception ex) {
                DialogUtil.showError(this, ex.getMessage());
            }
        }
    }

    private void deleteSelectedRoom() {
        Room r = getSelectedRoom();
        if (r == null) return;

        boolean confirm = DialogUtil.confirm(this,
                "Are you sure you want to delete Room " + r.getRoomNumber() + "?",
                "Confirm Room Deletion");
        if (confirm) {
            try {
                roomService.deleteRoom(r.getRoomId());
                DialogUtil.showSuccess(this, "Room " + r.getRoomNumber() + " removed.");
                refreshRooms();
            } catch (Exception ex) {
                DialogUtil.showError(this, ex.getMessage());
            }
        }
    }

    private void showAvailabilityCheckerDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Live Room Availability Checker", true);
        dialog.setSize(680, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel topForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topForm.setBorder(new EmptyBorder(10, 10, 0, 10));

        JTextField checkInField = new JTextField(LocalDate.now().toString(), 8);
        JTextField checkOutField = new JTextField(LocalDate.now().plusDays(2).toString(), 8);
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Any Type", "Single", "Double", "Deluxe", "Suite", "Executive"});
        JSpinner guestSpin = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
        guestSpin.setPreferredSize(new Dimension(50, 26));

        JButton findBtn = new JButton("Search Available");
        findBtn.setBackground(UITheme.PRIMARY_NAVY);
        findBtn.setForeground(Color.WHITE);

        topForm.add(new JLabel("Check-in:"));
        topForm.add(checkInField);
        topForm.add(new JLabel("Check-out:"));
        topForm.add(checkOutField);
        topForm.add(new JLabel("Type:"));
        topForm.add(typeCombo);
        topForm.add(new JLabel("Guests:"));
        topForm.add(guestSpin);
        topForm.add(findBtn);

        String[] cols = {"Room #", "Type", "Floor", "Price/Night", "Max Capacity", "Status"};
        DefaultTableModel availModel = new DefaultTableModel(cols, 0);
        CustomTable availTable = new CustomTable(availModel);
        JScrollPane scroll = CustomTable.createScrollPane(availTable);

        findBtn.addActionListener(e -> {
            try {
                LocalDate cin = LocalDate.parse(checkInField.getText().trim());
                LocalDate cout = LocalDate.parse(checkOutField.getText().trim());
                String tStr = (String) typeCombo.getSelectedItem();
                RoomType rtype = tStr.startsWith("Any") ? null : RoomType.valueOf(tStr);
                int guests = (Integer) guestSpin.getValue();

                List<Room> available = roomService.findAvailableRooms(cin, cout, rtype, guests, null);
                availModel.setRowCount(0);
                for (Room rm : available) {
                    availModel.addRow(new Object[]{
                            rm.getRoomNumber(),
                            rm.getRoomType(),
                            rm.getFloor(),
                            CurrencyUtil.format(rm.getPricePerNight()),
                            rm.getCapacity() + " Guest(s)",
                            rm.getStatus()
                    });
                }
                if (available.isEmpty()) {
                    DialogUtil.showInfo(dialog, "No rooms match these dates and criteria.");
                }
            } catch (Exception ex) {
                DialogUtil.showError(dialog, "Error checking availability: " + ex.getMessage());
            }
        });

        // Trigger initial search
        findBtn.doClick();

        dialog.add(topForm, BorderLayout.NORTH);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
}
