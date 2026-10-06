package com.hotelmanagement.ui;

import com.hotelmanagement.model.HotelSettings;
import com.hotelmanagement.model.User;
import com.hotelmanagement.model.UserRole;
import com.hotelmanagement.service.HotelSettingsService;
import com.hotelmanagement.util.DialogUtil;
import com.hotelmanagement.util.UITheme;
import java.awt.*;
import java.math.BigDecimal;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class SettingsPanel extends JPanel {

    private final User currentUser;
    private final HotelSettingsService settingsService;

    private JTextField hotelNameField;
    private JTextField taglineField;
    private JTextField addressField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField websiteField;
    private JTextField taxField;
    private JTextField currencyField;
    private JTextField checkInTimeField;
    private JTextField checkOutTimeField;
    private JTextField invoiceFooterField;

    public SettingsPanel(User currentUser) {
        this.currentUser = currentUser;
        this.settingsService = new HotelSettingsService();

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        loadSettings();
    }

    private void initUI() {
        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1));
        titleBlock.setOpaque(false);
        JLabel titleLabel = new JLabel("Hotel Configuration & System Settings");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.PRIMARY_NAVY);

        JLabel subLabel = new JLabel("Configure property information, tax rates, standard check-in/out policies, and invoice branding");
        subLabel.setFont(UITheme.FONT_BODY);
        subLabel.setForeground(UITheme.TEXT_MUTED);
        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);

        JButton saveBtn = new JButton("Save Property Settings");
        saveBtn.setFont(UITheme.FONT_BODY_BOLD);
        saveBtn.setBackground(UITheme.PRIMARY_NAVY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> saveSettings());

        headerPanel.add(titleBlock, BorderLayout.WEST);
        if (currentUser.getRole() == UserRole.Admin || currentUser.getRole() == UserRole.Manager) {
            headerPanel.add(saveBtn, BorderLayout.EAST);
        }

        // Center Content (Form Card)
        JPanel contentCard = new JPanel(new GridLayout(11, 2, 12, 10));
        contentCard.setBackground(UITheme.CARD_BG);
        contentCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1),
                new EmptyBorder(24, 28, 24, 28)
        ));

        hotelNameField = new JTextField();
        taglineField = new JTextField();
        addressField = new JTextField();
        phoneField = new JTextField();
        emailField = new JTextField();
        websiteField = new JTextField();
        taxField = new JTextField();
        currencyField = new JTextField();
        checkInTimeField = new JTextField();
        checkOutTimeField = new JTextField();
        invoiceFooterField = new JTextField();

        contentCard.add(new JLabel("Hotel / Property Legal Name *:")); contentCard.add(hotelNameField);
        contentCard.add(new JLabel("Property Tagline / Slogan:")); contentCard.add(taglineField);
        contentCard.add(new JLabel("Street Address & City:")); contentCard.add(addressField);
        contentCard.add(new JLabel("Front Desk Telephone:")); contentCard.add(phoneField);
        contentCard.add(new JLabel("Official Inquiries Email:")); contentCard.add(emailField);
        contentCard.add(new JLabel("Hotel Website Domain:")); contentCard.add(websiteField);
        contentCard.add(new JLabel("Standard Lodging Tax Rate (%):")); contentCard.add(taxField);
        contentCard.add(new JLabel("Currency Symbol:")); contentCard.add(currencyField);
        contentCard.add(new JLabel("Standard Check-In Time (HH:MM):")); contentCard.add(checkInTimeField);
        contentCard.add(new JLabel("Standard Check-Out Time (HH:MM):")); contentCard.add(checkOutTimeField);
        contentCard.add(new JLabel("Printed Invoice Footer Note:")); contentCard.add(invoiceFooterField);

        boolean canEdit = currentUser.getRole() == UserRole.Admin || currentUser.getRole() == UserRole.Manager;
        hotelNameField.setEditable(canEdit);
        taglineField.setEditable(canEdit);
        addressField.setEditable(canEdit);
        phoneField.setEditable(canEdit);
        emailField.setEditable(canEdit);
        websiteField.setEditable(canEdit);
        taxField.setEditable(canEdit);
        currencyField.setEditable(canEdit);
        checkInTimeField.setEditable(canEdit);
        checkOutTimeField.setEditable(canEdit);
        invoiceFooterField.setEditable(canEdit);

        JScrollPane scrollPane = new JScrollPane(contentCard);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UITheme.BG_LIGHT);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadSettings() {
        try {
            HotelSettings s = settingsService.getSettings();
            hotelNameField.setText(s.getHotelName());
            taglineField.setText(s.getTagline());
            addressField.setText(s.getAddress());
            phoneField.setText(s.getPhone());
            emailField.setText(s.getEmail());
            websiteField.setText(s.getWebsite());
            taxField.setText(s.getTaxPercentage() != null ? s.getTaxPercentage().toString() : "10.00");
            currencyField.setText(s.getCurrencySymbol() != null ? s.getCurrencySymbol() : "$");
            checkInTimeField.setText(s.getCheckInTime());
            checkOutTimeField.setText(s.getCheckOutTime());
            invoiceFooterField.setText(s.getInvoiceFooter());
        } catch (Exception ex) {
            DialogUtil.showError(this, "Failed to load hotel settings: " + ex.getMessage());
        }
    }

    private void saveSettings() {
        try {
            HotelSettings s = new HotelSettings();
            s.setHotelName(hotelNameField.getText().trim());
            s.setTagline(taglineField.getText().trim());
            s.setAddress(addressField.getText().trim());
            s.setPhone(phoneField.getText().trim());
            s.setEmail(emailField.getText().trim());
            s.setWebsite(websiteField.getText().trim());
            s.setTaxPercentage(new BigDecimal(taxField.getText().trim()));
            s.setCurrencySymbol(currencyField.getText().trim());
            s.setCheckInTime(checkInTimeField.getText().trim());
            s.setCheckOutTime(checkOutTimeField.getText().trim());
            s.setInvoiceFooter(invoiceFooterField.getText().trim());

            settingsService.updateSettings(s);
            DialogUtil.showSuccess(this, "Hotel settings updated successfully!");
        } catch (Exception ex) {
            DialogUtil.showError(this, "Failed to save settings: " + ex.getMessage());
        }
    }
}
