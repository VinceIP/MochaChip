//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//public class StackTest {
//
//    @Test
//    void testPush() {
//        mochachip.Stack stack = new mochachip.Stack();
//        stack.push(0x200);
//        assertEquals(0x200, stack.pop());
//    }
//
//    @Test
//    void testPushMultiple() {
//        mochachip.Stack stack = new mochachip.Stack();
//        stack.push(0xF11);
//        stack.push(0xB02);
//        stack.push(0x00F);
//        stack.push(0x015);
//        stack.pop();
//        assertEquals(0x00F, stack.pop());
//    }
//
//    @Test
//    void testPop() {
//        mochachip.Stack stack = new mochachip.Stack();
//        stack.push(0x200);
//        int address = stack.pop();
//        assertEquals(0x200, address);
//        assertTrue(stack.isEmpty());
//    }
//
//    @Test
//    void testPopMultiple() {
//        mochachip.Stack stack = new mochachip.Stack();
//        stack.push(0x200);
//        stack.push(0x210);
//        stack.push(0x220);
//        stack.pop();
//        int address = stack.pop();
//        assertEquals(0x210, address);
//        assertFalse(stack.isEmpty());
//    }
//
//    @Test
//    void testStackOverflow() {
//        mochachip.Stack stack = new mochachip.Stack();
//        //Fill up the stack
//        for (int i = 0; i < 16; i++) {
//            stack.push(i);
//        }
//        //Push another address
//        assertThrows(StackOverflowError.class, () -> {
//            stack.push(0x200);
//        });
//    }
//
//    @Test
//    void testStackUnderflow() {
//        mochachip.Stack stack = new mochachip.Stack();
//        assertThrows(mochachip.StackUnderflowError.class, stack::pop);
//    }
//
//    @Test
//    void testIsEmpty() {
//        mochachip.Stack stack = new mochachip.Stack();
//        assertEquals(true, stack.isEmpty());
//        stack.push(0x200);
//        assertEquals(false, stack.isEmpty());
//    }
//
//    @Test
//    void testIsFull() {
//        mochachip.Stack stack = new mochachip.Stack();
//        assertFalse(stack.isFull());
//        for (int i = 0; i < 16; i++) {
//            stack.push(i);
//        }
//        assertTrue(stack.isFull());
//    }
//
//
//}
