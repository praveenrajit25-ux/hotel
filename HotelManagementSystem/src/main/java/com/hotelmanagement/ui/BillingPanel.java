package com.hotelmanagement.ui;

import com.hotelmanagement.model.*;
import com.hotelmanagement.service.*;
import com.hotelmanagement.ui.components.CustomTable;
import com.hotelmanagement.util.CurrencyUtil;
import com.hotelmanagement.util.DateUtil;
import com.hotelmanagement.util.DialogUtil;
import com.hotelmanagement.util.PDFExporter;
import com.hotelmanagement.util.UITheme;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class BillingPanel extends JPanel {

    private final User currentUser;
    private final BillingService billingService;
    private final PaymentService paymentService;
    private final ReservationService reservationService;
    private final ServiceManagementService serviceManagementService;
    private final HotelSettingsService settingsService;

    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private DefaultTableModel tableModel;
    private CustomTable invoiceTable;
    private List<Invoice> currentInvoiceList;

    public BillingPanel(User currentUser) {
        this.currentUser = currentUser;
        this.billingService = new BillingService();
        this.paymentService = new PaymentService();
        this.reservationService = new ReservationService();
        this.serviceManagementService = new ServiceManagementService();
        this.settingsService = new HotelSettingsService();

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshInvoices();
    }

    private void initUI() {
        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1));
        titleBlock.setOpaque(false);
        JLabel titleLabel = new JLabel("Billing, Invoicing & Payments");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.PRIMARY_NAVY);

        JLabel subLabel = new JLabel("Generate guest folios, apply service fees & discounts, record multi-method payments, and export receipts");
        subLabel.setFont(UITheme.FONT_BODY);
        subLabel.setForeground(UITheme.TEXT_MUTED);
        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);

        JButton createInvoiceBtn = new JButton("+ Generate New Invoice");
        createInvoiceBtn.setFont(UITheme.FONT_BODY_BOLD);
        createInvoiceBtn.setBackground(UITheme.PRIMARY_NAVY);
        createInvoiceBtn.setForeground(Color.WHITE);
        createInvoiceBtn.setFocusPainted(false);
        createInvoiceBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createInvoiceBtn.addActionListener(e -> showGenerateInvoiceDialog());

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(createInvoiceBtn, BorderLayout.EAST);

        // Center Area: Filter Bar + Invoices Table + Action Controls
        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.setOpaque(false);

        // Filter Bar
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterCard.setBackground(UITheme.CARD_BG);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        searchField = new JTextField(15);
        searchField.setPreferredSize(new Dimension(180, 32));
        searchField.setBorder(UITheme.BORDER_INPUT);

        statusFilterCombo = new JComboBox<>(new String[]{"All Statuses", "Paid", "Partially Paid", "Pending"});
        statusFilterCombo.setPreferredSize(new Dimension(140, 32));

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
            refreshInvoices();
        });

        filterCard.add(new JLabel("Search (Invoice # / Guest / Room / Res ID):"));
        filterCard.add(searchField);
        filterCard.add(new JLabel("Payment Status:"));
        filterCard.add(statusFilterCombo);
        filterCard.add(searchBtn);
        filterCard.add(resetBtn);

        // Table
        String[] columns = {"Invoice #", "Res ID", "Guest Name", "Room #", "Room Chg", "Services", "Discount", "Tax", "Grand Total", "Paid", "Balance Due", "Status", "Date"};
        tableModel = new DefaultTableModel(columns, 0);
        invoiceTable = new CustomTable(tableModel);
        JScrollPane tableScroll = CustomTable.createScrollPane(invoiceTable);

        // Bottom Action Bar
        JPanel bottomActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottomActionPanel.setBackground(UITheme.CARD_BG);
        bottomActionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1),
                new EmptyBorder(6, 12, 6, 12)
        ));

        JButton viewInvoiceBtn = new JButton("View Folio & Export Receipt");
        viewInvoiceBtn.setFont(UITheme.FONT_BODY_BOLD);
        viewInvoiceBtn.setBackground(UITheme.INFO_BLUE);
        viewInvoiceBtn.setForeground(Color.WHITE);
        viewInvoiceBtn.addActionListener(e -> viewSelectedInvoiceDetails());

        JButton settlePaymentBtn = new JButton("Record Payment");
        settlePaymentBtn.setFont(UITheme.FONT_BODY_BOLD);
        settlePaymentBtn.setBackground(UITheme.SUCCESS_GREEN);
        settlePaymentBtn.setForeground(Color.WHITE);
        settlePaymentBtn.addActionListener(e -> recordPaymentForSelected());

        bottomActionPanel.add(new JLabel("Selected Folio Actions: "));
        bottomActionPanel.add(viewInvoiceBtn);
        bottomActionPanel.add(settlePaymentBtn);

        centerPanel.add(filterCard, BorderLayout.NORTH);
        centerPanel.add(tableScroll, BorderLayout.CENTER);
        centerPanel.add(bottomActionPanel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void refreshInvoices() {
        applyFilter();
    }

    private void applyFilter() {
        String kw = searchField.getText().trim();
        String st = statusFilterCombo.getSelectedItem().toString();
        if (st.startsWith("All")) st = "All";

        try {
            currentInvoiceList = billingService.searchInvoices(kw, st, null, null);
            tableModel.setRowCount(0);
            for (Invoice inv : currentInvoiceList) {
                tableModel.addRow(new Object[]{
                        inv.getInvoiceNumber(),
                        "#" + inv.getReservationId(),
                        inv.getGuestName(),
                        inv.getRoomNumber(),
                        CurrencyUtil.format(inv.getRoomCharges()),
                        CurrencyUtil.format(inv.getServiceCharges()),
                        CurrencyUtil.format(inv.getDiscount()),
                        CurrencyUtil.format(inv.getTaxAmount()),
                        CurrencyUtil.format(inv.getGrandTotal()),
                        CurrencyUtil.format(inv.getPaidAmount()),
                        CurrencyUtil.format(inv.getBalanceAmount()),
                        inv.getPaymentStatus().getDisplayName(),
                        DateUtil.formatDate(inv.getInvoiceDate())
                });
            }
        } catch (Exception ex) {
            DialogUtil.showError(this, "Failed to search invoices: " + ex.getMessage());
        }
    }

    private Invoice getSelectedInvoice() {
        int row = invoiceTable.getSelectedRow();
        if (row < 0 || currentInvoiceList == null || row >= currentInvoiceList.size()) {
            DialogUtil.showWarning(this, "Please select an invoice from the table.");
            return null;
        }
        return currentInvoiceList.get(row);
    }

    private void showGenerateInvoiceDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Generate Guest Invoice / Folio", true);
        dialog.setSize(540, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        List<Reservation> resList = reservationService.getAllReservations();
        JComboBox<ResComboItem> resCombo = new JComboBox<>();
        for (Reservation r : resList) {
            if (r.getStatus() != ReservationStatus.Cancelled) {
                resCombo.addItem(new ResComboItem(r));
            }
        }

        JTextField discountField = new JTextField("0.00");
        JTextField addChargesField = new JTextField("0.00");
        JComboBox<PaymentMethod> methodCombo = new JComboBox<>(PaymentMethod.values());

        JLabel roomChargeLbl = new JLabel("$0.00");
        JLabel serviceChargeLbl = new JLabel("$0.00");
        JLabel grandTotalLbl = new JLabel("$0.00");
        grandTotalLbl.setFont(UITheme.FONT_BODY_BOLD);
        grandTotalLbl.setForeground(UITheme.ACCENT_GOLD);

        Runnable updatePreview = () -> {
            ResComboItem item = (ResComboItem) resCombo.getSelectedItem();
            if (item != null) {
                try {
                    BigDecimal disc = new BigDecimal(discountField.getText().trim());
                    BigDecimal addc = new BigDecimal(addChargesField.getText().trim());
                    PaymentMethod meth = (PaymentMethod) methodCombo.getSelectedItem();
                    Invoice preview = billingService.calculateInvoicePreview(item.res.getReservationId(), disc, addc, meth);

                    roomChargeLbl.setText(CurrencyUtil.format(preview.getRoomCharges()) + " (" + preview.getNumberOfNights() + " nights)");
                    serviceChargeLbl.setText(CurrencyUtil.format(preview.getServiceCharges()));
                    grandTotalLbl.setText(CurrencyUtil.format(preview.getGrandTotal()) + " (Tax: " + CurrencyUtil.format(preview.getTaxAmount()) + ")");
                } catch (Exception ignored) {}
            }
        };

        resCombo.addActionListener(e -> updatePreview.run());

        form.add(new JLabel("Select Reservation *:")); form.add(resCombo);
        form.add(new JLabel("Calculated Room Charges:")); form.add(roomChargeLbl);
        form.add(new JLabel("Room Services Charges:")); form.add(serviceChargeLbl);
        form.add(new JLabel("Discount Amount ($):")); form.add(discountField);
        form.add(new JLabel("Additional Charges ($):")); form.add(addChargesField);
        form.add(new JLabel("Est. Grand Total (Inc. Tax):")); form.add(grandTotalLbl);

        updatePreview.run();

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton calcBtn = new JButton("Recalculate");
        calcBtn.addActionListener(e -> updatePreview.run());

        JButton genBtn = new JButton("Save & Issue Invoice");
        genBtn.setBackground(UITheme.PRIMARY_NAVY);
        genBtn.setForeground(Color.WHITE);
        genBtn.addActionListener(e -> {
            try {
                ResComboItem item = (ResComboItem) resCombo.getSelectedItem();
                if (item == null) throw new IllegalArgumentException("Please select a reservation.");

                BigDecimal disc = new BigDecimal(discountField.getText().trim());
                BigDecimal addc = new BigDecimal(addChargesField.getText().trim());
                PaymentMethod meth = (PaymentMethod) methodCombo.getSelectedItem();

                Invoice inv = billingService.generateOrUpdateInvoice(item.res.getReservationId(), disc, addc, meth);
                DialogUtil.showSuccess(dialog, "Invoice " + inv.getInvoiceNumber() + " generated with Grand Total: " + CurrencyUtil.format(inv.getGrandTotal()));
                dialog.dispose();
                refreshInvoices();
            } catch (Exception ex) {
                DialogUtil.showError(dialog, ex.getMessage());
            }
        });

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(calcBtn);
        btnPanel.add(cancelBtn);
        btnPanel.add(genBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void recordPaymentForSelected() {
        Invoice inv = getSelectedInvoice();
        if (inv == null) return;

        if (inv.getBalanceAmount() != null && inv.getBalanceAmount().compareTo(BigDecimal.ZERO) <= 0) {
            DialogUtil.showInfo(this, "Invoice " + inv.getInvoiceNumber() + " is already fully paid.");
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Record Payment: " + inv.getInvoiceNumber(), true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel guestLbl = new JLabel(inv.getGuestName() + " (Room " + inv.getRoomNumber() + ")");
        JLabel grandLbl = new JLabel(CurrencyUtil.format(inv.getGrandTotal()));
        JLabel balanceLbl = new JLabel(CurrencyUtil.format(inv.getBalanceAmount()));
        balanceLbl.setFont(UITheme.FONT_BODY_BOLD);
        balanceLbl.setForeground(UITheme.DANGER_RED);

        JTextField payAmountField = new JTextField(inv.getBalanceAmount().toString());
        JComboBox<PaymentMethod> methodCombo = new JComboBox<>(PaymentMethod.values());
        JTextField refField = new JTextField("TXN-" + System.currentTimeMillis());

        form.add(new JLabel("Guest / Room:")); form.add(guestLbl);
        form.add(new JLabel("Grand Total:")); form.add(grandLbl);
        form.add(new JLabel("Outstanding Balance:")); form.add(balanceLbl);
        form.add(new JLabel("Payment Amount ($) *:")); form.add(payAmountField);
        form.add(new JLabel("Payment Method *:")); form.add(methodCombo);
        form.add(new JLabel("Transaction Ref:")); form.add(refField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Process Payment");
        saveBtn.setBackground(UITheme.SUCCESS_GREEN);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            try {
                BigDecimal amount = new BigDecimal(payAmountField.getText().trim());
                PaymentMethod method = (PaymentMethod) methodCombo.getSelectedItem();
                String ref = refField.getText().trim();
                int staffId = currentUser != null ? currentUser.getUserId() : null;

                paymentService.processPayment(inv.getInvoiceId(), amount, method, ref, "Settled at front desk", staffId);

                DialogUtil.showSuccess(dialog, "Payment of " + CurrencyUtil.format(amount) + " recorded successfully!");
                dialog.dispose();
                refreshInvoices();
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

    private void viewSelectedInvoiceDetails() {
        Invoice inv = getSelectedInvoice();
        if (inv == null) return;

        HotelSettings settings = settingsService.getSettings();
        List<GuestService> services = serviceManagementService.getServicesForReservation(inv.getReservationId());
        List<Payment> payments = paymentService.getPaymentsForInvoice(inv.getInvoiceId());

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Guest Folio - " + inv.getInvoiceNumber(), true);
        dialog.setSize(680, 620);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JTextArea folioText = new JTextArea();
        folioText.setFont(new Font("Monospaced", Font.PLAIN, 12));
        folioText.setEditable(false);
        folioText.setBorder(new EmptyBorder(12, 12, 12, 12));

        StringBuilder sb = new StringBuilder();
        sb.append("================================================================================\n");
        sb.append("                         ").append(settings.getHotelName().toUpperCase()).append("\n");
        sb.append("                         ").append(settings.getTagline() != null ? settings.getTagline() : "").append("\n");
        sb.append("================================================================================\n");
        sb.append("INVOICE NUMBER: ").append(inv.getInvoiceNumber()).append("           INVOICE DATE: ").append(DateUtil.formatDate(inv.getInvoiceDate())).append("\n");
        sb.append("GUEST: ").append(inv.getGuestName()).append("                 ROOM: ").append(inv.getRoomNumber()).append(" (").append(inv.getRoomType()).append(")\n");
        sb.append("CHECK-IN: ").append(DateUtil.formatDate(inv.getCheckInDate())).append("    CHECK-OUT: ").append(DateUtil.formatDate(inv.getCheckOutDate())).append("  (").append(inv.getNumberOfNights()).append(" Nights)\n");
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("ITEMIZED CHARGES:\n");
        sb.append(String.format("%-42s %10s %10s %14s\n", "Description", "Qty", "Unit Price", "Total"));
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format("%-42s %10d %10s %14s\n", "Room Stay Charges", inv.getNumberOfNights(),
                CurrencyUtil.format(inv.getRoomCharges().divide(BigDecimal.valueOf(Math.max(1, inv.getNumberOfNights())), 2, java.math.RoundingMode.HALF_UP)),
                CurrencyUtil.format(inv.getRoomCharges())));

        for (GuestService gs : services) {
            sb.append(String.format("%-42s %10d %10s %14s\n", gs.getServiceName(), gs.getQuantity(), CurrencyUtil.format(gs.getUnitPrice()), CurrencyUtil.format(gs.getTotalPrice())));
        }

        if (inv.getAdditionalCharges() != null && inv.getAdditionalCharges().compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-42s %10s %10s %14s\n", "Miscellaneous Surcharge", "-", "-", CurrencyUtil.format(inv.getAdditionalCharges())));
        }

        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format("%-64s %14s\n", "Subtotal:", CurrencyUtil.format(inv.getSubtotal())));
        if (inv.getDiscount() != null && inv.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-64s %14s\n", "Discount:", "-" + CurrencyUtil.format(inv.getDiscount())));
        }
        sb.append(String.format("%-64s %14s\n", "Tax (" + inv.getTaxPercentage() + "%):", CurrencyUtil.format(inv.getTaxAmount())));
        sb.append("================================================================================\n");
        sb.append(String.format("%-64s %14s\n", "GRAND TOTAL:", CurrencyUtil.format(inv.getGrandTotal())));
        sb.append(String.format("%-64s %14s\n", "AMOUNT PAID:", CurrencyUtil.format(inv.getPaidAmount())));
        sb.append(String.format("%-64s %14s\n", "BALANCE DUE:", CurrencyUtil.format(inv.getBalanceAmount())));
        sb.append(String.format("%-64s %14s\n", "PAYMENT STATUS:", inv.getPaymentStatus().getDisplayName().toUpperCase()));
        sb.append("================================================================================\n");

        if (!payments.isEmpty()) {
            sb.append("PAYMENT TRANSACTIONS LOG:\n");
            for (Payment p : payments) {
                sb.append(String.format("  • %s | %s | %s | Ref: %s\n",
                        DateUtil.formatDateTime(p.getPaymentDate()),
                        CurrencyUtil.format(p.getAmount()),
                        p.getPaymentMethod(),
                        p.getTransactionReference()));
            }
            sb.append("================================================================================\n");
        }

        sb.append(settings.getInvoiceFooter() != null ? settings.getInvoiceFooter() : "Thank you for choosing Grand Luxe Hotel!");
        folioText.setText(sb.toString());

        JScrollPane scroll = new JScrollPane(folioText);

        JPanel bottomBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exportBtn = new JButton("Export to Receipt File (.txt)");
        exportBtn.setBackground(UITheme.PRIMARY_NAVY);
        exportBtn.setForeground(Color.WHITE);
        exportBtn.addActionListener(e -> {
            try {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(new File(inv.getInvoiceNumber() + "_Receipt.txt"));
                int userSelection = fileChooser.showSaveDialog(dialog);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    PDFExporter.exportInvoiceToTextFile(inv, settings, services, fileToSave);
                    DialogUtil.showSuccess(dialog, "Invoice exported successfully to:\n" + fileToSave.getAbsolutePath());
                }
            } catch (Exception ex) {
                DialogUtil.showError(dialog, "Failed to export invoice: " + ex.getMessage());
            }
        });

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());

        bottomBtnRow.add(exportBtn);
        bottomBtnRow.add(closeBtn);

        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(bottomBtnRow, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private static class ResComboItem {
        final Reservation res;
        ResComboItem(Reservation r) { this.res = r; }
        @Override
        public String toString() { return "#" + res.getReservationId() + " - " + res.getGuestName() + " (Room " + res.getRoomNumber() + ", " + res.getStatus().getDisplayName() + ")"; }
    }
}
