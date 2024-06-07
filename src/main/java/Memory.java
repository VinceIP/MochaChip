import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Memory {
    //Chip-8 has direct access to up to 4KB of RAM
    private static final int MEMORY_SIZE = 4096;
    private static final int FONT_DATA_START_ADDRESS = 0x50;
    private static final int PROGRAM_START_ADDRESS = 0x200;

    byte[] memory;
    final int firstAvailableAddress = 0x200;
    final int fontDataAddress = 0x50;
    private final byte[] fontData = {
            (byte) 0xF0, (byte) 0x90, (byte) 0x90, (byte) 0x90, (byte) 0xF0, // 0
            (byte) 0x20, (byte) 0x60, (byte) 0x20, (byte) 0x20, (byte) 0x70, // 1
            (byte) 0xF0, (byte) 0x10, (byte) 0xF0, (byte) 0x80, (byte) 0xF0, // 2
            (byte) 0xF0, (byte) 0x10, (byte) 0xF0, (byte) 0x10, (byte) 0xF0, // 3
            (byte) 0x90, (byte) 0x90, (byte) 0xF0, (byte) 0x10, (byte) 0x10, // 4
            (byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x10, (byte) 0xF0, // 5
            (byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x90, (byte) 0xF0, // 6
            (byte) 0xF0, (byte) 0x10, (byte) 0x20, (byte) 0x40, (byte) 0x40, // 7
            (byte) 0xF0, (byte) 0x90, (byte) 0xF0, (byte) 0x90, (byte) 0xF0, // 8
            (byte) 0xF0, (byte) 0x90, (byte) 0xF0, (byte) 0x10, (byte) 0xF0, // 9
            (byte) 0xF0, (byte) 0x90, (byte) 0xF0, (byte) 0x90, (byte) 0x90, // A
            (byte) 0xE0, (byte) 0x90, (byte) 0xE0, (byte) 0x90, (byte) 0xE0, // B
            (byte) 0xF0, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0xF0, // C
            (byte) 0xE0, (byte) 0x90, (byte) 0x90, (byte) 0x90, (byte) 0xE0, // D
            (byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x80, (byte) 0xF0, // E
            (byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x80, (byte) 0x80  // F
    };

    public Memory() {
        reset();
    }

    public void initialize() {
        //System.out.println("Initializing memory.");
        reserveMemoryForInterpreter();
        loadFontToMemory();
    }

    public void reset() {
        memory = new byte[MEMORY_SIZE];
        initialize();
    }

    public void write(int address, byte value) {
        byte val = (byte) (value & 0xFF);
        if (address >= 0 && address < MEMORY_SIZE) {
            memory[address] = val;
            //System.out.printf("Memory write at 0x%X: 0x%02X%n", address, value & 0xFF);
        } else {
            throw new IllegalArgumentException("Error with request to write memory at " + address
                    + " with value " + value + ". Memory address out of bounds.");
        }
    }

    public byte read(int address) {
        if (address >= 0 && address < memory.length) {
            return (byte) (memory[address] & 0xFF);
        } else {
            throw new IllegalArgumentException("Error with request to read memory at " + address
                    + ". Memory address out of bounds.");
        }
    }

    private void reserveMemoryForInterpreter() {
        for (int i = 0x0; i < firstAvailableAddress; i++) {
            write(i, (byte) 0x5);
        }
    }

    private void loadFontToMemory() {
        for (int i = 0; i < fontData.length; i++) {
            write(FONT_DATA_START_ADDRESS + i, fontData[i]);
        }
        //System.out.println("Font data loaded into memory:");
        for (int i = 0; i < fontData.length; i++) {
            //System.out.printf("Memory at 0x%X: 0x%02X%n", fontDataAddress + i, memory[fontDataAddress + i] & 0xFF);
        }
    }

    public boolean loadChip8File(String filePath) {
        reset();
        try (InputStream inputStream = new FileInputStream(filePath)) {
            byte[] buffer = new byte[inputStream.available()];
            int bytesRead = inputStream.read(buffer);
            if (bytesRead != buffer.length) {
                System.out.println("Warning: Could not read the entire file");
            }
            inputStream.read(buffer);
            loadProgramDataToMemory(buffer);
            printMemoryMap();
            return true;
        } catch (IOException e) {
            System.out.println("Couldn't load Chip 8 ROM: " + e.getMessage());
            return false;
        }
    }

    public void loadProgramDataToMemory(byte[] data) {
        System.arraycopy(data, 0, memory, PROGRAM_START_ADDRESS, data.length);
        //System.out.println("Program data loaded into memory:");
//        for (int i = 0; i < data.length; i++) {
//            System.out.printf("Memory at 0x%X: 0x%02X%n", PROGRAM_START_ADDRESS + i, memory[PROGRAM_START_ADDRESS + i] & 0xFF);
//        }
    }

    public void printMemoryMap() {
        for (int i = 0; i < 4096; i++) {
            if (i % 10 == 0) {
                System.out.printf("%nAddress 0x%03X: ", i);
            }
            System.out.printf("0x%02X ", memory[i] & 0xFF);
        }
    }

    //Return an address in memory corresponding to a hex digit
    public int getAddressOfDigit(int digit) {
        return FONT_DATA_START_ADDRESS + (digit * 5);
    }

    public static byte unsignByte(byte b) {
        return (byte) (b & 0xFF);
    }
}