package mochachip;

import mochachip.gui.DebugGUI;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.LockSupport;

public class CPU {
    private volatile boolean running = false;

    private int cyclesPerSecond;
    private static final int DEFAULT_SPEED = 500;
    private long frameTime;
    Memory memory;
    ProgramCounter programCounter;
    Registers registers;
    Input input;
    Stack stack;
    Display display;
    boolean waitingForKeyPress = false;
    int waitingRegister;
    DebugGUI debugGUI;
    List<Instruction> instructionList;
    private Instruction currentInstruction;

    public CPU(Input input, Display display) {
        this.display = display;
        this.input = input;
        setCyclesPerSecond(DEFAULT_SPEED);
    }

    public CPU(Input input, Display display, int speed) {
        this.display = display;
        this.input = input;
        setCyclesPerSecond(speed);
    }

    public void start() {
        running = true;
        long timePerCycle = 1_000_000_000L / cyclesPerSecond; //Time each cycle should take up to reach desired speed
        long nextCycleTime = System.nanoTime();

        while (running && debugGUI != null && !debugGUI.isStepMode()) {
            long currentTime = System.nanoTime();
            if (currentTime >= nextCycleTime) {
                registers.update();
                if (!waitingForKeyPress) {
                    prepareCycle();
                    cycle();
                } else {
                    if (input.isAnyKeyPressed()) {
                        registers.variableRegisters[waitingRegister] = (byte) input.getLastKeyPressed();
                        input.resetLastKeyPressed();
                        waitingForKeyPress = false;
                        waitingRegister = -1;
                    }
                }
                nextCycleTime += timePerCycle;
            }

            //long sleepTime = (nextCycleTime - System.nanoTime()) / 1_000_000; // Convert to milliseconds
            long sleepTime = nextCycleTime - currentTime;
            if (sleepTime > 0) {
                //Thread.sleep(sleepTime);
                LockSupport.parkNanos(sleepTime);
            }
        }
    }

    public void stop() {
        running = false;
        display.clearScreen();
    }

    public void togglePause() {
        debugGUI.toggleStepMode();
    }

    public void reset() {
        this.memory = new Memory();
        this.stack = new Stack();
        this.programCounter = new ProgramCounter();
        this.registers = new Registers();
    }

    public void prepareCycle() {
        currentInstruction = fetchInstruction();
        if (debugGUI != null && debugGUI.getFrame().isVisible()) {
            debugGUI.setCurrentInstruction(currentInstruction);
        }
    }

    public void cycle() {
        decode(currentInstruction);
    }

    //Fetch a 2-byte instruction at address in memory
    public Instruction fetchInstruction() {
        if (programCounter.getCurrentAddress() < 0 || programCounter.getCurrentAddress() >= 4095) {
            throw new IllegalArgumentException("ERROR: Program counter out of bounds.");
        }

        //Get a 2 byte instruction
        int byte1 = memory.read(programCounter.getCurrentAddress()) & 0xFF;
        int byte2 = memory.read(programCounter.getCurrentAddress() + 1) & 0xFF;
        //Shift the first byte to the upper 16-bits/left half, then OR with byte2 to combine the 2 bytes
        int byteCode = (byte1 << 8) | byte2;
        Instruction instruction = new Instruction(programCounter.getCurrentAddress(), byteCode);
        programCounter.incrementPC();
        return instruction;
    }

    public void decode(Instruction instruction) {

        //Get each value of the byteCode to be used in decoding
        int opCode = instruction.getOpcode();
        int x = instruction.getNibble1();
        int y = instruction.getNibble2();
        if (x < 0 || x > 0xF || y < 0 || y > 0xF) {
            throw new IllegalArgumentException("Register index out of bounds.");
        }
        int n = instruction.getNibble3();
        int nn = instruction.getNN();
        int nnn = instruction.getNNN();

        switch (opCode) {

//            nnn or addr - A 12-bit value, the lowest 12 bits of the instruction
//            n or nibble - A 4-bit value, the lowest 4 bits of the instruction
//            x - A 4-bit value, the lower 4 bits of the high byte of the instruction
//            y - A 4-bit value, the upper 4 bits of the low byte of the instruction
//            kk or byte - An 8-bit value, the lowest 8 bits of the instruction

            case 0x0:
                switch (nnn) {
                    //00e0 CLS -  clear screen
                    case 0x0E0:
                        cls();
                        break;
                    //00ee RET - return
                    case 0x0EE:
                        ret();
                        return;
                }
                break;

            //1nnn JP addr - jump
            case 0x1:
                jp(nnn);
                return;

            //2nnn CALL addr - call subroutine
            case 0x2:
                call(nnn);
                return;

            //3xnn SE Vx, byte - Skip next instruction if Vx = nn
            case 0x3:
                seCompareByte(x, nn);
                break;

            //4xnn SNE Vx, byte - Skip next instruction if Vx != nn
            case 0x4:
                sne(x, nn);
                break;

            //5xy0 SE Vx, Vy - Skip next instruction if Vx = Vy
            case 0x5:
                seCompareRegister(x, y);
                break;

            //6xnn LD Vx, byte - Puts value of nn into Vx
            case 0x6:
                ldByte(x, nn);
                break;

            //7xnn ADD Vx, byte - Set Vx = Vx + nn
            case 0x7:
                addByte(x, nn);
                break;

            case 0x8:
                switch (n) {
                    //8xy0 LD Vx, Vy - Set Vx = Vy
                    case 0x0:
                        ldRegister(x, y);
                        break;

                    // 8xy1 OR Vx, Vy - Set Vx = Vx OR Vy
                    case 0x1:
                        logicalOR(x, y);
                        break;

                    //8xy2 AND Vx, Vy - Set Vx = Vx & Vy
                    case 0x2:
                        logicalAND(x, y);
                        break;
                    // 8xy3 XOR Vx, Vy - Set Vx = Vx ^ Vy
                    case 0x3:
                        logicalXOR(x, y);
                        break;
                    //8xy4 ADD Vx, Vy - Set Vx - Vx + Vy, set VF carry
                    case 0x4:
                        addWithCarry(x, y);
                        break;

                    // 8xy5 SUB Vx, Vy - Set Vx = Vx - Vy
                    case 0x5:
                        subWithCarry(x, y);
                        break;

                    //8xy6 SHR Vx {, Vy} - Set Vx = Vx SHR 1 (shift right)
                    case 0x6:
                        bitshiftRight(x);
                        break;

                    // 8xy7 SUBN Vx, Vy - Set Vx = Vy - Vx
                    case 0x7:
                        subWithCarryReverse(x, y);
                        break;

                    //8xyE SHL Vx {, Vy}
                    case 0xE:
                        bitshiftLeft(x);
                        break;

                }
                break;

            //9xy0 SNE Vx, Vy - Skip next  instruction if Vx != Vy
            case 0x9:
                sneRegister(x, y);
                break;

            // Annn LD I, addr - Set I to nnn
            case 0xA:
                ldI(nnn);
                break;

            // Bnnn JP V0, addr - Jump to location nnn + V0
            case 0xB:
                jpTo(nnn);
                break;

            // Cxnn RND Vx, byte - Set Vx = random byte AND nn
            case 0xC:
                rnd(x, nn);
                break;

            //Dxyn  DRW Vx, Vy, nibble
            //Display n-byte sprite starting at memory location I at (Vx, Vy), set VF = collision.
            case 0xD:
                draw(x, y, n);
                break;

            case 0xE:
                switch (nn) {
                    //Ex9E  SKP Vx
                    case (byte) 0x9E:
                        skp(x);
                        break;

                    //ExA1 - SKNP Vx
                    case (byte) 0xA1:
                        sknp(x);
                        break;

                }
                break;

            case 0xF:
                switch (nn) {
                    // Fx07 LD Vx, DT - Set Vx = delay timer value
                    case 0x07:
                        ldDelayTimer(x);
                        break;

                    //Fx0A  LD Vx, K
                    case 0x0A:
                        ldKey(x);
                        break;

                    // Fx15 LD DT, Vx - Set delay timer = Vx
                    case 0x15:
                        ldDelayTimerFromRegister(x);
                        break;

                    // Fx18 LD ST, Vx - Set sound timer = Vx
                    case 0x18:
                        ldSoundTimer(x);
                        break;

                    // Fx1E ADD I, Vx - Set I = I + Vx
                    case 0x1E:
                        addI(x);
                        break;

                    // Fx29 LD F, Vx - Set I = location of sprite digit Vx
                    case 0x29:
                        ldFontDigit(x);
                        break;

                    //Fx33 LD B, Vx
                    case 0x33:
                        ldBCD(x);
                        break;

                    //Fx55 LD [I], Vx
                    case 0x55:
                        ldIFor(x);
                        break;

                    //Fx65 - LD Vx, [I]
                    case 0x65:
                        ldIForRead(x);
                }
                break;

            default:
                throw new OpcodeUnimplementedException(instruction.getByteCode());
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
    }


    public void cls() {
        display.clearScreen();
    }

    public void jp(int nnn) {
        programCounter.jump(nnn);
    }

    public void jpTo(int nnn) {
        int vx = registers.variableRegisters[0] & 0xFF;
        programCounter.jump(nnn + vx);
    }

    public void seCompareByte(int x, int nn) {
        int vx = registers.variableRegisters[x] & 0xFF;
        int val = nn & 0xFF;
        if (vx == val) programCounter.incrementPC();
    }

    public void seCompareRegister(int x, int y) {
        int vx = registers.variableRegisters[x] & 0xFF;
        int vy = registers.variableRegisters[y] & 0xFF;
        if (vx == vy) {
            programCounter.incrementPC();
        }
    }

    public void sne(int x, int nn) {
        int vx = registers.variableRegisters[x] & 0xFF;
        int val = nn & 0xFF;
        if (vx != val) programCounter.incrementPC();
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
        //int i = registers.indexRegister & 0xFFF; //Index register is a 12-bit value!
        registers.setIndexRegister(registers.getIndexRegister() + vx);
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
                registers.setVariableRegister(i, memory.read(registers.indexRegister + i));
            }
        }
    }

    public void ldDelayTimer(int x) {
        int val = registers.delayTimer & 0xFF;
        registers.setVariableRegister(x, val);
    }

    public void ldDelayTimerFromRegister(int x) {
        int vx = registers.variableRegisters[x] & 0xFF;
        registers.setDelayTimer(vx);
    }

    public void ldSoundTimer(int x) {
        int vx = registers.variableRegisters[x] & 0xFF;
        registers.setSoundTimer(vx);
    }

    public void ldKey(int x) {
        waitingForKeyPress = true;
        waitingRegister = x;
    }

    public void ldFontDigit(int x) {
        int digit = registers.variableRegisters[x] & 0xFF;
        registers.setIndexRegister(memory.getAddressOfDigit(digit));
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
        int rand = ThreadLocalRandom.current().nextInt(0, 256) & 0xFF;
        int val = nn & 0xFF;
        int result = (rand & val) & 0xFF;
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

    public int getCyclesPerSecond() {
        return cyclesPerSecond;
    }

    public void setCyclesPerSecond(int cyclesPerSecond) {
        this.cyclesPerSecond = cyclesPerSecond;
    }

    public void setDebugGUI(DebugGUI debugGUI) {
        //Pass debugGUI to this, plus Registers, which will tell debugGUI when to update register view
        this.debugGUI = debugGUI;
        this.registers.setDebugGUI(debugGUI);
        this.stack.setDebugGUI(debugGUI);
        this.programCounter.setDebugGUI(debugGUI);
    }

    public long getFrameTime() {
        return frameTime;
    }

    public DebugGUI getDebugGUI() {
        return debugGUI;
    }
}

class OpcodeUnimplementedException extends RuntimeException {
    public OpcodeUnimplementedException(int instruction) {
        super("ERROR: Instruction " + instruction + " unimplemented.");
    }
}
