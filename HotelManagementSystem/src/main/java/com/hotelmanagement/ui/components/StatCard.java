package com.hotelmanagement.ui.components;

import com.hotelmanagement.util.UITheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class StatCard extends JPanel {

    private final JLabel valueLabel;
    private final JLabel titleLabel;
    private final JLabel subLabel;
    private final Color accentColor;

    public StatCard(String title, String initialValue, String subtitle, Color accentColor) {
        this.accentColor = accentColor;
        setLayout(new BorderLayout(10, 8));
        setBackground(UITheme.CARD_BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1),
                        new EmptyBorder(14, 16, 14, 16)
                )
        ));

        // Center Content
        JPanel contentPanel = new JPanel(new GridLayout(3, 1, 2, 2));
        contentPanel.setOpaque(false);

        titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(UITheme.TEXT_MUTED);

        valueLabel = new JLabel(initialValue);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(UITheme.PRIMARY_NAVY);

        subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLabel.setForeground(accentColor);

        contentPanel.add(titleLabel);
        contentPanel.add(valueLabel);
        contentPanel.add(subLabel);

        add(contentPanel, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setSubtitle(String subtitle) {
        subLabel.setText(subtitle);
    }
}
