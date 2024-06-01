public class Display {
    //64x32 px for Chip-8
    //128x64 px for SUPER-CHIP
    //Monochrome
    boolean[][] display;
    final int displayWidth = 64;
    final int displayHeight = 32;

    public Display() {
        display = new boolean[displayWidth][displayHeight];
    }

    public void update(){

    }
}
