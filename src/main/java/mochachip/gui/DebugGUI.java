package mochachip.gui;

import mochachip.CPU;
import mochachip.Instruction;
import mochachip.Registers;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.List;

public class DebugGUI {
    private JFrame frame;
    private JPanel memoryViewerPanel;
    private JTextArea memoryViewerTextArea;
    private JTable instructionViewerTable;
    private JPanel registerViewerPanel;
    private JPanel stackViewerPanel;
    private JPanel registerAndStackPanel;
    private JPanel instructionViewerPanel;
    private JPanel stepModePanel;
    private JLabel[] registerViewerLabels;
    private JLabel[] stackViewerLabels;
    private CPU cpu;
    private JLabel registerILabel;
    private JLabel registerDTLabel;
    private JLabel registerSTLabel;
    private JLabel registerPCLabel;
    private JCheckBox stepModeCheckBox;
    private JButton stepModeStepThroughButton;
    private List<Instruction> instructionList;
    private Object[][] instructionTableData;

    Color bgColor = new Color(25, 25, 25);
    Color textColor = new Color(230, 230, 230);
    Font font = new Font("Monospaced", Font.PLAIN, 12);

    public DebugGUI(CPU cpu) {
        this.cpu = cpu;
        frame = new JFrame();
        init();
    }

    private void init() {


        //Init main panels
        memoryViewerPanel = new JPanel();
        registerViewerPanel = new JPanel();
        stackViewerPanel = new JPanel();
        registerAndStackPanel = new JPanel(); //Parent for registry and stack
        instructionViewerPanel = new JPanel();

        //Init text areas and tables
        memoryViewerTextArea = new JTextArea();
        instructionViewerTable = new JTable();

        //Memory viewer
        memoryViewerPanel.add(memoryViewerTextArea);
        memoryViewerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        memoryViewerPanel.setLayout(new BorderLayout());

        //Memory viewer properties
        memoryViewerTextArea.setBackground(bgColor);
        memoryViewerTextArea.setForeground(textColor);
        memoryViewerTextArea.setFont(font);
        memoryViewerTextArea.setEditable(false);
        memoryViewerTextArea.setColumns(20);
        memoryViewerTextArea.setRows(10);
        memoryViewerTextArea.setLineWrap(false);

        //Memory viewer scroll pane
        JScrollPane memoryViewerScrollPane = new JScrollPane(memoryViewerTextArea);
        memoryViewerScrollPane.setVerticalScrollBar(memoryViewerScrollPane.createVerticalScrollBar());
        memoryViewerPanel.add(memoryViewerScrollPane, BorderLayout.CENTER);

        //RegisterAndStack properties
        registerAndStackPanel.setLayout(new GridLayout(2, 1, 5, 5));
        registerAndStackPanel.add(registerViewerPanel);
        registerAndStackPanel.add(stackViewerPanel);

        //Register viewer properties
        registerViewerPanel.setLayout(new GridLayout(0, 2, 25, 25));
        registerViewerPanel.setBackground(bgColor);
        registerViewerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel vRegistersPanel = new JPanel();
        vRegistersPanel.setLayout(new GridLayout(0, 2, 5, 5));
        vRegistersPanel.setBackground(bgColor);

        //left column: v registers
        JLabel registerViewerVHeader = new JLabel();
        registerViewerVHeader.setText("Registers: ");
        registerViewerVHeader.setHorizontalAlignment(JLabel.LEFT);
        registerViewerVHeader.setForeground(textColor);
        registerViewerVHeader.setFont(font);
        vRegistersPanel.add(registerViewerVHeader);
        vRegistersPanel.add(Box.createVerticalStrut(5));

        //Init labels for registers
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

        additionalRegistersPanel.add(Box.createVerticalStrut(1));

        //Init labels for additional registers
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
        additionalRegistersPanel.add(registerPCLabel);

        registerViewerPanel.add(additionalRegistersPanel);

        //Stack panel properties
        stackViewerPanel.setBackground(bgColor);
        stackViewerPanel.setLayout(new GridLayout(0, 2, 5, 5));
        stackViewerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel stackHeaderLabel = new JLabel("Stack: ");
        stackHeaderLabel.setForeground(textColor);
        stackHeaderLabel.setFont(font);
        stackViewerPanel.add(stackHeaderLabel);
        stackViewerPanel.add(Box.createVerticalStrut(20));

        //Init labels for stack viewer
        stackViewerLabels = new JLabel[16];
        for (int i = 0; i < 16; i++) {
            String addr = String.format("%01X", i);
            stackViewerLabels[i] = new JLabel(addr + ": ");
            stackViewerLabels[i].setForeground(textColor);
            stackViewerLabels[i].setFont(font);
            stackViewerPanel.add(stackViewerLabels[i]);
        }

        //Step Mode
        stepModePanel = new JPanel();
        stepModePanel.setLayout(new BoxLayout(stepModePanel, BoxLayout.X_AXIS));
        //stepModePanel.setMaximumSize(new Dimension(100,100));
        stepModeCheckBox = new JCheckBox("Enable step mode: ");
        stepModeStepThroughButton = new JButton("->");
        stepModeStepThroughButton.setToolTipText("Step through");
        stepModePanel.add(stepModeCheckBox);
        stepModePanel.add(stepModeStepThroughButton);
        instructionViewerPanel.add(stepModePanel);

        //Instruction panel properties
        instructionViewerPanel.add(instructionViewerTable);
        instructionViewerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        instructionViewerPanel.setLayout(new BoxLayout(instructionViewerPanel, BoxLayout.Y_AXIS));

        //Instruction table properties
        instructionViewerTable.setBackground(bgColor);
        instructionViewerTable.setForeground(textColor);
        instructionViewerTable.setFont(font);


        //Instruction panel scroll pane
        JScrollPane instructionViewerScrollPane = new JScrollPane(instructionViewerTable);
        instructionViewerScrollPane.setVerticalScrollBar(instructionViewerScrollPane.createVerticalScrollBar());
        instructionViewerPanel.add(instructionViewerScrollPane, BorderLayout.CENTER);


        //Setup whole frame and finish
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
                    String valStr = String.format("%04X", val);
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

    public void initInstructionTable() {
        if (instructionList != null) {
            instructionTableData = new Object[instructionList.size()][4];
            String[] instructionTableColumns = new String[]{
                    "BRK", "Address", "Instruction", "Description"
            };

            for (int i = 0; i < instructionList.size(); i++) {
                Instruction instruction = instructionList.get(i);
                instructionTableData[i][0] = instruction.isBreakpoint(); //Renders a red oval if isBreakpoint, per custom cell renderer
                instructionTableData[i][1] = String.format("%04X", instruction.getAddress());
                instructionTableData[i][2] = String.format("%04X", instruction.getByteCode());
                instructionTableData[i][3] = instruction.getDescription();
            }

            TableModel tableModel = new TableModel() {
                @Override
                public int getRowCount() {
                    return instructionTableData.length;
                }

                @Override
                public int getColumnCount() {
                    return instructionTableColumns.length;
                }

                @Override
                public String getColumnName(int columnIndex) {
                    return instructionTableColumns[columnIndex];
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    //If column index is 0, get column class as boolean for BRK, otherwise String
                    return columnIndex == 0 ? Boolean.class : String.class;
                }

                @Override
                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return false;
                }

                @Override
                public Object getValueAt(int rowIndex, int columnIndex) {
                    return instructionTableData[rowIndex][columnIndex];
                }

                @Override
                public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                    instructionTableData[rowIndex][columnIndex] = aValue;
                }

                @Override
                public void addTableModelListener(TableModelListener l) {
                }

                @Override
                public void removeTableModelListener(TableModelListener l) {
                }
            };

            instructionViewerTable.setModel(tableModel);
            instructionViewerTable.getColumnModel().getColumn(0).setCellRenderer(new BreakpointCellRenderer());

            instructionViewerTable.getColumnModel().getColumn(0).setPreferredWidth(30);
            instructionViewerTable.getColumnModel().getColumn(0).setMaxWidth(30);
            instructionViewerTable.getColumnModel().getColumn(0).setResizable(false);

        }

    }

    public void setInstructionList(List<Instruction> instructionList) {
        this.instructionList = instructionList;
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
    }

    public void setCpu(CPU cpu) {
        this.cpu = cpu;
    }
}
