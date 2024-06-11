package mochachip.gui;

import mochachip.CPU;

import javax.swing.*;
import java.awt.*;

public class DebugGUI {
    private JFrame frame;
    private JPanel memoryViewerPanel;
    private JTextArea memoryViewerTextArea;
    private JPanel registerViewerPanel;
    private CPU cpu;
    private String memoryString;

    public DebugGUI(CPU cpu) {
        this.cpu = cpu;
        frame = new JFrame();
        memoryViewerPanel = new JPanel();
        memoryViewerTextArea = new JTextArea();
        registerViewerPanel = new JPanel();
        init();
    }

    private void init() {
        Color bgColor = new Color(25, 25, 25);
        //memoryViewerPanel.setBackground(bgColor);
        memoryViewerPanel.add(memoryViewerTextArea);
        memoryViewerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        memoryViewerPanel.setLayout(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(memoryViewerTextArea);
        scrollPane.setVerticalScrollBar(scrollPane.createVerticalScrollBar());
        memoryViewerPanel.add(scrollPane, BorderLayout.CENTER);

        memoryViewerTextArea.setBackground(bgColor);
        memoryViewerTextArea.setEditable(false);
        memoryViewerTextArea.setColumns(20);
        memoryViewerTextArea.setRows(10);
        memoryViewerTextArea.setLineWrap(false);

        registerViewerPanel.setBackground(bgColor);
        registerViewerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        frame.setLayout(new GridLayout(1, 2, 5, 5));
        frame.add(memoryViewerPanel);
        frame.add(registerViewerPanel);

        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.setResizable(true);
        frame.setPreferredSize(new Dimension(500, 500));
        frame.pack();
    }

    //Update some debugger values when needed
    public void update(CPU cpu) {
        this.cpu = cpu; //Update current running cpu reference
        displayMemory();
    }

    public JFrame getFrame() {
        return frame;
    }

    private void displayMemory() {
        int memoryPerLine = 10;
        memoryViewerTextArea.setText("");
        byte[] memory = cpu.getMemory().getMemoryArray();
        //Start printing values after unused memory
        for (int i = 0x200; i < cpu.getMemory().getMemoryArray().length; i++) {
            String value = String.format("%02X", (memory[i] & 0xFF));
            String address = String.format("$%04X", i);
            if (i == 0x200) {
                memoryViewerTextArea.append(address + ":   ");
            } else if (i % memoryPerLine == 0) {
                memoryViewerTextArea.append("\n" + address + ":   ");

            }
            //memoryViewerTextArea.append(value + " ");
            memoryViewerTextArea.append(String.format("%-3s", value));
        }
    }

}
