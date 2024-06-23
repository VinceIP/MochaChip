package mochachip.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

//Custom table cell renderer to display breakpoints
public class BreakpointCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                //If the value in this cell is a bool and true, draw a red circle
                if (value instanceof Boolean && (Boolean) value) {
                    g.setColor(Color.RED);
                    g.fillOval(10, 5, 10, 10); // Draw red circle
                }
            }
        };

        panel.setOpaque(true);
        if (isSelected) {
            panel.setBackground(table.getSelectionBackground());
        } else {
            panel.setBackground(table.getBackground());
        }

        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));

        return panel;
    }
}
