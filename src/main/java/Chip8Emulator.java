public class Chip8Emulator {
    public static void main(String[] args) {
        System.out.println("Hello, world. Starting Chip-8");
        CPU cpu = new CPU();
        cpu.start();
    }
}
