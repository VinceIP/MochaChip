import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

public class Chip8Emulator extends JFrame {
    private Display display;
    private Input input;
    private CPU cpu;
    private Thread emulationThread;
    private String lastPathUsed;

    public Chip8Emulator() {
        setTitle("Chip8Emulator");
        //setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        input = new Input();
        display = new Display(input);
        add(display, BorderLayout.CENTER);
        initMenu();
        pack();
        adjustSizeForInsets();
        display.resizeDisplay(display.getPreferredSize().width, display.getPreferredSize().height);
        getContentPane().setBackground(UIManager.getColor("Panel.background"));
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                display.resizeDisplay(getWidth(), getHeight());
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

        JMenu fileMenu = new JMenu("File");
        JMenu emulationMenu = new JMenu("Emulation");
        JMenu aboutMenu = new JMenu("About");

        JMenuItem loadRomItem = new JMenuItem("Load CH8 file");
        loadRomItem.addActionListener(e -> loadRom());
        JMenuItem exitItem = new JMenuItem("Exit");

        JMenuItem pauseItem = new JMenuItem("Pause Emulation");
        JMenuItem stopItem = new JMenuItem("Stop Emulation");
        JMenuItem optionsItem = new JMenuItem("Options");

        JMenuItem aboutItem = new JMenuItem("About Chip8Emulator");

        fileMenu.add(loadRomItem);
        fileMenu.add(exitItem);

        emulationMenu.add(pauseItem);
        emulationMenu.add(stopItem);
        emulationMenu.add(optionsItem);

        aboutMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(emulationMenu);
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
            System.out.println("Path: " + filePath);
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
        }
        if (emulationThread != null && emulationThread.isAlive()) {
            try {
                emulationThread.join();
            } catch (InterruptedException e) {
                JOptionPane.showMessageDialog(this, "Error occurred while halting emulation thread.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Hello, world. Starting Chip-8");
        SwingUtilities.invokeLater(() -> {
            FlatDarkLaf.setup();
            UIManager.put("MenuBar.background", UIManager.getColor("Panel.background"));
            Chip8Emulator emulator = new Chip8Emulator();
            emulator.setVisible(true);
        });
    }
}
