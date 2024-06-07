import java.awt.*;

public class ColorTheme {

    public static enum Chip8Color {
        MONOCHROME,
        SOFT_MONOCHROME,
        ICE,
        OLIVE,
        LAVA,
        GRAPE,
        DESERT,
        PASTEL
    }

    private Color background;
    private Color foreground;

    public ColorTheme() {
        setTheme(Chip8Color.MONOCHROME);
    }

    public Color getBackground() {
        return background;
    }

    public void setTheme(Chip8Color chip8Color) {
        switch (chip8Color) {
            case MONOCHROME:
                setBackground(Color.BLACK);
                setForeground(Color.WHITE);
                break;
            case SOFT_MONOCHROME:
                setBackground(new Color(0x343a40));
                setForeground(new Color(0xdee2e6));
                break;
            case ICE:
                setBackground(new Color(0x023e8a));
                setForeground(new Color(0x90e0ef));
                break;
            case OLIVE:
                setBackground(new Color(0x718355));
                setForeground(new Color(0xcfe1b9));
                break;
            case LAVA:
                setBackground(new Color(0x370617));
                setForeground(new Color(0xfaa307));
                break;
            case GRAPE:
                setBackground(new Color(0x240046));
                setForeground(new Color(0xc77dff));
                break;
            case DESERT:
                setBackground(new Color(0xa47e1b));
                setForeground(new Color(0xffe169));
                break;
            case PASTEL:
                setBackground(new Color(0xf686bd));
                setForeground(new Color(0xf1e4f3));
        }
    }

    public void setBackground(Color background) {
        this.background = background;
    }

    public Color getForeground() {
        return foreground;
    }

    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }
}
