package mochachip;

public class Instruction {
    private final int byteCode;
    private int address;
    private byte opcode;
    private byte nibble1;
    private byte nibble2;
    private byte nibble3;
    private boolean valid;
    private String description;

    public Instruction(int address, int byteCode) {
        this.address = address;
        this.byteCode = byteCode;
        parse();
    }

    private void parse() {
        this.opcode = (byte) ((byteCode & 0xF000) >> 12);
        this.nibble1 = (byte) ((byteCode & 0x0F00) >> 8);
        this.nibble2 = (byte) ((byteCode & 0x00F0) >> 4);
        this.nibble3 = (byte) (byteCode & 0x000F);
    }

    public byte getOpcode() {
        return opcode;
    }

    public byte getNibble1() {
        return nibble1;
    }

    public byte getNibble2() {
        return nibble2;
    }

    public byte getNibble3() {
        return nibble3;
    }

    public int getByteCode() {
        return byteCode;
    }

    public byte getNN() {
        return (byte) (byteCode & 0x00FF);
    }

    public int getNNN() {
        return byteCode & 0x0FFF;
    }

    public int getAddress() {
        return address;
    }

    public String getDescription(){
        return description;
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public String toString() {
        return String.format("%04X: %01X%01X%01X%01X", address, opcode, nibble1, nibble2, nibble3);
    }

    //Returns true if the instruction's bytecode is a valid CHIP-8 instruction
    //Will need to modify for CHIP-8 extensions
    //It's possible that a series of bytes meant to be sprite data could be read as valid instructions, but this should
    //be okay enough - validation will stop the first time we encounter invalid instructions
    public boolean validateInstruction() {
        byte opCode = getOpcode();
        byte x = getNibble1();
        byte y = getNibble2();
        byte n = getNibble3();
        int nn = getNN();
        int nnn = getNNN();

        switch (opCode) {
            case 0x0:
                if (nnn == 0x0E0) {
                    description = "CLS";
                    return true;
                } else if (nnn == 0x0EE) {
                    description = "RET";
                    return true;
                }
                break;
            case 0x1: // JP addr
                description = String.format("JP %03X", nnn);
                return true;
            case 0x2: // CALL addr
                description = String.format("CALL %03X", nnn);
                return true;
            case 0xA: // LD I, addr
                description = String.format("LD I, %03X", nnn);
                return true;
            case 0xB: // JP V0, addr
                description = String.format("JP V0, %03X", nnn);
                return true; // Instructions with nnn
            case 0x3: // SE Vx, byte
                description = String.format("SE V%01X, %02X", x, nn& 0xFF);
                return true;
            case 0x4: // SNE Vx, byte
                description = String.format("SNE V%01X, %02X", x, nn& 0xFF);
                return true;
            case 0x6: // LD Vx, byte
                description = String.format("LD V%01X, %02X", x, nn& 0xFF);
                return true;
            case 0x7: // ADD Vx, byte
                description = String.format("ADD V%01X, %02X", x, nn& 0xFF);
                return true;
            case 0xC: // RND Vx, byte
                description = String.format("RND V%01X, %02X", x, nn & 0xFF);
                return true; // Instructions with nn
            case 0x5: // SE Vx, Vy
                if (n == 0) {
                    description = String.format("SE V%01X, V%01X", x, y);
                    return true;
                }
                break;
            case 0x8: // Logical and arithmetic operations
                switch (n) {
                    case 0x0:
                        description = String.format("LD V%01X, V%01X", x, y);
                        return true;
                    case 0x1:
                        description = String.format("OR V%01X, V%01X", x, y);
                        return true;
                    case 0x2:
                        description = String.format("AND V%01X, V%01X", x, y);
                        return true;
                    case 0x3:
                        description = String.format("XOR V%01X, V%01X", x, y);
                        return true;
                    case 0x4:
                        description = String.format("ADD V%01X, V%01X", x, y);
                        return true;
                    case 0x5:
                        description = String.format("SUB V%01X, V%01X", x, y);
                        return true;
                    case 0x6:
                        description = String.format("SHR V%01X", x);
                        return true;
                    case 0x7:
                        description = String.format("SUBN V%01X, V%01X", x, y);
                        return true;
                    case 0xE:
                        description = String.format("SHL V%01X", x);
                        return true;
                }
                break;
            case 0x9: // SNE Vx, Vy
                if (n == 0) {
                    description = String.format("SNE V%01X, V%01X", x, y);
                    return true;
                }
                break;
            case 0xD: // DRW Vx, Vy, nibble
                description = String.format("DRW V%01X, V%01X, %01X", x, y, n);
                return true; // Draw instruction
            case 0xE:
                if ((nn & 0xFF) == 0x9E) {
                    description = String.format("SKP V%01X", x);
                    return true;
                } else if ((nn& 0xFF) == 0xA1) {
                    description = String.format("SKNP V%01X", x);
                    return true;
                }
                break;
            case 0xF:
                switch (nn) {
                    case 0x07: // LD Vx, DT
                        description = String.format("LD V%01X, DT", x);
                        return true;
                    case 0x0A: // LD Vx, K
                        description = String.format("LD V%01X, K", x);
                        return true;
                    case 0x15: // LD DT, Vx
                        description = String.format("LD DT, V%01X", x);
                        return true;
                    case 0x18: // LD ST, Vx
                        description = String.format("LD ST, V%01X", x);
                        return true;
                    case 0x1E: // ADD I, Vx
                        description = String.format("ADD I, V%01X", x);
                        return true;
                    case 0x29: // LD F, Vx
                        description = String.format("LD F, V%01X", x);
                        return true;
                    case 0x33: // LD B, Vx
                        description = String.format("LD B, V%01X", x);
                        return true;
                    case 0x55: // LD [I], Vx
                        description = String.format("LD [I], V%01X", x);
                        return true;
                    case 0x65: // LD V%01X, [I]
                        description = String.format("LD V%01X, [I]", x);
                        return true;
                }
                break;
        }
        return false; // If no valid instruction matched
    }

}
