package mochachip;

public class Instruction {
    private final int byteCode;
    private byte opcode;
    private byte nibble1;
    private byte nibble2;
    private byte nibble3;

    public Instruction(int byteCode){
        this.byteCode = byteCode;
        parse();
    }

    private void parse(){
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

    public int getByteCode(){
        return byteCode;
    }

    public byte getNN(){
        return (byte) (byteCode & 0x00FF);
    }

    public int getNNN(){
        return byteCode & 0x0FFF;
    }

    @Override
    public String toString(){
        return String.format("%01X%01X%01X%01X", opcode, nibble1, nibble2, nibble3);
    }
}
