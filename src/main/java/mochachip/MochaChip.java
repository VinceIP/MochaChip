package mochachip;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.ArrayList;

public class MochaChip extends JFrame {
    private Display display;
    private Input input;
    private CPU cpu;
    private Thread emulationThread;
    private String lastPathUsed;

    public MochaChip() {
        setTitle("MochaChip");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        input = new Input();
        display = new Display(input);
        add(display, BorderLayout.CENTER);
        initMenu();
        pack();
        adjustSizeForInsets();
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIManager.getColor("Panel.background"));
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                display.resizeDisplay();
                pack();
            }
        });
    }

    private void adjustSizeForInsets() {
        Insets insets = getInsets();
        Dimension displaySize = display.getPreferredSize();
        int menuBarHeight = getJMenuBar().getHeight();
        setSize(displaySize.width + insets.left + insets.right,
                displaySize.height + insets.top + insets.bottom + menuBarHeight);
    }

    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();
        ArrayList<JMenu> menus = new ArrayList<>();

        JMenu fileMenu = new JMenu("File");
        JMenu emulationMenu = new JMenu("Emulation");
        JMenu displayMenu = new JMenu("Display");
        JMenu settingsMenu = new JMenu("Settings");
        JMenu aboutMenu = new JMenu("About");

        //File
        JMenuItem loadRomItem = new JMenuItem("Load CH8 file");
        loadRomItem.addActionListener(e -> loadRom());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> quit());

        //Emulation
        JMenuItem pauseItem = new JMenuItem("Pause/Resume Emulation");
        pauseItem.addActionListener(e -> pauseEmulation());
        JMenuItem stopItem = new JMenuItem("Stop Emulation");
        stopItem.addActionListener(e -> stopEmulation());
        JMenuItem optionsItem = new JMenuItem("Options");

        //mochachip.Display
        //Size
        JMenu windowSize = new JMenu("Size");
        JMenuItem windowSize1x = new JMenuItem("1");
        windowSize1x.addActionListener(e -> adjustSize(12));
        JMenuItem windowSize2x = new JMenuItem("2");
        windowSize2x.addActionListener(e -> adjustSize(16));
        JMenuItem windowSize3x = new JMenuItem("3");
        windowSize3x.addActionListener(e -> adjustSize(22));
        JMenuItem windowSize4x = new JMenuItem("4");
        windowSize4x.addActionListener(e -> adjustSize(28));

        //Color
        JMenu colorMenu = new JMenu("Color");
        JMenuItem colorMono = new JMenuItem("Monochrome");
        JMenuItem colorSoftMono = new JMenuItem("Soft Monochrome");
        JMenuItem colorIce = new JMenuItem("Ice");
        JMenuItem colorOlive = new JMenuItem("Olive");
        JMenuItem colorLava = new JMenuItem("Lava");
        JMenuItem colorGrape = new JMenuItem("Grape");
        JMenuItem colorDesert = new JMenuItem("Desert");
        JMenuItem colorPastel = new JMenuItem("Pastel");

        //Settings
        JMenuItem settingsItem = new JMenuItem("Open settings");

        JMenuItem aboutItem = new JMenuItem("About Chip8Emulator");

        fileMenu.add(loadRomItem);
        fileMenu.add(exitItem);

        emulationMenu.add(pauseItem);
        emulationMenu.add(stopItem);
        emulationMenu.add(optionsItem);

        displayMenu.add(windowSize);
        displayMenu.add(colorMenu);
        windowSize.add(windowSize1x);
        windowSize.add(windowSize2x);
        windowSize.add(windowSize3x);
        windowSize.add(windowSize4x);

        displayMenu.add(colorMenu);
        colorMenu.add(colorMono);
        colorMono.addActionListener(e -> display.setColorTheme(ColorTheme.Chip8Color.MONOCHROME));
        colorMenu.add(colorSoftMono);
        colorSoftMono.addActionListener(e -> display.setColorTheme(ColorTheme.Chip8Color.SOFT_MONOCHROME));
        colorMenu.add(colorIce);
        colorIce.addActionListener(e -> display.setColorTheme(ColorTheme.Chip8Color.ICE));
        colorMenu.add(colorOlive);
        colorOlive.addActionListener(e -> display.setColorTheme(ColorTheme.Chip8Color.OLIVE));
        colorMenu.add(colorLava);
        colorLava.addActionListener(e -> display.setColorTheme(ColorTheme.Chip8Color.LAVA));
        colorMenu.add(colorGrape);
        colorGrape.addActionListener(e -> display.setColorTheme(ColorTheme.Chip8Color.GRAPE));
        colorMenu.add(colorDesert);
        colorDesert.addActionListener(e -> display.setColorTheme(ColorTheme.Chip8Color.DESERT));
        colorMenu.add(colorPastel);
        colorPastel.addActionListener(e -> display.setColorTheme(ColorTheme.Chip8Color.PASTEL));

        settingsMenu.add(settingsItem);

        aboutMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(emulationMenu);
        menuBar.add(displayMenu);
        menuBar.add(settingsMenu);
        menuBar.add(aboutMenu);

        setJMenuBar(menuBar);
    }

    private void loadRom() {
        JFileChooser fileChooser = new JFileChooser();
        if (lastPathUsed != null) fileChooser.setCurrentDirectory(new File(lastPathUsed));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Chip 8 Programs .ch8", "ch8");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(filter);
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            lastPathUsed = fileChooser.getCurrentDirectory().getAbsolutePath();
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            //System.out.println("Path: " + filePath);
            stopEmulation();
            display.reset();
            input.reset();
            cpu = new CPU(display, input);
            if (cpu.memory.loadChip8File(filePath)) startEmulation();
        } else if (returnValue == JFileChooser.ERROR_OPTION) {
            JOptionPane.showMessageDialog(this, "Failed to load CH8 file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startEmulation() {
        setSize(display.windowWidth, display.windowHeight);
        //Run emulation on separate thread
        if (emulationThread != null && emulationThread.isAlive()) {
            emulationThread.interrupt();
        }
        emulationThread = new Thread(() -> cpu.start());
        emulationThread.start();
    }

    private void stopEmulation() {
        if (cpu != null) {
            cpu.stop();
            repaint();
        }
        if (emulationThread != null && emulationThread.isAlive()) {
            try {
                emulationThread.join();
            } catch (InterruptedException e) {
                JOptionPane.showMessageDialog(this, "Error occurred while halting emulation thread.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void pauseEmulation() {
        cpu.togglePause();
    }

    private void quit() {
        System.exit(1);
    }

    private void adjustSize(int scaleFactor) {
        display.adjustSize(scaleFactor);
        pack();
        centerWindow();
    }

    private void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatDarkLaf.setup();
            UIManager.put("MenuBar.background", UIManager.getColor("Panel.background"));
            MochaChip emulator = new MochaChip();
            emulator.setVisible(true);
        });
    }
}
