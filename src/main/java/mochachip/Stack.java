package mochachip;

import mochachip.gui.DebugGUI;

public class Stack {
    //Stores return addresses when calling subroutines
    int[] stack;
    int stackPointer;
    private DebugGUI debugGUI;

    public Stack() {
        //mochachip.Stack has a size of 16 bytes
        stack = new int[16];
        //mochachip.Stack pointer initialized at -1 to indicate the stack is empty - 0 means there is at least 1 address
        // on the stack
        stackPointer = -1;
    }

    //Push an address onto the stack to return to when a subroutine is completed
    public void push(int address) {
        if (stackPointer + 1 < stack.length) {
            stackPointer++;
            stack[stackPointer] = address;
            debugGUI.updateStack(stackPointer,address);
        } else {
            //throw new StackOverflowError("ERROR: Stack overflow when trying to push address " + address);
            System.out.println("WARNING: Stack overflow when trying to push address " + address);
        }
    }

    //Pop an address off the stack
    public int pop() {
        if (stackPointer >= 0) {
            int address = stack[stackPointer];
            debugGUI.updateStack(stackPointer, -1);
            stackPointer--;
            return address;

        } else {
            throw new StackUnderflowError("ERROR: Stack underflow.");
        }
    }

    public boolean isEmpty() {
        return stackPointer < 0;
    }

    public boolean isFull() {
        return stackPointer >= 15;

    }

    public void setDebugGUI(DebugGUI debugGUI){
        this.debugGUI = debugGUI;
    }

}

class StackUnderflowError extends RuntimeException{
    public StackUnderflowError(String message){
        super(message);
    }
}