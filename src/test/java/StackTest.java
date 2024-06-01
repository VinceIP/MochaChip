import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StackTest {

    @Test
    void testPush() {
        Stack stack = new Stack();
        stack.push(0x200);
        assertEquals(0x200, stack.pop());
    }

    @Test
    void testPushMultiple() {
        Stack stack = new Stack();
        stack.push(0xF11);
        stack.push(0xB02);
        stack.push(0x00F);
        stack.push(0x015);
        stack.pop();
        assertEquals(0x00F, stack.pop());
    }

    @Test
    void testPop() {
        Stack stack = new Stack();
        stack.push(0x200);
        int address = stack.pop();
        assertEquals(0x200, address);
        assertTrue(stack.isEmpty());
    }

    @Test
    void testPopMultiple() {
        Stack stack = new Stack();
        stack.push(0x200);
        stack.push(0x210);
        stack.push(0x220);
        stack.pop();
        int address = stack.pop();
        assertEquals(0x210, address);
        assertFalse(stack.isEmpty());
    }

    @Test
    void testStackOverflow() {
        Stack stack = new Stack();
        //Fill up the stack
        for (int i = 0; i < 16; i++) {
            stack.push(i);
        }
        //Push another address
        assertThrows(StackOverflowError.class, () -> {
            stack.push(0x200);
        });
    }

    @Test
    void testStackUnderflow() {
        Stack stack = new Stack();
        assertThrows(StackUnderflowError.class, stack::pop);
    }

    @Test
    void testIsEmpty() {
        Stack stack = new Stack();
        assertEquals(true, stack.isEmpty());
        stack.push(0x200);
        assertEquals(false, stack.isEmpty());
    }

    @Test
    void testIsFull() {
        Stack stack = new Stack();
        assertFalse(stack.isFull());
        for (int i = 0; i < 16; i++) {
            stack.push(i);
        }
        assertTrue(stack.isFull());
    }


}
