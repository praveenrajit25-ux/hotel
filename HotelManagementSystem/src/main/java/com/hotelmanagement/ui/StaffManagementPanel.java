package com.hotelmanagement.ui;

import com.hotelmanagement.model.Staff;
import com.hotelmanagement.model.User;
import com.hotelmanagement.model.UserRole;
import com.hotelmanagement.service.StaffService;
import com.hotelmanagement.ui.components.CustomTable;
import com.hotelmanagement.util.CurrencyUtil;
import com.hotelmanagement.util.DateUtil;
import com.hotelmanagement.util.DialogUtil;
import com.hotelmanagement.util.UITheme;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class StaffManagementPanel extends JPanel {

    private final User currentUser;
    private final StaffService staffService;

    private JTextField searchField;
    private JComboBox<String> roleFilterCombo;
    private JComboBox<String> statusFilterCombo;

    private DefaultTableModel tableModel;
    private CustomTable staffTable;
    private List<Staff> currentStaffList;

    public StaffManagementPanel(User currentUser) {
        this.currentUser = currentUser;
        this.staffService = new StaffService();

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshStaff();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1));
        titleBlock.setOpaque(false);
        JLabel titleLabel = new JLabel("Staff Directory & Access Control");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.PRIMARY_NAVY);

        JLabel subLabel = new JLabel("Manage employee records, department roles, compensations, and system login credentials");
        subLabel.setFont(UITheme.FONT_BODY);
        subLabel.setForeground(UITheme.TEXT_MUTED);
        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);

        JButton addStaffBtn = new JButton("+ Add New Staff Member");
        addStaffBtn.setFont(UITheme.FONT_BODY_BOLD);
        addStaffBtn.setBackground(UITheme.PRIMARY_NAVY);
        addStaffBtn.setForeground(Color.WHITE);
        addStaffBtn.setFocusPainted(false);
        addStaffBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addStaffBtn.addActionListener(e -> showAddStaffDialog());

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(addStaffBtn, BorderLayout.EAST);

        // Center: Filter + Table + Action Bar
        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.setOpaque(false);

        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterCard.setBackground(UITheme.CARD_BG);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        searchField = new JTextField(15);
        searchField.setPreferredSize(new Dimension(160, 32));
        searchField.setBorder(UITheme.BORDER_INPUT);

        roleFilterCombo = new JComboBox<>(new String[]{"All Roles", "Admin", "Manager", "Receptionist", "Housekeeping", "Chef", "Staff"});
        roleFilterCombo.setPreferredSize(new Dimension(130, 32));

        statusFilterCombo = new JComboBox<>(new String[]{"All Statuses", "Active", "Inactive", "On Leave", "Terminated"});
        statusFilterCombo.setPreferredSize(new Dimension(130, 32));

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
            roleFilterCombo.setSelectedIndex(0);
            statusFilterCombo.setSelectedIndex(0);
            refreshStaff();
        });

        filterCard.add(new JLabel("Search (Name / Code / Email / Phone):"));
        filterCard.add(searchField);
        filterCard.add(new JLabel("Role:"));
        filterCard.add(roleFilterCombo);
        filterCard.add(new JLabel("Status:"));
        filterCard.add(statusFilterCombo);
        filterCard.add(searchBtn);
        filterCard.add(resetBtn);

        // Table
        String[] columns = {"ID", "Code", "Full Name", "Username", "Role", "Phone", "Email", "Salary", "Joining Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        staffTable = new CustomTable(tableModel);
        JScrollPane tableScroll = CustomTable.createScrollPane(staffTable);

        // Action Bar
        JPanel bottomActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottomActionPanel.setBackground(UITheme.CARD_BG);
        bottomActionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1),
                new EmptyBorder(6, 12, 6, 12)
        ));

        JButton editBtn = new JButton("Edit Employee Details");
        editBtn.addActionListener(e -> editSelectedStaff());

        JButton deleteBtn = new JButton("Remove Staff");
        deleteBtn.setForeground(UITheme.DANGER_RED);
        deleteBtn.addActionListener(e -> deleteSelectedStaff());

        bottomActionPanel.add(new JLabel("Staff Actions: "));
        bottomActionPanel.add(editBtn);
        bottomActionPanel.add(deleteBtn);

        centerPanel.add(filterCard, BorderLayout.NORTH);
        centerPanel.add(tableScroll, BorderLayout.CENTER);
        centerPanel.add(bottomActionPanel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void refreshStaff() {
        applyFilter();
    }

    private void applyFilter() {
        String kw = searchField.getText().trim();
        String role = roleFilterCombo.getSelectedItem().toString();
        if (role.startsWith("All")) role = "All";
        String status = statusFilterCombo.getSelectedItem().toString();
        if (status.startsWith("All")) status = "All";

        try {
            currentStaffList = staffService.searchStaff(kw, role, status);
            tableModel.setRowCount(0);
            for (Staff s : currentStaffList) {
                tableModel.addRow(new Object[]{
                        s.getStaffId(),
                        s.getEmployeeCode(),
                        s.getFullName(),
                        s.getUsername() != null ? s.getUsername() : "-",
                        s.getRole().name(),
                        s.getPhone(),
                        s.getEmail(),
                        CurrencyUtil.format(s.getSalary()),
                        DateUtil.formatDate(s.getJoiningDate()),
                        s.getStatus()
                });
            }
        } catch (Exception ex) {
            DialogUtil.showError(this, "Failed to search staff: " + ex.getMessage());
        }
    }

    private Staff getSelectedStaff() {
        int row = staffTable.getSelectedRow();
        if (row < 0 || currentStaffList == null || row >= currentStaffList.size()) {
            DialogUtil.showWarning(this, "Please select an employee from the table.");
            return null;
        }
        return currentStaffList.get(row);
    }

    private void showAddStaffDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Staff Member", true);
        dialog.setSize(520, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(9, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField codeField = new JTextField("EMP-" + (100 + (int)(Math.random() * 900)));
        JTextField nameField = new JTextField();
        JComboBox<UserRole> roleCombo = new JComboBox<>(UserRole.values());
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField salaryField = new JTextField("3500.00");
        JTextField joinDateField = new JTextField(LocalDate.now().toString());
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField("Staff@123");

        form.add(new JLabel("Employee Code *:")); form.add(codeField);
        form.add(new JLabel("Full Name *:")); form.add(nameField);
        form.add(new JLabel("System Role *:")); form.add(roleCombo);
        form.add(new JLabel("Phone Number *:")); form.add(phoneField);
        form.add(new JLabel("Email Address *:")); form.add(emailField);
        form.add(new JLabel("Monthly Salary ($) *:")); form.add(salaryField);
        form.add(new JLabel("Joining Date (YYYY-MM-DD):")); form.add(joinDateField);
        form.add(new JLabel("Login Username *:")); form.add(userField);
        form.add(new JLabel("Initial Password *:")); form.add(passField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Create Staff & Account");
        saveBtn.setBackground(UITheme.PRIMARY_NAVY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            try {
                Staff s = new Staff();
                s.setEmployeeCode(codeField.getText().trim());
                s.setFullName(nameField.getText().trim());
                s.setRole((UserRole) roleCombo.getSelectedItem());
                s.setPhone(phoneField.getText().trim());
                s.setEmail(emailField.getText().trim());
                s.setSalary(new BigDecimal(salaryField.getText().trim()));
                s.setJoiningDate(LocalDate.parse(joinDateField.getText().trim()));
                s.setStatus("Active");

                String uname = userField.getText().trim();
                String pass = new String(passField.getPassword());

                staffService.createStaffWithAccount(s, uname, pass);
                DialogUtil.showSuccess(dialog, "Staff profile and user login created successfully!");
                dialog.dispose();
                refreshStaff();
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

    private void editSelectedStaff() {
        Staff s = getSelectedStaff();
        if (s == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Staff: " + s.getFullName(), true);
        dialog.setSize(500, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField nameField = new JTextField(s.getFullName());
        JComboBox<UserRole> roleCombo = new JComboBox<>(UserRole.values());
        roleCombo.setSelectedItem(s.getRole());
        JTextField phoneField = new JTextField(s.getPhone());
        JTextField emailField = new JTextField(s.getEmail());
        JTextField addressField = new JTextField(s.getAddress() != null ? s.getAddress() : "");
        JTextField salaryField = new JTextField(s.getSalary().toString());
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active", "Inactive", "On Leave", "Terminated"});
        statusCombo.setSelectedItem(s.getStatus());

        form.add(new JLabel("Full Name *:")); form.add(nameField);
        form.add(new JLabel("System Role *:")); form.add(roleCombo);
        form.add(new JLabel("Phone Number *:")); form.add(phoneField);
        form.add(new JLabel("Email Address *:")); form.add(emailField);
        form.add(new JLabel("Address:")); form.add(addressField);
        form.add(new JLabel("Monthly Salary ($) *:")); form.add(salaryField);
        form.add(new JLabel("Status:")); form.add(statusCombo);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Update Employee");
        saveBtn.setBackground(UITheme.PRIMARY_NAVY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            try {
                s.setFullName(nameField.getText().trim());
                s.setRole((UserRole) roleCombo.getSelectedItem());
                s.setPhone(phoneField.getText().trim());
                s.setEmail(emailField.getText().trim());
                s.setAddress(addressField.getText().trim());
                s.setSalary(new BigDecimal(salaryField.getText().trim()));
                s.setStatus((String) statusCombo.getSelectedItem());

                staffService.updateStaff(s);
                DialogUtil.showSuccess(dialog, "Staff details updated!");
                dialog.dispose();
                refreshStaff();
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

    private void deleteSelectedStaff() {
        Staff s = getSelectedStaff();
        if (s == null) return;

        boolean confirm = DialogUtil.confirm(this, "Remove staff member '" + s.getFullName() + "' and revoke login?", "Confirm Staff Deletion");
        if (confirm) {
            try {
                staffService.deleteStaff(s.getStaffId());
                DialogUtil.showSuccess(this, "Staff record deleted.");
                refreshStaff();
            } catch (Exception ex) {
                DialogUtil.showError(this, ex.getMessage());
            }
        }
    }
}
