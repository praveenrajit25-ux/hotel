package com.hotelmanagement.util;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

public class UITheme {

    // Sophisticated Luxury Palette: Dark Navy, Slate, Gold Accents, Clean White/Off-White
    public static final Color PRIMARY_NAVY = new Color(15, 23, 42);      // #0f172a
    public static final Color SECONDARY_NAVY = new Color(30, 41, 59);    // #1e293b
    public static final Color SIDEBAR_HOVER = new Color(51, 65, 85);     // #334155
    public static final Color ACCENT_GOLD = new Color(217, 119, 6);      // #d97706
    public static final Color ACCENT_GOLD_HOVER = new Color(245, 158, 11); // #f59e0b
    public static final Color BG_LIGHT = new Color(248, 250, 252);        // #f8fafc
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER_LIGHT = new Color(226, 232, 240);    // #e2e8f0
    public static final Color TEXT_DARK = new Color(15, 23, 42);         // #0f172a
    public static final Color TEXT_MUTED = new Color(100, 116, 139);     // #64748b
    public static final Color SUCCESS_GREEN = new Color(22, 163, 74);    // #16a34a
    public static final Color DANGER_RED = new Color(220, 38, 38);       // #dc2626
    public static final Color INFO_BLUE = new Color(2, 132, 199);        // #0284c7
    public static final Color WARNING_ORANGE = new Color(234, 88, 12);   // #ea580c

    // Typography
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    // Borders
    public static final Border BORDER_INPUT = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
    );

    public static final Border BORDER_PANEL = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
    );
}
