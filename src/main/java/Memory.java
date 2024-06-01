public class Memory {
    //Chip-8 has direct access to up to 4KB of RAM

    byte[] memory;
    final int firstAvailableAddress = 0x200;
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
        System.out.println("Initiating memory.");
        //4 kilobytes of memory represented as a Byte array
        memory = new byte[4096];
        reserveMemoryForInterpreter();
        loadFontToMemory();
        //printMemoryMap();
    }

    public void write(int address, byte value) {
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
        //Set each byte up to F11 to a non-zero value to reserve it for the Chip-8 interpreter
        //This is not needed at all, just for me for now
        for (int i = 0x0; i < firstAvailableAddress; i++) {
            write(i, (byte) 0x5);
        }
    }

    private void loadFontToMemory() {
        //050–09F
        //5 bytes per char
        int startAddress = 0x50;
        for (int i = 0; i < fontData.length; i++) {
            write(startAddress + i, fontData[i]);
        }
    }

    private void printMemoryMap() {
        for (int i = 0; i < memory.length; i++) {
            if (i % 40 == 0) System.out.print("\n");
            System.out.print(String.format("%02X ", memory[i]));
        }
    }

    public static int unsignedToBytes(byte b) {
        return b & 0xFF;
    }
}
