import javax.swing.*;
import java.awt.*;

public class Display {
    private JFrame frame;
    private DisplayPanel panel;
    private final int displayWidth = 64;
    private final int displayHeight = 32;
    boolean[][] display;

    public Display() {
        display = new boolean[displayWidth][displayHeight];
        panel = new DisplayPanel();

        // Create GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Chip-8 Emulator");
            panel = new DisplayPanel();
            frame.add(panel);
            frame.setSize(displayWidth * 14 + 30, displayHeight * 14 +30); // Scale size
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }

    private class DisplayPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            for (int y = 0; y < displayHeight; y++) {
                for (int x = 0; x < displayWidth; x++) {
                    if (display[x][y]) {
                        g.fillRect(x * 14, y * 14, 14, 14); // Scale pixels
                    }
                }
            }
        }
    }

    public void updateDisplay() {
        panel.repaint();
    }

    public void setPixel(int x, int y, boolean enabled){
        display[x][y] = enabled;
    }

    public boolean getPixelState(int x, int y){
        return display[x][y];
    }

    public void clearScreen(){
        for (int i = 0; i < displayHeight; i++) {
            for (int j = 0; j < displayWidth; j++) {
                setPixel(j,i,false);
            }
        }
    }
}
