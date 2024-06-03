public class CPU {
    //handles all CPU operations
    Memory memory;
    ProgramCounter programCounter;
    Registers registers;
    Timer timer;
    Input input;
    Stack stack;
    Display display;

    public CPU(Display display) {
        this.memory = new Memory();
        this.stack = new Stack();
        this.programCounter = new ProgramCounter();
        this.registers = new Registers();
        this.timer = new Timer();
        this.input = new Input();
        this.display = display;
    }

    public void start() {
        memory.initialize();
        memory.loadChip8File();
        long delay = 30; //Delay in ms - roughly 60hz
        while (true) {
            cycle();
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void cycle() {
        int currentInstruction = fetchInstruction();
        decode(currentInstruction);
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

        programCounter.incrementPC();
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

        System.out.println("Decoding instruction: " + String.format("%04X", instruction) + " at PC: " + String.format("%04X", programCounter.getCurrentAddress()));

        switch (opCode) {

//            nnn or addr - A 12-bit value, the lowest 12 bits of the instruction
//            n or nibble - A 4-bit value, the lowest 4 bits of the instruction
//            x - A 4-bit value, the lower 4 bits of the high byte of the instruction
//            y - A 4-bit value, the upper 4 bits of the low byte of the instruction
//            kk or byte - An 8-bit value, the lowest 8 bits of the instruction

            case "0":
                //00e0 CLS -  clear screen
                if (xStr.equals("0") && yStr.equals("E") && nStr.equals("0")) {
                    display.clearScreen();
                }

                //00ee RET - return
                else if (xStr.equals("0") && yStr.equals("E") && nStr.equals("E")) {
                    returnFromSubroutine();
                }
                break;

            //1nnn JP addr - jump
            case "1":
                //System.out.println("Jumping to address: " + String.format("%04X", nnn));
                programCounter.jump(nnn);
                break;

            //2nnn CALL addr - call subroutine
            case "2":
                callSubroutine(nnn);
                break;

            //3xnn SE Vx, byte - Skip next instruction if Vx = nn
            case "3":
                if (registers.variableRegisters[x] == nn) programCounter.incrementPC();
                break;

            //4xnn SNE Vx, byte - Skip next instruction if Vx != nn
            case "4":
                if (registers.variableRegisters[x] != nn) programCounter.incrementPC();
                break;

            //5xy0 SE Vx, Vy - Skip next instruction if Vx = Vy
            case "5":
                if (registers.variableRegisters[x] == registers.variableRegisters[y]) {
                    programCounter.incrementPC();
                }

                //6xnn LD Vx, byte - Puts value of nn into Vx
            case "6":
                registers.variableRegisters[x] = nn;
                break;

            //7xnn ADD Vx, byte - Set Vx = Vx + nn
            case "7":
                registers.variableRegisters[x] += nn;
                break;

            case "8":
                //8xy0 LD Vx, Vy - Set Vx = Vy
                if (nStr.equals("0")) {
                    registers.variableRegisters[x] = registers.variableRegisters[y];

                    // 8xy1 OR Vx, Vy - Set Vx = Vx OR Vy
                } else if (nStr.equals("1")) {
                    registers.variableRegisters[x] = (byte) ((byte) ((registers.variableRegisters[x] | registers.variableRegisters[y])) & 0xFF);

                    //8xy2 AND Vx, Vy - Set Vx = Vx & Vy
                } else if (nStr.equals("2")) {
                    registers.variableRegisters[x] = (byte) ((byte) ((registers.variableRegisters[x] & registers.variableRegisters[y])) & 0xFF);

                    // 8xy3 XOR Vx, Vy - Set Vx = Vx ^ Vy
                } else if (nStr.equals("3")) {
                    registers.variableRegisters[x] = (byte) ((byte) ((registers.variableRegisters[x] ^ registers.variableRegisters[y])) & 0xFF);

                    //8xy4 ADD Vx, Vy - Set Vx - Vx + Vy, set VF carry
                } else if (nStr.equals("4")) {
                    int sum = (registers.variableRegisters[x] & 0xFF) + (registers.variableRegisters[y] & 0xFF);
                    if (sum > 255) registers.variableRegisters[0xF] = 1;
                    else registers.variableRegisters[0xF] = 0;
                    registers.variableRegisters[x] = (byte) sum;

                    // 8xy5 SUB Vx, Vy - Set Vx = Vx - Vy
                } else if (nStr.equals("5")) {
                    if (registers.variableRegisters[x] > registers.variableRegisters[y])
                        registers.variableRegisters[0xF] = 1;
                    else registers.variableRegisters[0xF] = 0;
                    int difference = (registers.variableRegisters[x] & 0xFF) - (registers.variableRegisters[y] & 0xFF);
                    registers.variableRegisters[x] = (byte) difference;

                    //8xy6 SHR Vx {, Vy} - Set Vx = Vx SHR 1 (shift right)
                    // If the least-significant bit of Vx is 1, then VF is set to 1, otherwise 0. Then Vx is divided by 2.
                } else if (nStr.equals("6")) {

                } else if (nStr.equals("7")) {

                } else if (nStr.equals("E")) {

                }
                break;
            //9xy0 SNE Vx, Vy - Skip next  instruction if Vx != Vy
            case "9":
                if (registers.variableRegisters[x] != registers.variableRegisters[y]) programCounter.incrementPC();
                break;
            //Set index - sets I to nnn
            case "A":
                registers.indexRegister = nnn;
                break;
            //Draw
            case "D":
                drawSprite(x, y, n);
                break;
            default:
                throw new OpcodeUnimplementedException(instruction);


        }
    }

    private void returnFromSubroutine() {
        int returnAddress = stack.pop();
        //System.out.println("Returning to address: " + String.format("%04X", returnAddress));
        programCounter.jump(returnAddress);
    }

    private void callSubroutine(int address) {
        //System.out.println("Calling subroutine at: " + String.format("%04X", address));
        stack.push(programCounter.currentAddress);
        programCounter.jump(address);
    }

    //Draw sprite at x, y, with height n
    private void drawSprite(int x, int y, int n) {
        //System.out.println("Draw sprite at V" + x + ", V" + y + " at height " + n);
        int vx = (registers.variableRegisters[x] & 0xFF) % 64;
        int vy = (registers.variableRegisters[y] & 0xFF) % 64;
        registers.variableRegisters[0xF] = 0;
        //for n rows
        for (int i = 0; i < n; i++) {
            int spriteData = memory.read(registers.indexRegister + i);
            //for each 8 pixels/bits in a row
            for (int j = 0; j < 8; j++) {
                //is this bit set?
                if ((spriteData & (0x80 >> j)) != 0) {
                    int displayX = (vx + j) % 64;
                    int displayY = (vy + i) % 32;

                    if (display.getPixelState(displayX, displayY)) {
                        registers.variableRegisters[0xF] = 1; //Set VF to 1 if collision
                        display.setPixel(displayX, displayY, false);
                    } else {
                        display.setPixel(displayX, displayY, true);
                    }
                }
            }

        }
        display.updateDisplay();

    }


    public void execute() {

    }
}

class OpcodeUnimplementedException extends RuntimeException {
    public OpcodeUnimplementedException(int instruction) {
        System.out.println("ERROR: Instruction " + instruction + " unimplemented.");
    }
}
