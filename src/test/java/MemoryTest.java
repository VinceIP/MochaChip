//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class MemoryTest {
//
//    @Test
//    void write() {
//        mochachip.Memory memory = new mochachip.Memory();
//        memory.write(memory.firstAvailableAddress, (byte) 0xFF);
//        assertEquals((byte)0xFF, memory.read(memory.firstAvailableAddress));
//    }
//
//    @Test
//    void writeIllegalArgument(){
//        mochachip.Memory memory = new mochachip.Memory();
//        assertThrows(IllegalArgumentException.class, () ->
//                memory.write(0x1000, (byte)0x01));
//    }
//
//    @Test
//    void readIllegalArgument(){
//        mochachip.Memory memory = new mochachip.Memory();
//        assertThrows(IllegalArgumentException.class, () ->
//                memory.read(0x1005));
//    }
//
//    @Test
//    void read() {
//    }
//}