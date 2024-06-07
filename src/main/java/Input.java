import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Input implements KeyListener {
    boolean[] keys;
    CPU cpu;


    int lastKeyPressed = 16;

    public Input() {
        reset();
    }

    public void reset(){
        keys = new boolean[17];

    }

    public void pressKey(int key) {
        keys[key] = true;
        lastKeyPressed = key;
    }

    public void releaseKey(int key) {
        keys[key] = false;
    }

    public boolean isKeyPressed(int key) {
        return keys[key];
    }

    public int waitForKeyPress() {
        return 0;
    }


    @Override
    public void keyTyped(KeyEvent e) {
        //
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = mapKey(e.getKeyCode());
        pressKey(key);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = mapKey(e.getKeyCode());
        releaseKey(key);

    }

    //Default Chip-8 hex keypad layout mapped to QWERTY layout
    private int mapKey(int keyCode) {
        return switch (keyCode) {
            case KeyEvent.VK_1 -> 0x1;
            case KeyEvent.VK_2 -> 0x2;
            case KeyEvent.VK_3 -> 0x3;
            case KeyEvent.VK_4 -> 0xC;
            case KeyEvent.VK_Q -> 0x4;
            case KeyEvent.VK_W -> 0x5;
            case KeyEvent.VK_E -> 0x6;
            case KeyEvent.VK_R -> 0xD;
            case KeyEvent.VK_A -> 0x7;
            case KeyEvent.VK_S -> 0x8;
            case KeyEvent.VK_D -> 0x9;
            case KeyEvent.VK_F -> 0xE;
            case KeyEvent.VK_Z -> 0xA;
            case KeyEvent.VK_X -> 0x0;
            case KeyEvent.VK_C -> 0xB;
            case KeyEvent.VK_V -> 0xF;
            //Any other key is not valid, set it to the top of the array
            default -> 16;
        };
    }

    public int getLastKeyPressed() {
        return lastKeyPressed;
    }

    public void resetLastKeyPressed() {
        this.lastKeyPressed = 16;
    }

    public boolean isAnyKeyPressed() {
        for (boolean key : keys) {
            if (key) return true;
        }
        return false;
    }

}
