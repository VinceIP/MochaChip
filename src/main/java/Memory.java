import java.io.IOException;
import java.io.InputStream;

public class Memory {
    //Chip-8 has direct access to up to 4KB of RAM

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
        memory = new byte[4096];
    }

    public void initialize() {
        //System.out.println("Initializing memory.");
        reserveMemoryForInterpreter();
        loadFontToMemory();
    }

    public void write(int address, byte value) {
        value = unsignByte(value);
        if (address >= 0 && address < memory.length) {
            memory[address] = value;
        } else {
            throw new IllegalArgumentException("Error with request to write memory at " + address
                    + " with value " + value + ". Memory address out of bounds.");
        }
    }

    public byte read(int address) {
        if (address >= 0 && address < memory.length) {
            return memory[address];
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
            write(fontDataAddress + i, fontData[i]);
        }
    }

    public void loadChip8File() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test/keypadtest.ch8")) {
            if (inputStream == null) {
                throw new IOException("Chip 8 ROM not found in resources folder.");
            }
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            loadProgramDataToMemory(buffer);
            System.out.println("Data loaded. Here's the map:");
            //printMemoryMap(0x200, 0x210);
        } catch (IOException e) {
            System.out.println("Couldn't load Chip 8 ROM: " + e.getMessage());
        }
    }

    public void loadProgramDataToMemory(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            write(firstAvailableAddress + i, data[i]);
        }
    }

    public void printMemoryMap() {
        for (int i = 0; i <= 4096; i++) {
            System.out.printf("Memory at 0x%X: 0x%02X%n", i, memory[i] & 0xFF);
        }
    }

    //Return an address in memory corresponding to a hex digit
    public int getAddressOfDigit(int digit) {
        return fontDataAddress + (digit * 5);
    }

    public static byte unsignByte(byte b) {
        return (byte) (b & 0xFF);
    }
}