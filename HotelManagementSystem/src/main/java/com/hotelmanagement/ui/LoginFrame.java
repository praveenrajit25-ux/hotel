package com.hotelmanagement.ui;

import com.hotelmanagement.model.User;
import com.hotelmanagement.service.AuthenticationService;
import com.hotelmanagement.util.DialogUtil;
import com.hotelmanagement.util.UITheme;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginFrame extends JFrame {

    private final AuthenticationService authService;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel errorLabel;

    public LoginFrame() {
        this.authService = new AuthenticationService();
        initUI();
    }

    private void initUI() {
        setTitle("Grand Luxe Hotel PMS - Secure Staff Login");
        setSize(850, 560);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Left Branding Visual Column (Navy with Gold Accents)
        JPanel leftBrandPanel = new JPanel(new GridBagLayout());
        leftBrandPanel.setBackground(UITheme.PRIMARY_NAVY);
        leftBrandPanel.setPreferredSize(new Dimension(360, 0));

        GridBagConstraints gbcL = new GridBagConstraints();
        gbcL.gridx = 0;
        gbcL.gridy = 0;
        gbcL.insets = new Insets(10, 24, 10, 24);
        gbcL.anchor = GridBagConstraints.CENTER;

        JLabel logoEmblem = new JLabel("GL", SwingConstants.CENTER);
        logoEmblem.setPreferredSize(new Dimension(64, 64));
        logoEmblem.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logoEmblem.setForeground(UITheme.PRIMARY_NAVY);
        logoEmblem.setOpaque(true);
        logoEmblem.setBackground(UITheme.ACCENT_GOLD);

        JLabel brandTitle = new JLabel("GRAND LUXE", SwingConstants.CENTER);
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        brandTitle.setForeground(Color.WHITE);

        JLabel brandSub = new JLabel("HOTEL & RESORT MANAGEMENT", SwingConstants.CENTER);
        brandSub.setFont(new Font("Segoe UI", Font.BOLD, 10));
        brandSub.setForeground(UITheme.ACCENT_GOLD);

        JLabel brandDesc = new JLabel("<html><center style='color:#94a3b8;font-size:11px;'>"
                + "Integrated Property Management System<br>"
                + "Front Desk • Reservations • Billing • Analytics"
                + "</center></html>", SwingConstants.CENTER);

        leftBrandPanel.add(logoEmblem, gbcL);
        gbcL.gridy++;
        leftBrandPanel.add(brandTitle, gbcL);
        gbcL.gridy++;
        leftBrandPanel.add(brandSub, gbcL);
        gbcL.gridy++;
        gbcL.insets = new Insets(20, 24, 10, 24);
        leftBrandPanel.add(brandDesc, gbcL);

        add(leftBrandPanel, BorderLayout.WEST);

        // Right Login Form Column (Clean Off-White)
        JPanel rightFormPanel = new JPanel(new GridBagLayout());
        rightFormPanel.setBackground(UITheme.BG_LIGHT);
        rightFormPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(4, 0, 4, 0);

        JLabel formHeader = new JLabel("Staff Authentication");
        formHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        formHeader.setForeground(UITheme.PRIMARY_NAVY);
        rightFormPanel.add(formHeader, gbc);

        gbc.gridy++;
        JLabel formSub = new JLabel("Please enter your credentials to access the system");
        formSub.setFont(UITheme.FONT_BODY);
        formSub.setForeground(UITheme.TEXT_MUTED);
        rightFormPanel.add(formSub, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(16, 0, 4, 0);
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(UITheme.FONT_BODY_BOLD);
        userLabel.setForeground(UITheme.TEXT_DARK);
        rightFormPanel.add(userLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(2, 0, 8, 0);
        usernameField = new JTextField("admin");
        usernameField.setPreferredSize(new Dimension(0, 36));
        usernameField.setFont(UITheme.FONT_BODY);
        usernameField.setBorder(UITheme.BORDER_INPUT);
        rightFormPanel.add(usernameField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(8, 0, 4, 0);
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(UITheme.FONT_BODY_BOLD);
        passLabel.setForeground(UITheme.TEXT_DARK);
        rightFormPanel.add(passLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(2, 0, 12, 0);
        passwordField = new JPasswordField("Admin@123");
        passwordField.setPreferredSize(new Dimension(0, 36));
        passwordField.setFont(UITheme.FONT_BODY);
        passwordField.setBorder(UITheme.BORDER_INPUT);
        rightFormPanel.add(passwordField, gbc);

        gbc.gridy++;
        errorLabel = new JLabel(" ");
        errorLabel.setFont(UITheme.FONT_SMALL);
        errorLabel.setForeground(UITheme.DANGER_RED);
        rightFormPanel.add(errorLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(8, 0, 16, 0);
        loginButton = new JButton("Sign In to Portal");
        loginButton.setPreferredSize(new Dimension(0, 40));
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(UITheme.PRIMARY_NAVY);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> performLogin());
        rightFormPanel.add(loginButton, gbc);

        // Demo Quick Credentials Helper Bar
        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 4, 0);
        JLabel demoLabel = new JLabel("Quick Demo Profiles:");
        demoLabel.setFont(UITheme.FONT_SMALL);
        demoLabel.setForeground(UITheme.TEXT_MUTED);
        rightFormPanel.add(demoLabel, gbc);

        gbc.gridy++;
        JPanel demoBtnRow = new JPanel(new GridLayout(1, 4, 6, 0));
        demoBtnRow.setOpaque(false);

        demoBtnRow.add(createDemoButton("Admin", "admin", "Admin@123"));
        demoBtnRow.add(createDemoButton("Manager", "manager", "Manager@123"));
        demoBtnRow.add(createDemoButton("Reception", "receptionist", "Recept@123"));
        demoBtnRow.add(createDemoButton("Housekeep", "housekeeping", "House@123"));

        rightFormPanel.add(demoBtnRow, gbc);

        add(rightFormPanel, BorderLayout.CENTER);

        // Enter key listeners
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        usernameField.addKeyListener(enterListener);
        passwordField.addKeyListener(enterListener);
    }

    private JButton createDemoButton(String label, String user, String pass) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBackground(Color.WHITE);
        btn.setForeground(UITheme.PRIMARY_NAVY);
        btn.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            usernameField.setText(user);
            passwordField.setText(pass);
            errorLabel.setText(" ");
        });
        return btn;
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        errorLabel.setText(" ");
        loginButton.setEnabled(false);
        loginButton.setText("Authenticating...");

        SwingUtilities.invokeLater(() -> {
            try {
                User user = authService.authenticate(username, password);
                dispose(); // close login window
                new MainFrame(user).setVisible(true);
            } catch (Exception ex) {
                errorLabel.setText(ex.getMessage());
                loginButton.setEnabled(true);
                loginButton.setText("Sign In to Portal");
            }
        });
    }
}
