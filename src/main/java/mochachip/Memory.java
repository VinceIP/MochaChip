package mochachip;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Memory {
    //Chip-8 has direct access to up to 4KB of RAM
    public static final int MEMORY_SIZE = 4096;
    public static final int FONT_DATA_START_ADDRESS = 0x50;
    public static final int PROGRAM_START_ADDRESS = 0x200;

    private byte[] memory;
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
            write(FONT_DATA_START_ADDRESS + i, fontData[i]);
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
            loadProgramDataToMemory(buffer);
            //printMemoryMap();
            return true;
        } catch (IOException e) {
            System.out.println("Couldn't load Chip 8 ROM: " + e.getMessage());
            return false;
        }
    }

    public void loadProgramDataToMemory(byte[] data) {
        System.arraycopy(data, 0, memory, PROGRAM_START_ADDRESS, data.length);
    }

    public void printMemoryMap() {
        for (int i = 0; i < 4096; i++) {
            if (i % 10 == 0) {
                System.out.printf("%nAddress 0x%03X: ", i);
            }
            System.out.printf("0x%02X ", memory[i] & 0xFF);
        }
    }

    public byte[] getMemoryArray(){
        return memory;
    }

    //Return an address in memory corresponding to a hex digit
    public int getAddressOfDigit(int digit) {
        return FONT_DATA_START_ADDRESS + (digit * 5);
    }

}