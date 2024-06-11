package mochachip.gui;

import mochachip.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

public class MochaChipGUI {
    static final public String title = "MochaChip";
    private String version;
    private String lastPathUsed;
    private Input input;
    private Display display;
    private CPU cpu;
    private Thread emulationThread;
    private JFrame frame;
    private DebugGUI debugGUI;


    public MochaChipGUI(Input input, Display display, CPU cpu) {
        this.display = display;
        this.input = input;
        this.cpu = cpu;
        frame = new JFrame();
        version = getVersion();
        init();
        this.debugGUI = new DebugGUI(cpu);
    }

    public void init() {
        frame.setTitle(title + " - " + version);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        frame.add(display, BorderLayout.CENTER);
        initMenu();
        frame.pack();
    }


    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();
        ArrayList<JMenu> menus = new ArrayList<>();

        JMenu fileMenu = new JMenu("File");
        JMenu emulationMenu = new JMenu("Emulation");
        JMenu displayMenu = new JMenu("Display");
        JMenu settingsMenu = new JMenu("Settings");
        JMenu debuggerMenu = new JMenu("Debugger");
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

        //Debugger
        JMenuItem debuggerItem = new JMenuItem("Open Debugger");
        debuggerItem.addActionListener(e -> openDebugger());

        //About
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

        debuggerMenu.add(debuggerItem);
        debuggerItem.addActionListener(e -> openDebugger());

        aboutMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(emulationMenu);
        menuBar.add(displayMenu);
        menuBar.add(settingsMenu);
        menuBar.add(debuggerMenu);
        menuBar.add(aboutMenu);

        frame.setJMenuBar(menuBar);
    }

    private void loadRom() {
        JFileChooser fileChooser = new JFileChooser();
        if (lastPathUsed != null) fileChooser.setCurrentDirectory(new File(lastPathUsed));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Chip 8 Programs .ch8", "ch8");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(filter);
        int returnValue = fileChooser.showOpenDialog(frame);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            lastPathUsed = fileChooser.getCurrentDirectory().getAbsolutePath();
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            //System.out.println("Path: " + filePath);
            stopEmulation();
            display.reset();
            input.reset();
            cpu = new CPU(input, display);
            if (cpu.getMemory().loadChip8File(filePath)) {
                debugGUI.update(cpu);
                startEmulation();
            }
        } else if (returnValue == JFileChooser.ERROR_OPTION) {
            JOptionPane.showMessageDialog(frame, "Failed to load CH8 file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startEmulation() {
        //frame.setSize(display.windowWidth, display.windowHeight);
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
            frame.repaint();
        }
        if (emulationThread != null && emulationThread.isAlive()) {
            try {
                emulationThread.join();
            } catch (InterruptedException e) {
                JOptionPane.showMessageDialog(frame, "Error occurred while halting emulation thread.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void pauseEmulation() {
        cpu.togglePause();
    }

    private void quit() {
        System.exit(1);
    }

    public void adjustSize(int scaleFactor) {
        display.adjustSize(scaleFactor);
        //adjustSizeForInsets();
        // Explicitly set the size of the JFrame
        //Dimension newSize = frame.getPreferredSize();
        //frame.setSize(newSize);
        frame.pack(); // This should enforce the new size
        centerWindow();
        frame.revalidate();
        frame.repaint();
    }

    private void openDebugger() {
        debugGUI.getFrame().setVisible(true);
        debugGUI.getFrame().setLocationRelativeTo(this.frame);
    }


    public void adjustSizeForInsets() {
        Insets insets = frame.getInsets();
        Dimension displaySize = display.getPreferredSize();
        int menuBarHeight = frame.getJMenuBar().getHeight();
        frame.setSize(displaySize.width + insets.left + insets.right,
                displaySize.height + insets.top + insets.bottom + menuBarHeight);
    }


    public void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.pack();
        frame.revalidate();
        frame.repaint();
    }


    public String getVersion() {
        Properties properties = new Properties();
        try (InputStream input = MochaChip.class.getClassLoader().getResourceAsStream("project.properties")) {
            if (input == null) {
                return "Unknown version";
            }
            properties.load(input);
            return properties.getProperty("version");
        } catch (IOException e) {
            return "Error loading version";
        }
    }

    public JFrame getFrame() {
        return frame;
    }
}
