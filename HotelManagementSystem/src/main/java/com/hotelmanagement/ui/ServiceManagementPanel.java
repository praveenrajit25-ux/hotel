package com.hotelmanagement.ui;

import com.hotelmanagement.model.GuestService;
import com.hotelmanagement.model.Reservation;
import com.hotelmanagement.model.ReservationStatus;
import com.hotelmanagement.model.Service;
import com.hotelmanagement.model.User;
import com.hotelmanagement.service.ReservationService;
import com.hotelmanagement.service.ServiceManagementService;
import com.hotelmanagement.ui.components.CustomTable;
import com.hotelmanagement.util.CurrencyUtil;
import com.hotelmanagement.util.DateUtil;
import com.hotelmanagement.util.DialogUtil;
import com.hotelmanagement.util.UITheme;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class ServiceManagementPanel extends JPanel {

    private final User currentUser;
    private final ServiceManagementService serviceManagementService;
    private final ReservationService reservationService;

    private DefaultTableModel serviceCatalogModel;
    private CustomTable serviceCatalogTable;
    private List<Service> currentServices;

    private DefaultTableModel guestServicesModel;
    private CustomTable guestServicesTable;
    private List<GuestService> currentGuestServices;

    public ServiceManagementPanel(User currentUser) {
        this.currentUser = currentUser;
        this.serviceManagementService = new ServiceManagementService();
        this.reservationService = new ReservationService();

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshAll();
    }

    private void initUI() {
        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1));
        titleBlock.setOpaque(false);
        JLabel titleLabel = new JLabel("Hotel Services & Guest Add-on Charges");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.PRIMARY_NAVY);

        JLabel subLabel = new JLabel("Manage restaurant dining, spa, laundry, airport shuttle, and bill add-on services directly to guest rooms");
        subLabel.setFont(UITheme.FONT_BODY);
        subLabel.setForeground(UITheme.TEXT_MUTED);
        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        JButton addCatalogBtn = new JButton("+ Add Service to Catalog");
        addCatalogBtn.setFont(UITheme.FONT_BODY);
        addCatalogBtn.setBackground(Color.WHITE);
        addCatalogBtn.addActionListener(e -> showAddServiceCatalogDialog());

        JButton chargeRoomBtn = new JButton("Charge Service to Guest Room");
        chargeRoomBtn.setFont(UITheme.FONT_BODY_BOLD);
        chargeRoomBtn.setBackground(UITheme.PRIMARY_NAVY);
        chargeRoomBtn.setForeground(Color.WHITE);
        chargeRoomBtn.addActionListener(e -> showChargeRoomDialog());

        btnRow.add(addCatalogBtn);
        btnRow.add(chargeRoomBtn);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(btnRow, BorderLayout.EAST);

        // Center Split Content: Left (Service Catalogue), Right (Active Guest Service Charges)
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        contentPanel.setOpaque(false);

        // 1. Service Catalogue
        JPanel catalogCard = new JPanel(new BorderLayout(8, 8));
        catalogCard.setBackground(UITheme.CARD_BG);
        catalogCard.setBorder(UITheme.BORDER_PANEL);

        JLabel catalogTitle = new JLabel("AVAILABLE HOTEL SERVICES CATALOG");
        catalogTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        catalogTitle.setForeground(UITheme.TEXT_MUTED);

        String[] catCols = {"ID", "Service Name", "Category", "Unit Price", "Status"};
        serviceCatalogModel = new DefaultTableModel(catCols, 0);
        serviceCatalogTable = new CustomTable(serviceCatalogModel);
        JScrollPane catScroll = CustomTable.createScrollPane(serviceCatalogTable);

        JPanel catActionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        catActionRow.setOpaque(false);
        JButton editCatBtn = new JButton("Edit Service");
        editCatBtn.addActionListener(e -> editSelectedCatalogService());
        JButton deleteCatBtn = new JButton("Delete");
        deleteCatBtn.setForeground(UITheme.DANGER_RED);
        deleteCatBtn.addActionListener(e -> deleteSelectedCatalogService());
        catActionRow.add(editCatBtn);
        catActionRow.add(deleteCatBtn);

        catalogCard.add(catalogTitle, BorderLayout.NORTH);
        catalogCard.add(catScroll, BorderLayout.CENTER);
        catalogCard.add(catActionRow, BorderLayout.SOUTH);

        // 2. Charged Guest Services Log
        JPanel chargedCard = new JPanel(new BorderLayout(8, 8));
        chargedCard.setBackground(UITheme.CARD_BG);
        chargedCard.setBorder(UITheme.BORDER_PANEL);

        JLabel chargedTitle = new JLabel("RECENT CHARGED ROOM SERVICES LOG");
        chargedTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chargedTitle.setForeground(UITheme.TEXT_MUTED);

        String[] chgCols = {"Charge ID", "Res ID", "Guest", "Room #", "Service", "Qty", "Total ($)", "Timestamp"};
        guestServicesModel = new DefaultTableModel(chgCols, 0);
        guestServicesTable = new CustomTable(guestServicesModel);
        JScrollPane chgScroll = CustomTable.createScrollPane(guestServicesTable);

        JPanel chgActionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        chgActionRow.setOpaque(false);
        JButton voidChargeBtn = new JButton("Void / Remove Charge");
        voidChargeBtn.setForeground(UITheme.DANGER_RED);
        voidChargeBtn.addActionListener(e -> voidSelectedGuestCharge());
        chgActionRow.add(voidChargeBtn);

        chargedCard.add(chargedTitle, BorderLayout.NORTH);
        chargedCard.add(chgScroll, BorderLayout.CENTER);
        chargedCard.add(chgActionRow, BorderLayout.SOUTH);

        contentPanel.add(catalogCard);
        contentPanel.add(chargedCard);

        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    public void refreshAll() {
        SwingUtilities.invokeLater(() -> {
            try {
                // 1. Load Catalog
                currentServices = serviceManagementService.getAllServices();
                serviceCatalogModel.setRowCount(0);
                for (Service s : currentServices) {
                    serviceCatalogModel.addRow(new Object[]{
                            s.getServiceId(),
                            s.getServiceName(),
                            s.getCategory(),
                            CurrencyUtil.format(s.getPrice()),
                            s.getStatus()
                    });
                }

                // 2. Load Charged Services
                currentGuestServices = serviceManagementService.getAllGuestServices();
                guestServicesModel.setRowCount(0);
                for (GuestService gs : currentGuestServices) {
                    guestServicesModel.addRow(new Object[]{
                            "GS-" + gs.getGuestServiceId(),
                            "#" + gs.getReservationId(),
                            gs.getGuestName() != null ? gs.getGuestName() : "-",
                            gs.getRoomNumber() != null ? gs.getRoomNumber() : "-",
                            gs.getServiceName(),
                            gs.getQuantity(),
                            CurrencyUtil.format(gs.getTotalPrice()),
                            DateUtil.formatDateTime(gs.getServiceDate())
                    });
                }
            } catch (Exception ex) {
                DialogUtil.showError(this, "Failed to load services data: " + ex.getMessage());
            }
        });
    }

    private void showAddServiceCatalogDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Service to Catalog", true);
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField nameField = new JTextField();
        JComboBox<String> catCombo = new JComboBox<>(new String[]{"Dining", "Laundry", "Spa & Wellness", "Transportation", "Mini Bar", "Housekeeping"});
        JTextField priceField = new JTextField("25.00");
        JTextField descField = new JTextField();
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active", "Inactive"});

        form.add(new JLabel("Service Name *:")); form.add(nameField);
        form.add(new JLabel("Category *:")); form.add(catCombo);
        form.add(new JLabel("Price ($) *:")); form.add(priceField);
        form.add(new JLabel("Description:")); form.add(descField);
        form.add(new JLabel("Status:")); form.add(statusCombo);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save Service");
        saveBtn.setBackground(UITheme.PRIMARY_NAVY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            try {
                Service s = new Service();
                s.setServiceName(nameField.getText().trim());
                s.setCategory((String) catCombo.getSelectedItem());
                s.setPrice(new BigDecimal(priceField.getText().trim()));
                s.setDescription(descField.getText().trim());
                s.setStatus((String) statusCombo.getSelectedItem());

                serviceManagementService.addService(s);
                DialogUtil.showSuccess(dialog, "Service added to catalog!");
                dialog.dispose();
                refreshAll();
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

    private void editSelectedCatalogService() {
        int row = serviceCatalogTable.getSelectedRow();
        if (row < 0 || currentServices == null || row >= currentServices.size()) {
            DialogUtil.showWarning(this, "Please select a service from the left catalog table.");
            return;
        }

        Service s = currentServices.get(row);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Service: " + s.getServiceName(), true);
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField nameField = new JTextField(s.getServiceName());
        JComboBox<String> catCombo = new JComboBox<>(new String[]{"Dining", "Laundry", "Spa & Wellness", "Transportation", "Mini Bar", "Housekeeping"});
        catCombo.setSelectedItem(s.getCategory());
        JTextField priceField = new JTextField(s.getPrice().toString());
        JTextField descField = new JTextField(s.getDescription() != null ? s.getDescription() : "");
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active", "Inactive"});
        statusCombo.setSelectedItem(s.getStatus());

        form.add(new JLabel("Service Name *:")); form.add(nameField);
        form.add(new JLabel("Category *:")); form.add(catCombo);
        form.add(new JLabel("Price ($) *:")); form.add(priceField);
        form.add(new JLabel("Description:")); form.add(descField);
        form.add(new JLabel("Status:")); form.add(statusCombo);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Update Service");
        saveBtn.setBackground(UITheme.PRIMARY_NAVY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            try {
                s.setServiceName(nameField.getText().trim());
                s.setCategory((String) catCombo.getSelectedItem());
                s.setPrice(new BigDecimal(priceField.getText().trim()));
                s.setDescription(descField.getText().trim());
                s.setStatus((String) statusCombo.getSelectedItem());

                serviceManagementService.updateService(s);
                DialogUtil.showSuccess(dialog, "Service updated!");
                dialog.dispose();
                refreshAll();
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

    private void deleteSelectedCatalogService() {
        int row = serviceCatalogTable.getSelectedRow();
        if (row < 0 || currentServices == null || row >= currentServices.size()) {
            DialogUtil.showWarning(this, "Please select a service to delete.");
            return;
        }

        Service s = currentServices.get(row);
        boolean confirm = DialogUtil.confirm(this, "Delete service '" + s.getServiceName() + "' from catalog?", "Confirm Delete");
        if (confirm) {
            try {
                serviceManagementService.deleteService(s.getServiceId());
                DialogUtil.showSuccess(this, "Service deleted.");
                refreshAll();
            } catch (Exception ex) {
                DialogUtil.showError(this, ex.getMessage());
            }
        }
    }

    private void showChargeRoomDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Charge Service to Guest Room", true);
        dialog.setSize(500, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Get in-house or confirmed reservations
        List<Reservation> resList = reservationService.getAllReservations();
        List<Reservation> activeReservations = new ArrayList<>();
        JComboBox<String> resCombo = new JComboBox<>();
        for (Reservation r : resList) {
            if (r.getStatus() == ReservationStatus.Checked_In || r.getStatus() == ReservationStatus.Confirmed) {
                activeReservations.add(r);
                resCombo.addItem("#" + r.getReservationId() + " - " + r.getGuestName() + " (Room " + r.getRoomNumber() + ")");
            }
        }

        List<Service> activeServices = serviceManagementService.getActiveServices();
        JComboBox<String> serviceCombo = new JComboBox<>();
        for (Service s : activeServices) {
            serviceCombo.addItem(s.getServiceName() + " (" + CurrencyUtil.format(s.getPrice()) + ")");
        }

        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        JTextField notesField = new JTextField("Charged to room");
        JLabel totalLabel = new JLabel("$0.00");
        totalLabel.setFont(UITheme.FONT_BODY_BOLD);
        totalLabel.setForeground(UITheme.ACCENT_GOLD);

        Runnable calcTotal = () -> {
            int sIdx = serviceCombo.getSelectedIndex();
            if (sIdx >= 0 && sIdx < activeServices.size()) {
                Service s = activeServices.get(sIdx);
                int qty = (Integer) qtySpinner.getValue();
                BigDecimal tot = s.getPrice().multiply(BigDecimal.valueOf(qty));
                totalLabel.setText(CurrencyUtil.format(tot));
            }
        };

        serviceCombo.addActionListener(e -> calcTotal.run());
        qtySpinner.addChangeListener(e -> calcTotal.run());

        form.add(new JLabel("Select In-House Guest / Room:")); form.add(resCombo);
        form.add(new JLabel("Select Service:")); form.add(serviceCombo);
        form.add(new JLabel("Quantity:")); form.add(qtySpinner);
        form.add(new JLabel("Notes:")); form.add(notesField);
        form.add(new JLabel("Calculated Total Amount:")); form.add(totalLabel);

        calcTotal.run();

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton chargeBtn = new JButton("Post Charge to Folio");
        chargeBtn.setBackground(UITheme.PRIMARY_NAVY);
        chargeBtn.setForeground(Color.WHITE);
        chargeBtn.addActionListener(e -> {
            try {
                int rIdx = resCombo.getSelectedIndex();
                int sIdx = serviceCombo.getSelectedIndex();
                if (rIdx < 0 || sIdx < 0) throw new IllegalArgumentException("Please select guest and service.");

                Reservation r = activeReservations.get(rIdx);
                Service s = activeServices.get(sIdx);
                int qty = (Integer) qtySpinner.getValue();

                serviceManagementService.chargeServiceToReservation(r.getReservationId(), s.getServiceId(), qty, notesField.getText().trim());

                DialogUtil.showSuccess(dialog, "Posted charge of " + totalLabel.getText() + " to Room " + r.getRoomNumber() + " folio!");
                dialog.dispose();
                refreshAll();
            } catch (Exception ex) {
                DialogUtil.showError(dialog, ex.getMessage());
            }
        });

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(cancelBtn);
        btnPanel.add(chargeBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void voidSelectedGuestCharge() {
        int row = guestServicesTable.getSelectedRow();
        if (row < 0 || currentGuestServices == null || row >= currentGuestServices.size()) {
            DialogUtil.showWarning(this, "Please select a charged service row from the right table.");
            return;
        }

        GuestService gs = currentGuestServices.get(row);
        boolean confirm = DialogUtil.confirm(this, "Void and remove charge GS-" + gs.getGuestServiceId() + " (" + gs.getServiceName() + ")?", "Void Service Charge");
        if (confirm) {
            try {
                serviceManagementService.removeGuestService(gs.getGuestServiceId());
                DialogUtil.showSuccess(this, "Service charge voided from folio.");
                refreshAll();
            } catch (Exception ex) {
                DialogUtil.showError(this, ex.getMessage());
            }
        }
    }
}
