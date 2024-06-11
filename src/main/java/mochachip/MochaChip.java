package mochachip;

import com.formdev.flatlaf.FlatDarkLaf;
import mochachip.gui.MochaChipGUI;

import javax.swing.*;

public class MochaChip {
    private MochaChipGUI gui;
    private Display display;
    private Input input;
    private CPU cpu;

    public MochaChip() {
        input = new Input();
        display = new Display(input);
        //cpu = new CPU(input, display);
        gui = new MochaChipGUI(input, display, cpu);

    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatDarkLaf.setup();
            UIManager.put("MenuBar.background", UIManager.getColor("Panel.background"));
            MochaChip emulator = new MochaChip();
            emulator.gui.getFrame().setLocationRelativeTo(null);
            emulator.gui.getFrame().setVisible(true);
        });

    }
}
