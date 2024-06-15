package mochachip.gui;

import mochachip.CPU;
import mochachip.Registers;

import javax.swing.*;
import java.awt.*;

public class DebugGUI {
    private JFrame frame;
    private JPanel memoryViewerPanel;
    private JTextArea memoryViewerTextArea;
    private JTextArea instructionViewerTextArea;
    private JPanel registerViewerPanel;
    private JPanel stackViewerPanel;
    private JPanel registerAndStackPanel;
    private JPanel instructionViewerPanel;
    private JLabel[] registerViewerLabels;
    private JLabel[] stackViewerLabels;
    private CPU cpu;
    private JLabel registerILabel;
    private JLabel registerDTLabel;
    private JLabel registerSTLabel;
    private JLabel registerPCLabel;

    public DebugGUI(CPU cpu) {
        this.cpu = cpu;
        frame = new JFrame();
        init();
    }

    private void init() {
        Color bgColor = new Color(25, 25, 25);
        Color textColor = new Color(230, 230, 230);
        Font font = new Font("Monospaced", Font.PLAIN, 12);

        memoryViewerPanel = new JPanel();
        registerViewerPanel = new JPanel();
        stackViewerPanel = new JPanel();
        registerAndStackPanel = new JPanel();
        instructionViewerPanel = new JPanel();

        memoryViewerTextArea = new JTextArea();
        instructionViewerTextArea = new JTextArea();

        //Memory viewer
        memoryViewerPanel.add(memoryViewerTextArea);
        memoryViewerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        memoryViewerPanel.setLayout(new BorderLayout());

        memoryViewerTextArea.setBackground(bgColor);
        memoryViewerTextArea.setForeground(textColor);
        memoryViewerTextArea.setFont(font);
        memoryViewerTextArea.setEditable(false);
        memoryViewerTextArea.setColumns(20);
        memoryViewerTextArea.setRows(10);
        memoryViewerTextArea.setLineWrap(false);

        JScrollPane memoryViewerScrollPane = new JScrollPane(memoryViewerTextArea);
        memoryViewerScrollPane.setVerticalScrollBar(memoryViewerScrollPane.createVerticalScrollBar());
        memoryViewerPanel.add(memoryViewerScrollPane, BorderLayout.CENTER);

        registerAndStackPanel.setLayout(new GridLayout(2, 1, 5, 5));
        registerAndStackPanel.add(registerViewerPanel);
        registerAndStackPanel.add(stackViewerPanel);

        //Register viewer
        registerViewerPanel.setLayout(new GridLayout(0, 2, 25, 25));
        registerViewerPanel.setBackground(bgColor);
        registerViewerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel vRegistersPanel = new JPanel();
        //vRegistersPanel.setLayout(new BoxLayout(vRegistersPanel, BoxLayout.Y_AXIS));
        vRegistersPanel.setLayout(new GridLayout(0, 2, 5, 5));
        vRegistersPanel.setBackground(bgColor);
        //vRegistersPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //left column: v registers
        JLabel registerViewerVHeader = new JLabel();
        registerViewerVHeader.setText("Registers: ");
        registerViewerVHeader.setHorizontalAlignment(JLabel.LEFT);
        registerViewerVHeader.setForeground(textColor);
        registerViewerVHeader.setFont(font);
        vRegistersPanel.add(registerViewerVHeader);
        vRegistersPanel.add(Box.createVerticalStrut(5));

        registerViewerLabels = new JLabel[16];

        for (int i = 0; i < 16; i++) {
            registerViewerLabels[i] = new JLabel();
            String regNum = String.format("%01X", i);
            registerViewerLabels[i].setText("V" + regNum + ": 00");
            registerViewerLabels[i].setHorizontalAlignment(JLabel.LEFT);
            registerViewerLabels[i].setFont(font);
            registerViewerLabels[i].setForeground(textColor);
            vRegistersPanel.add(registerViewerLabels[i]);
        }

        registerViewerPanel.add(vRegistersPanel);

        //right column: additional registers
        JPanel additionalRegistersPanel = new JPanel();
        additionalRegistersPanel.setLayout(new GridLayout(0, 1, 5, 5));
        additionalRegistersPanel.setBackground(bgColor);
        //additionalRegistersPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        additionalRegistersPanel.add(Box.createVerticalStrut(1));

        registerILabel = new JLabel("I: 00");
        registerILabel.setHorizontalAlignment(JLabel.LEFT);
        registerILabel.setForeground(textColor);
        registerILabel.setFont(font);
        additionalRegistersPanel.add(registerILabel);


        registerDTLabel = new JLabel("DT: 00");
        registerDTLabel.setHorizontalAlignment(JLabel.LEFT);
        registerDTLabel.setForeground(textColor);
        registerDTLabel.setFont(font);
        additionalRegistersPanel.add(registerDTLabel);

        registerSTLabel = new JLabel("ST: 00");
        registerSTLabel.setHorizontalAlignment(JLabel.LEFT);
        registerSTLabel.setForeground(textColor);
        registerSTLabel.setFont(font);
        additionalRegistersPanel.add(registerSTLabel);

        registerPCLabel = new JLabel("PC: 00");
        registerPCLabel.setHorizontalAlignment(JLabel.LEFT);
        registerPCLabel.setForeground(textColor);
        registerPCLabel.setFont(font);
        //additionalRegistersPanel.add(Box.createVerticalStrut(10));
        additionalRegistersPanel.add(registerPCLabel);

        registerViewerPanel.add(additionalRegistersPanel);

        //Stack
        stackViewerPanel.setBackground(bgColor);
        stackViewerPanel.setLayout(new GridLayout(0, 2, 5, 5));
        stackViewerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel stackHeaderLabel = new JLabel("Stack: ");
        stackHeaderLabel.setForeground(textColor);
        stackHeaderLabel.setFont(font);
        stackViewerPanel.add(stackHeaderLabel);
        stackViewerPanel.add(Box.createVerticalStrut(20));

        stackViewerLabels = new JLabel[16];
        for (int i = 0; i < 16; i++) {
            String addr = String.format("%01X", i);
            stackViewerLabels[i] = new JLabel(addr + ": ");
            stackViewerLabels[i].setForeground(textColor);
            stackViewerLabels[i].setFont(font);
            //stackViewerLabels[i].setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            stackViewerPanel.add(stackViewerLabels[i]);
        }

        instructionViewerPanel.add(instructionViewerTextArea);
        instructionViewerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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
        frame.add(registerAndStackPanel);
        frame.add(instructionViewerPanel);

        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.setResizable(true);
        frame.setPreferredSize(new Dimension(1024, 768));
        frame.pack();
    }

    //Update some debugger values when needed
    public void updateRegister(int address, int val) {
        if (frame.isVisible()) {
            SwingUtilities.invokeLater(() -> {
                if (address <= 16) {
                    String regStr = String.format("%01X", address);
                    String valStr = String.format("%02X", (val & 0xFF));
                    registerViewerLabels[address].setText("V" + regStr + ": " + valStr);
                }
            });

        }
    }

    public void updateRegister(RegisterType registerType, int val) {
        if (frame.isVisible()) {
            SwingUtilities.invokeLater(() -> {
                if (registerType == RegisterType.I) {
                    String valStr = String.format("%04X", (val & 0xFFF));
                    registerILabel.setText("I: " + valStr);
                } else if (registerType == RegisterType.DT) {
                    String valStr = String.format("%02X", (val & 0xFF));
                    registerDTLabel.setText("DT: " + valStr);
                } else if (registerType == RegisterType.ST) {
                    String valStr = String.format("%02X", (val & 0xFF));
                    registerSTLabel.setText("ST: " + valStr);
                } else if (registerType == RegisterType.PC) {
                    String valStr = String.format("%02X", (val & 0xFF));
                    registerPCLabel.setText("PC: " + valStr);
                }
            });
        }
    }

    public void updateStack(int index, int address) {
        if (frame.isVisible()) {
            SwingUtilities.invokeLater(() -> {
                String indexStr = String.format("%01X", index);
                if (address > -1) {
                    String addrStr = String.format("%04X", (address & 0xFFFF));
                    stackViewerLabels[index].setText(indexStr + ": " + addrStr);
                } else {
                    stackViewerLabels[index].setText(indexStr + ": ");
                }
            });
        }
    }

    public enum RegisterType {
        I,
        ST,
        DT,
        PC
    }

    public void updateMemoryMap() {
        displayMemory();
    }

    public JFrame getFrame() {
        return frame;
    }

    private void displayMemory() {
        SwingUtilities.invokeLater(() -> {
            int memoryPerLine = 10;
            memoryViewerTextArea.setText("");
            byte[] memory = cpu.getMemory().getMemoryArray();
            //Start printing values after unused memory
            try {
                for (int i = 512; i < cpu.getMemory().getMemoryArray().length; i++) {
                    String value = String.format("%02X", (memory[i] & 0xFF));
                    String address = String.format("$%04X", i);
                    if (i == 512 || (i - 0x200) % memoryPerLine == 0) {
                        memoryViewerTextArea.append("\n" + address + ": ");
                    }
                    memoryViewerTextArea.append(String.format("%-3s", value));
                }
            } catch (NumberFormatException e) {
                System.out.println(e.getMessage());
            }

            memoryViewerTextArea.setCaretPosition(0);
        });

    }

    private void displayRegisters() {
        Registers registers = cpu.getRegisters();
    }


    private void displayInstructions() {
        instructionViewerTextArea.setText("Instructions:\n");
    }

    public void setCpu(CPU cpu) {
        this.cpu = cpu;
    }
}
