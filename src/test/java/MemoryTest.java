//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class MemoryTest {
//
//    @Test
//    void write() {
//        Memory memory = new Memory();
//        memory.write(memory.firstAvailableAddress, (byte) 0xFF);
//        assertEquals((byte)0xFF, memory.read(memory.firstAvailableAddress));
//    }
//
//    @Test
//    void writeIllegalArgument(){
//        Memory memory = new Memory();
//        assertThrows(IllegalArgumentException.class, () ->
//                memory.write(0x1000, (byte)0x01));
//    }
//
//    @Test
//    void readIllegalArgument(){
//        Memory memory = new Memory();
//        assertThrows(IllegalArgumentException.class, () ->
//                memory.read(0x1005));
//    }
//
//    @Test
//    void read() {
//    }
//}