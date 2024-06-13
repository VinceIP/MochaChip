package mochachip;

import mochachip.gui.DebugGUI;

import java.util.concurrent.ThreadLocalRandom;

public class CPU {
    private volatile boolean running = false;
    Memory memory;
    ProgramCounter programCounter;
    Registers registers;
    Timer timer;
    Input input;
    Stack stack;
    Display display;
    boolean waitingForKeyPress = false;
    int waitingRegister;
    DebugGUI debugGUI;

    public CPU(Input input, Display display) {
        this.display = display;
        this.input = input;
        //reset();
    }

    public void start() {
        running = true;
        long delay = 1; //Delay in ms - roughly 60hz
        while (running) {
            registers.update();
            if (!waitingForKeyPress) cycle();
            else {
                if (input.isAnyKeyPressed()) {
                    registers.variableRegisters[waitingRegister] = (byte) input.getLastKeyPressed();
                    input.resetLastKeyPressed();
                    waitingForKeyPress = false;
                    waitingRegister = -1;
                }
            }
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
    }

    public void stop() {
        running = false;
        display.clearScreen();
    }

    public void togglePause() {
        if (running) running = false;
        else running = true;
    }

    public void reset() {
        this.memory = new Memory();
        this.stack = new Stack();
        this.programCounter = new ProgramCounter();
        this.registers = new Registers();
        this.timer = new Timer();
    }

    public void cycle() {
        int currentInstruction = fetchInstruction();
        decode(currentInstruction);
        //if(debugGUI != null)debugGUI.update();
    }

    //Fetch a 2-byte instruction at address in memory
    public int fetchInstruction() {
        if (programCounter.getCurrentAddress() < 0 || programCounter.getCurrentAddress() >= 4096) {
            throw new IllegalArgumentException("ERROR: Program counter out of bounds.");
        }
        //Unsign and parse instructions to Strings, combine them, parse to ints to avoid annoying bitwise operations
        //Yeah, yeah, i know
        String byte1 = String.format("%02X", memory.read(programCounter.getCurrentAddress()) & 0xFF);
        String byte2 = String.format("%02X", memory.read(programCounter.getCurrentAddress() + 1) & 0xFF);
        int instruction = Integer.parseInt(byte1 + byte2, 16);

        //Do not increment pc if jump
        if (byte1.charAt(0) != '1') {
            programCounter.incrementPC();
        }
        return instruction;
    }

    public void decode(int instruction) {
        //We're going to avoid bitwise operations entirely and use strings
        //to analyze hex values for decoding
        String instrStr = String.format("%04X", instruction);
        String opCode = instrStr.substring(0, 1); //First char indicates the opcode
        String xStr = instrStr.substring(1, 2);
        String yStr = instrStr.substring(2, 3);
        String nStr = instrStr.substring(3, 4);
        String nnStr = instrStr.substring(2);
        String nnnStr = instrStr.substring(1);

        //Parse strings into their values in base 16
        int x = Integer.parseInt(xStr, 16);
        int y = Integer.parseInt(yStr, 16);
        byte n = (byte) (Integer.parseInt(nStr, 16) & 0xFF); //4 bits, cast to unsigned byte
        byte nn = (byte) (Integer.parseInt(nnStr, 16) & 0xFF); //8 bits, cast to unsigned byte
        int nnn = Integer.parseInt(nnnStr, 16); ///nnn is probably 12 bits, so an int is needed

        //System.out.println("Decoding instruction: " + String.format("%04X", instruction) + " at PC: " + String.format("%04X", programCounter.getCurrentAddress()));

        switch (opCode) {

//            nnn or addr - A 12-bit value, the lowest 12 bits of the instruction
//            n or nibble - A 4-bit value, the lowest 4 bits of the instruction
//            x - A 4-bit value, the lower 4 bits of the high byte of the instruction
//            y - A 4-bit value, the upper 4 bits of the low byte of the instruction
//            kk or byte - An 8-bit value, the lowest 8 bits of the instruction

            case "0":
                //00e0 CLS -  clear screen
                if (xStr.equals("0") && yStr.equals("E") && nStr.equals("0")) {
                    cls();
                }

                //00ee RET - return
                else if (xStr.equals("0") && yStr.equals("E") && nStr.equals("E")) {
                    ret();
                }
                break;

            //1nnn JP addr - jump
            case "1":
                //System.out.println("Jumping to address: " + String.format("%04X", nnn));
                jp(nnn);
                break;

            //2nnn CALL addr - call subroutine
            case "2":
                call(nnn);
                break;

            //3xnn SE Vx, byte - Skip next instruction if Vx = nn
            case "3":
                seCompareByte(x, nn);
                break;

            //4xnn SNE Vx, byte - Skip next instruction if Vx != nn
            case "4":
                sne(x, nn);
                break;

            //5xy0 SE Vx, Vy - Skip next instruction if Vx = Vy
            case "5":
                seCompareRegister(x, y);
                break;

            //6xnn LD Vx, byte - Puts value of nn into Vx
            case "6":
                ldByte(x, nn);
                break;

            //7xnn ADD Vx, byte - Set Vx = Vx + nn
            case "7":
                addByte(x, nn);
                break;

            case "8":
                //8xy0 LD Vx, Vy - Set Vx = Vy
                if (nStr.equals("0")) {
                    ldRegister(x, y);

                    // 8xy1 OR Vx, Vy - Set Vx = Vx OR Vy
                } else if (nStr.equals("1")) {
                    logicalOR(x, y);

                    //8xy2 AND Vx, Vy - Set Vx = Vx & Vy
                } else if (nStr.equals("2")) {
                    logicalAND(x, y);

                    // 8xy3 XOR Vx, Vy - Set Vx = Vx ^ Vy
                } else if (nStr.equals("3")) {
                    logicalXOR(x, y);

                    //8xy4 ADD Vx, Vy - Set Vx - Vx + Vy, set VF carry
                } else if (nStr.equals("4")) {
                    addWithCarry(x, y);

                    // 8xy5 SUB Vx, Vy - Set Vx = Vx - Vy
                } else if (nStr.equals("5")) {
                    //System.out.println("Instr: " + instrStr);
                    subWithCarry(x, y);

                    //8xy6 SHR Vx {, Vy} - Set Vx = Vx SHR 1 (shift right)
                    // If the least-significant bit of Vx is 1, then VF is set to 1, otherwise 0. Then Vx is divided by 2.
                } else if (nStr.equals("6")) {
                    bitshiftRight(x);

                    // 8xy7 SUBN Vx, Vy - Set Vx = Vy - Vx
                    // If Vy > Vx, then VF is set to 1, otherwise 0. Then Vx is subtracted from Vy, and the results stored in Vx.
                } else if (nStr.equals("7")) {
                    subWithCarryReverse(x, y);

                    //8xyE SHL Vx {, Vy}
                    //Set Vx = Vx SHL 1.
                    //If the most-significant bit of Vx is 1, then VF is set to 1, otherwise to 0. Then Vx is multiplied by 2.
                } else if (nStr.equals("E")) {
                    bitshiftLeft(x);
                }
                break;

            //9xy0 SNE Vx, Vy - Skip next  instruction if Vx != Vy
            case "9":
                sneRegister(x, y);
                break;

            // Annn LD I, addr - Set I to nnn
            case "A":
                ldI(nnn);
                break;

            // Bnnn JP V0, addr - Jump to location nnn + V0
            case "B":
                jpTo(nnn);
                break;

            // Cxnn RND Vx, byte - Set Vx = random byte AND nn
            case "C":
                rnd(x, nn);
                break;

            //Dxyn  DRW Vx, Vy, nibble
            //mochachip.Display n-byte sprite starting at memory location I at (Vx, Vy), set VF = collision.

            //The interpreter reads n bytes from memory, starting at the address stored in I. These bytes are then displayed
            // as sprites on screen at coordinates (Vx, Vy). Sprites are XORed onto the existing screen. If this causes any
            // pixels to be erased, VF is set to 1, otherwise it is set to 0. If the sprite is positioned so part of it is outside the
            // coordinates of the display, it wraps around to the opposite side of the screen. See instruction 8xy3 for more information
            // on XOR, and section 2.4, mochachip.Display, for more information on the Chip-8 screen and sprites.
            case "D":
                draw(x, y, n);
                break;


            case "E":
                //Ex9E  SKP Vx
                //Skip next instruction if key with the value of Vx is pressed.
                //Checks the keyboard, and if the key corresponding to the value of Vx is currently in the down position, PC is increased by 2.
                if (nStr.equals("E")) {
                    skp(x);

                    //ExA1 - SKNP Vx
                    //Skip next instruction if key with the value of Vx is not pressed.
                    //Checks the keyboard, and if the key corresponding to the value of Vx is currently in the up position, PC is increased by 2.
                } else if (nnStr.equals("A1")) {
                    sknp(x);
                }
                break;


            case "F":
                // Fx07 LD Vx, DT - Set Vx = delay timer value
                if (nnStr.equals("07")) {
                    ldDelayTimer(x);

                    //Fx0A  LD Vx, K
                    //Wait for a key press, store the value of the key in Vx.
                    //All execution stops until a key is pressed, then the value of that key is stored in Vx.
                } else if (nnStr.equals("0A")) {
                    ldKey(x);

                    // Fx15 LD DT, Vx - Set delay timer = Vx
                } else if (nnStr.equals("15")) {
                    ldDelayTimerFromRegister(x);

                    // Fx18 LD ST, Vx - Set sound timer = Vx
                } else if (nnStr.equals("18")) {
                    ldSoundTimer(x);

                    // Fx1E ADD I, Vx - Set I = I + Vx
                } else if (nnStr.equals("1E")) {
                    addI(x);

                    // Fx29 LD F, Vx - Set I = location of sprite digit Vx
                } else if (nnStr.equals("29")) {
                    ldFontDigit(x);

                    //Fx33 LD B, Vx
                    //Store binary-coded decimal representation of Vx in memory locations I, I+1, and I+2.
                    //The interpreter takes the decimal value of Vx, and places the hundreds digit in memory at location in I, the tens digit at location I+1, and the ones digit at location I+2.
                } else if (nnStr.equals("33")) {
                    ldBCD(x);

                    //Fx55 LD [I], Vx
                    //Store registers V0 through Vx in memory starting at location I.
                    //The interpreter copies the values of registers V0 through Vx into memory, starting at the address in I.
                } else if (nnStr.equals("55")) {
                    ldIFor(x);

                    //Fx65 - LD Vx, [I]
                    //Read registers V0 through Vx from memory starting at location I.
                    //The interpreter reads values from memory starting at location I into registers V0 through Vx.
                } else if (nnStr.equals("65")) {
                    ldIForRead(x);
                }
                break;
            default:
                throw new OpcodeUnimplementedException(instruction);


        }
    }

    public void ret() {
        int returnAddress = stack.pop();
        programCounter.jump(returnAddress);
    }

    public void call(int address) {
        stack.push(programCounter.currentAddress);
        programCounter.jump(address);
    }

    //Draw sprite at x, y, with height n
    public void draw(int x, int y, int height) {
        int vx = registers.variableRegisters[x] & 0xFF;
        int vy = registers.variableRegisters[y] & 0xFF;
        registers.setVariableRegister(0xF, 0);
        for (int row = 0; row < height; row++) {
            byte spriteByte = memory.read(registers.indexRegister + row);
            for (int col = 0; col < 8; col++) {
                if ((spriteByte & (0x80 >> col)) != 0) {
                    int displayX = (vx + col) % 64;
                    int displayY = (vy + row) % 32;
                    if (display.getPixelState(displayX, displayY)) {
                        registers.setVariableRegister(0xF, 1);
                    }
                    display.setPixel(displayX, displayY, !display.getPixelState(displayX, displayY));
                }
            }
        }
        display.repaint();
    }


    public void cls() {
        display.clearScreen();
    }

    public void jp(int nnn) {
        programCounter.jump(nnn);
    }

    public void jpTo(int nnn) {
        programCounter.jump(nnn + registers.variableRegisters[0]);
    }

    public void seCompareByte(int x, byte nn) {
        if (registers.variableRegisters[x] == nn) programCounter.incrementPC();
    }

    public void seCompareRegister(int x, int y) {
        if (registers.variableRegisters[x] == registers.variableRegisters[y]) {
            programCounter.incrementPC();
        }
    }

    public void sne(int x, byte nn) {
        if (registers.variableRegisters[x] != nn) programCounter.incrementPC();
    }

    public void sneRegister(int x, int y) {
        if (registers.variableRegisters[x] != registers.variableRegisters[y]) programCounter.incrementPC();
    }

    public void addByte(int x, int nn) {
        //Notes and reminders:
        //& 0xFF masking will clamp variables to unsigned 8-bit values - anything outside range will roll over/under as expected
        //Java will cast all unsigned data to signed every chance it gets
        //All instruction implementations should follow this sort of design strategy of first converting our input values
        //into properly unsigned data within 8 bits.

        int val = nn & 0xFF; //Mask nibble nn to unsigned 8-bit value
        int vx = registers.variableRegisters[x] & 0xFF; //Get the current value of Vx and unsign it
        //Add the two values - we mask the result afterward because Java upcasts data to signed integers before math.
        int result = (vx + val) & 0xFF;
        //We cast the result down to 8-bits, removing any data that is not the least-significant 8 bits
        registers.setVariableRegister(x, result);
    }

    public void addWithCarry(int x, int y) {
        int vx = registers.variableRegisters[x] & 0xFF;
        int vy = registers.variableRegisters[y] & 0xFF;
        int result = (vx + vy) & 0xFF;
        int intResult = vx + vy;
        registers.setVariableRegister(x, result);
        registers.setVariableRegister(0xF, (intResult > 255 ? 1 : 0));

    }

    public void addI(int x) {
        int vx = registers.variableRegisters[x] & 0xFF;
        int i = registers.indexRegister & 0xFFF; //Index register is a 12-bit value!
        registers.indexRegister = (vx + i) & 0xFFF; //Mask the result to 12 bits
        registers.setIndexRegister(vx + i);
    }

    public void subWithCarry(int x, int y) {
        int vx = registers.variableRegisters[x] & 0xFF;
        int vy = registers.variableRegisters[y] & 0xFF;
        int result = vx - vy;

        registers.setVariableRegister(x, (result & 0xFF));
        registers.setVariableRegister(0xF, ((vx >= vy) ? 1 : 0));

    }


    public void subWithCarryReverse(int x, int y) {
        int vx = registers.variableRegisters[x] & 0xFF;
        int vy = registers.variableRegisters[y] & 0xFF;
        int result = vy - vx;

        registers.setVariableRegister(x, (result & 0xFF));
        registers.setVariableRegister(0xF, (vy >= vx ? 1 : 0));

    }

    public void ldByte(int x, int nn) {
        int val = nn & 0xFF;
        registers.setVariableRegister(x, val);
    }

    public void ldRegister(int x, int y) {
        registers.setVariableRegister(x, registers.variableRegisters[y]);
    }

    public void ldI(int nnn) {
        registers.setIndexRegister((nnn & 0xFFF));
    }

    public void ldIFor(int x) {
        if (x == 0) memory.write(registers.indexRegister, registers.variableRegisters[0]);
        else {
            for (int i = 0; i <= x; i++) {
                memory.write(registers.indexRegister + i, registers.variableRegisters[i]);
            }
        }
    }

    public void ldIForRead(int x) {
        if (x == 0) registers.setVariableRegister(0, memory.read(registers.indexRegister));
        else {
            for (int i = 0; i <= x; i++) {
                registers.setVariableRegister(i, memory.read(registers.indexRegister+i));
            }
        }
    }

    public void ldDelayTimer(int x) {
        registers.setVariableRegister(x, registers.delayTimer);
    }

    public void ldDelayTimerFromRegister(int x) {
        //Copying bytes to bytes - probably don't need masking for this
        registers.setDelayTimer(registers.variableRegisters[x]);
    }

    public void ldSoundTimer(int x) {
        registers.setSoundTimer(registers.variableRegisters[x]);
    }

    public void ldKey(int x) {
        waitingForKeyPress = true;
        waitingRegister = x;
    }

    public void ldFontDigit(int x) {
        int digit = registers.variableRegisters[x] & 0xFF;
        registers.setIndexRegister(memory.getAddressOfDigit(digit) & 0xFF);
    }

    public void ldBCD(int x) {
        int dec = registers.variableRegisters[x] & 0xFF;
        int hundreds = (dec / 100) & 0xFF;
        int tens = ((dec / 10) % 10) & 0xFF;
        int ones = (dec % 10) & 0xFF;

        memory.write(registers.indexRegister, (byte) hundreds); // Hundreds place
        memory.write(registers.indexRegister + 1, (byte) tens); // Tens place
        memory.write(registers.indexRegister + 2, (byte) ones); // Ones place
    }

    public void logicalOR(int x, int y) {
        int vx = registers.variableRegisters[x] & 0xFF;
        int vy = registers.variableRegisters[y] & 0xFF;
        registers.setVariableRegister(x, ((vx | vy)) & 0xFF);

    }

    public void logicalAND(int x, int y) {
        int vx = registers.variableRegisters[x] & 0xFF;
        int vy = registers.variableRegisters[y] & 0xFF;
        registers.setVariableRegister(x, ((vx & vy)) & 0xFF);
    }

    public void logicalXOR(int x, int y) {
        int vx = registers.variableRegisters[x] & 0xFF;
        int vy = registers.variableRegisters[y] & 0xFF;
        registers.setVariableRegister(x, (vx ^ vy) & 0xFF);

    }

    public void bitshiftRight(int x) {
        int vx = registers.variableRegisters[x] & 0xFF;
        int lsb = vx & 0x01;
        int shiftedValue = (vx >> 1) & 0xFF;

        registers.setVariableRegister(x, shiftedValue);
        registers.setVariableRegister(0xF, lsb);

    }

    public void bitshiftLeft(int x) {
        int vx = registers.variableRegisters[x] & 0xFF;
        // Store the most significant bit in carry flag (VF)
        byte shiftedValue = (byte) ((vx << 1) & 0xFF);
        byte msb = (byte) (((vx & 0x80) >> 7) & 0xFF);
        registers.setVariableRegister(x, shiftedValue);
        registers.setVariableRegister(0xF, msb);

    }

    public void rnd(int x, int nn) {
        int rand = ThreadLocalRandom.current().nextInt(0, 255) & 0xFF;
        int result = (rand & (nn & 0xFF)) & 0xFF;
        registers.setVariableRegister(x, result);
    }

    public void skp(int x) {
        if (input.isKeyPressed(registers.variableRegisters[x])) {
            programCounter.incrementPC();
        }
    }

    public void sknp(int x) {
        if (!input.isKeyPressed(registers.variableRegisters[x])) {
            programCounter.incrementPC();
        }
    }

    public Memory getMemory() {
        return memory;
    }

    public Registers getRegisters() {
        return registers;
    }

    public void setDebugGUI(DebugGUI debugGUI) {
        //Pass debugGUI to this, plus Registers, which will tell debugGUI when to update register view
        this.debugGUI = debugGUI;
        this.registers.setDebugGUI(debugGUI);
        this.programCounter.setDebugGUI(debugGUI);
    }

    public DebugGUI getDebugGUI() {
        return debugGUI;
    }
}

class OpcodeUnimplementedException extends RuntimeException {
    public OpcodeUnimplementedException(int instruction) {
        System.out.println("ERROR: Instruction " + instruction + " unimplemented.");
    }
}
