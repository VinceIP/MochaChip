package mochachip;

import javax.swing.*;
import java.awt.*;

public class Display extends JPanel {
    JFrame frame;
    final int displayWidth = 64;
    final int displayHeight = 32;
    boolean[][] display;
    public Input input;
    private int scaleFactor = 16;
    public int windowWidth = displayWidth * scaleFactor;
    public int windowHeight = displayHeight * scaleFactor;
    ColorTheme colorTheme;

    public Display(Input input) {
        this.input = input;
        this.colorTheme = new ColorTheme();
        setPreferredSize(new Dimension((displayWidth * scaleFactor), (displayHeight * scaleFactor)));
        reset();
    }

    public void reset(){
        display = new boolean[displayWidth][displayHeight];
        addKeyListener(input);
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        g.setColor(colorTheme.getBackground());
        g.fillRect(0,0, getWidth(), getHeight());
        g.setColor(colorTheme.getForeground());
        for (int y = 0; y < displayHeight; y++) {
            for (int x = 0; x < displayWidth; x++) {
                if (display[x][y]) {
                    g.fillRect(x * scaleFactor, y * scaleFactor, scaleFactor, scaleFactor); // Scale pixels
                }
            }
        }
    }

    public void resizeDisplay(){
        setPreferredSize(new Dimension((displayWidth * scaleFactor), (displayHeight * scaleFactor)));
        revalidate();
        repaint();
    }

    public void adjustSize(int scaleFactor){
        this.scaleFactor = scaleFactor;
        resizeDisplay();
    }

    public void updateDisplay() {
        repaint();
    }

    public void setColorTheme(ColorTheme.Chip8Color theme){
        colorTheme.setTheme(theme);
        updateDisplay();
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
