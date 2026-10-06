package com.hotelmanagement.ui.components;

import com.hotelmanagement.util.UITheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class CustomTable extends JTable {

    public CustomTable(DefaultTableModel model) {
        super(model);
        setRowHeight(34);
        setFont(UITheme.FONT_BODY);
        setShowGrid(true);
        setGridColor(new Color(241, 245, 249));
        setSelectionBackground(new Color(238, 242, 255));
        setSelectionForeground(UITheme.PRIMARY_NAVY);
        setFillsViewportHeight(true);

        // Header Styling
        JTableHeader header = getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(UITheme.PRIMARY_NAVY);
        header.setPreferredSize(new Dimension(0, 36));
        header.setReorderingAllowed(false);

        // Striped alternating row renderer
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    c.setForeground(UITheme.TEXT_DARK);
                } else {
                    c.setBackground(new Color(224, 231, 255));
                    c.setForeground(UITheme.PRIMARY_NAVY);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false; // read-only by default, interactions via action buttons/dialogs
    }

    public static JScrollPane createScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }
}
