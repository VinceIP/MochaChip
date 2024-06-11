package mochachip.gui;

import mochachip.CPU;

import javax.swing.*;
import java.awt.*;

public class DebugGUI {
    private JFrame frame;
    private JPanel memoryViewerPanel;
    private JTextArea memoryViewerTextArea;
    private JTextArea registerViewerTextArea;
    private JTextArea instructionViewerTextArea;
    private JPanel registerViewerPanel;
    private JPanel instructionViewerPanel;
    private CPU cpu;
    private String memoryString;

    public DebugGUI(CPU cpu) {
        this.cpu = cpu;
        frame = new JFrame();


        init();
    }

    private void init() {
        Color bgColor = new Color(25, 25, 25);

        memoryViewerPanel = new JPanel();
        registerViewerPanel = new JPanel();
        instructionViewerPanel = new JPanel();

        memoryViewerTextArea = new JTextArea();
        registerViewerTextArea = new JTextArea();
        instructionViewerTextArea = new JTextArea();

        memoryViewerPanel.add(memoryViewerTextArea);
        memoryViewerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        memoryViewerPanel.setLayout(new BorderLayout());

        memoryViewerTextArea.setBackground(bgColor);
        memoryViewerTextArea.setEditable(false);
        memoryViewerTextArea.setColumns(20);
        memoryViewerTextArea.setRows(10);
        memoryViewerTextArea.setLineWrap(false);

        JScrollPane memoryViewerScrollPane = new JScrollPane(memoryViewerTextArea);
        memoryViewerScrollPane.setVerticalScrollBar(memoryViewerScrollPane.createVerticalScrollBar());
        memoryViewerPanel.add(memoryViewerScrollPane, BorderLayout.CENTER);

        registerViewerPanel.add(registerViewerTextArea);
        registerViewerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        registerViewerPanel.setLayout(new BorderLayout());

        registerViewerTextArea.setBackground(bgColor);
        registerViewerTextArea.setEditable(false);
        registerViewerTextArea.setColumns(20);
        registerViewerTextArea.setRows(10);
        registerViewerTextArea.setLineWrap(false);

        JScrollPane registerViewerScrollPane = new JScrollPane(registerViewerTextArea);
        registerViewerScrollPane.setVerticalScrollBar(registerViewerScrollPane.createVerticalScrollBar());
        registerViewerPanel.add(registerViewerScrollPane, BorderLayout.CENTER);

        instructionViewerPanel.add(instructionViewerTextArea);
        instructionViewerPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        instructionViewerPanel.setLayout(new BorderLayout());

        instructionViewerTextArea.setBackground(bgColor);
        instructionViewerTextArea.setEditable(false);
        instructionViewerTextArea.setColumns(20);
        instructionViewerTextArea.setRows(10);
        instructionViewerTextArea.setLineWrap(false);

        JScrollPane instructionViewerScrollPane = new JScrollPane(instructionViewerTextArea);
        instructionViewerScrollPane.setVerticalScrollBar(instructionViewerScrollPane.createVerticalScrollBar());
        instructionViewerPanel.add(instructionViewerScrollPane, BorderLayout.CENTER);

        frame.setLayout(new GridLayout(1, 3, 5, 5));
        frame.add(memoryViewerPanel);
        frame.add(registerViewerPanel);
        frame.add(instructionViewerPanel);

        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.setResizable(true);
        frame.setPreferredSize(new Dimension(800, 600));
        frame.pack();
    }

    //Update some debugger values when needed
    public void update(CPU cpu) {
        this.cpu = cpu; //Update current running cpu reference
        displayMemory();
        displayRegisters();
        displayInstructions();
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

    private void displayRegisters() {
        registerViewerTextArea.setText("Register view");
    }

    private void displayInstructions(){
        instructionViewerTextArea.setText("Instruction view");
    }

}
