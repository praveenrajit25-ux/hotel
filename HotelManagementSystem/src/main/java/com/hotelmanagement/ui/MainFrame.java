package com.hotelmanagement.ui;

import com.hotelmanagement.model.User;
import com.hotelmanagement.ui.components.Navbar;
import com.hotelmanagement.ui.components.Sidebar;
import com.hotelmanagement.util.DialogUtil;
import com.hotelmanagement.util.UITheme;
import java.awt.*;
import javax.swing.*;

public class MainFrame extends JFrame {

    private final User currentUser;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final Sidebar sidebar;
    private final Navbar navbar;

    private DashboardPanel dashboardPanel;
    private RoomManagementPanel roomPanel;
    private GuestManagementPanel guestPanel;
    private ReservationManagementPanel reservationPanel;
    private CheckInPanel checkInPanel;
    private CheckOutPanel checkOutPanel;
    private BillingPanel billingPanel;
    private ServiceManagementPanel servicePanel;
    private StaffManagementPanel staffPanel;
    private ReportsPanel reportsPanel;
    private SettingsPanel settingsPanel;

    public MainFrame(User user) {
        this.currentUser = user;
        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(cardLayout);

        setTitle("Grand Luxe Hotel - Property Management System (PMS)");
        setSize(1320, 840);
        setMinimumSize(new Dimension(1080, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize Navigation Top Bar
        this.navbar = new Navbar(currentUser, this::performLogout);
        add(navbar, BorderLayout.NORTH);

        // Initialize Navigation Side Bar
        this.sidebar = new Sidebar(currentUser, this::showCard);
        add(sidebar, BorderLayout.WEST);

        // Initialize All Module Panels
        initCardModules();
        add(cardPanel, BorderLayout.CENTER);

        // Start on Dashboard
        showCard(Sidebar.CARD_DASHBOARD);
    }

    private void initCardModules() {
        dashboardPanel = new DashboardPanel(currentUser, this::showCard);
        roomPanel = new RoomManagementPanel(currentUser);
        guestPanel = new GuestManagementPanel(currentUser);
        reservationPanel = new ReservationManagementPanel(currentUser, this::showCard);
        checkInPanel = new CheckInPanel(currentUser);
        checkOutPanel = new CheckOutPanel(currentUser, this::showCard);
        billingPanel = new BillingPanel(currentUser);
        servicePanel = new ServiceManagementPanel(currentUser);
        staffPanel = new StaffManagementPanel(currentUser);
        reportsPanel = new ReportsPanel(currentUser);
        settingsPanel = new SettingsPanel(currentUser);

        cardPanel.add(dashboardPanel, Sidebar.CARD_DASHBOARD);
        cardPanel.add(roomPanel, Sidebar.CARD_ROOMS);
        cardPanel.add(guestPanel, Sidebar.CARD_GUESTS);
        cardPanel.add(reservationPanel, Sidebar.CARD_RESERVATIONS);
        cardPanel.add(checkInPanel, Sidebar.CARD_CHECK_IN);
        cardPanel.add(checkOutPanel, Sidebar.CARD_CHECK_OUT);
        cardPanel.add(billingPanel, Sidebar.CARD_BILLING);
        cardPanel.add(servicePanel, Sidebar.CARD_SERVICES);
        cardPanel.add(staffPanel, Sidebar.CARD_STAFF);
        cardPanel.add(reportsPanel, Sidebar.CARD_REPORTS);
        cardPanel.add(settingsPanel, Sidebar.CARD_SETTINGS);
    }

    public void showCard(String cardName) {
        cardLayout.show(cardPanel, cardName);
        if (sidebar != null) {
            sidebar.setActiveCard(cardName);
        }

        // Trigger live refresh when switching to tab
        SwingUtilities.invokeLater(() -> {
            switch (cardName) {
                case Sidebar.CARD_DASHBOARD -> dashboardPanel.refreshData();
                case Sidebar.CARD_ROOMS -> roomPanel.refreshRooms();
                case Sidebar.CARD_GUESTS -> guestPanel.refreshGuests();
                case Sidebar.CARD_RESERVATIONS -> reservationPanel.refreshReservations();
                case Sidebar.CARD_CHECK_IN -> checkInPanel.refreshData();
                case Sidebar.CARD_CHECK_OUT -> checkOutPanel.refreshData();
                case Sidebar.CARD_BILLING -> billingPanel.refreshInvoices();
                case Sidebar.CARD_SERVICES -> servicePanel.refreshAll();
                case Sidebar.CARD_STAFF -> staffPanel.refreshStaff();
                case Sidebar.CARD_REPORTS -> reportsPanel.generateReport();
            }
        });
    }

    private void performLogout() {
        boolean confirm = DialogUtil.confirm(this, "Are you sure you want to log out of the system?", "Confirm Logout");
        if (confirm) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
