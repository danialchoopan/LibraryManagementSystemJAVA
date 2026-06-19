package com.library.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class StatusCellRenderer extends DefaultTableCellRenderer {

    private static final Color BORROWED_COLOR = new Color(0xF39C12);
    private static final Color RETURNED_COLOR = new Color(0x2ECC71);
    private static final Color OVERDUE_COLOR = new Color(0xE74C3C);
    private static final Color AVAILABLE_GOOD = new Color(0x2ECC71);
    private static final Color AVAILABLE_LOW = new Color(0xF39C12);
    private static final Color AVAILABLE_ZERO = new Color(0xE74C3C);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value == null) return c;

        String text = value.toString();
        setHorizontalAlignment(SwingConstants.CENTER);

        if (!isSelected) {
            switch (text) {
                case "BORROWED":
                    setBackground(BORROWED_COLOR.darker());
                    setForeground(BORROWED_COLOR);
                    break;
                case "RETURNED":
                    setBackground(RETURNED_COLOR.darker());
                    setForeground(RETURNED_COLOR);
                    break;
                case "OVERDUE":
                    setBackground(OVERDUE_COLOR.darker());
                    setForeground(OVERDUE_COLOR);
                    break;
                default:
                    setBackground(table.getBackground());
                    setForeground(table.getForeground());
                    break;
            }
        }
        return c;
    }

    public static void applyAvailabilityColor(JTable table, int column) {
        table.getColumnModel().getColumn(column).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (value != null && !isSelected) {
                    int qty = Integer.parseInt(value.toString());
                    if (qty == 0) {
                        setBackground(AVAILABLE_ZERO.darker());
                        setForeground(AVAILABLE_ZERO);
                    } else if (qty <= 2) {
                        setBackground(AVAILABLE_LOW.darker());
                        setForeground(AVAILABLE_LOW);
                    } else {
                        setBackground(AVAILABLE_GOOD.darker());
                        setForeground(AVAILABLE_GOOD);
                    }
                }
                return c;
            }
        });
    }
}
