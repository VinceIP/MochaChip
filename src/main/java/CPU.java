import java.util.concurrent.ThreadLocalRandom;

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
        long delay = 3; //Delay in ms - roughly 60hz
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
                break;

            //6xnn LD Vx, byte - Puts value of nn into Vx
            case "6":
                registers.variableRegisters[x] = (byte) (nn & 0xFF);
                break;

            //7xnn ADD Vx, byte - Set Vx = Vx + nn
            case "7":
                registers.variableRegisters[x] += (byte) (nn & 0xFF);
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
                    //Store least significant bit in carry flag
                    registers.variableRegisters[0xF] = (byte) (registers.variableRegisters[x] & 0x01);
                    registers.variableRegisters[x] = (byte) ((registers.variableRegisters[x] & 0xFF) >> 1);

                    // 8xy7 SUBN Vx, Vy - Set Vx = Vy - Vx
                    // If Vy > Vx, then VF is set to 1, otherwise 0. Then Vx is subtracted from Vy, and the results stored in Vx.
                } else if (nStr.equals("7")) {
                    if (registers.variableRegisters[y] > registers.variableRegisters[x])
                        registers.variableRegisters[0xF] = 1;
                    else registers.variableRegisters[0xF] = 0;
                    int difference = (registers.variableRegisters[y] & 0xFF) - (registers.variableRegisters[x] & 0xFF);
                    registers.variableRegisters[x] = (byte) difference;

                } else if (nStr.equals("E")) {
                    //Store most significant bit in carry flag
                    registers.variableRegisters[0xF] = (byte) ((registers.variableRegisters[x] & 0x80) >> 7);
                    registers.variableRegisters[x] = (byte) ((registers.variableRegisters[x] & 0xFF) << 1);
                }
                break;

            //9xy0 SNE Vx, Vy - Skip next  instruction if Vx != Vy
            case "9":
                if (registers.variableRegisters[x] != registers.variableRegisters[y]) programCounter.incrementPC();
                break;

            // Annn LD I, addr - Set I to nnn
            case "A":
                registers.indexRegister = nnn;
                break;

            // Bnnn JP V0, addr - Jump to location nnn + V0
            case "B":
                programCounter.jump(nnn + registers.variableRegisters[0x0]);
                break;

            // Cxnn RND Vx, byte - Set Vx = random byte AND nn
            case "C":
                registers.variableRegisters[x] = (byte) ((byte) ((byte) ThreadLocalRandom.current().nextInt(0, 255) & 0xFF) & nn);
                break;

            //Draw
            case "D":
                drawSprite(x, y, n);
                break;

            case "F":
                // Fx07 LD Vx, DT - Set Vx = delay timer value
                if (nnStr.equals("07")) {
                    registers.variableRegisters[x] = registers.delayTimer;

                    // Fx0A LD Vx, K - Wait for key press, store value of key in Vx
                } else if (nnStr.equals("0A")) {

                    // Fx15 LD DT, Vx - Set delay timer = Vx
                } else if (nnStr.equals("15")) {
                    registers.delayTimer = registers.variableRegisters[x];

                    // Fx18 LD ST, Vx - Set sound timer = Vx
                } else if (nnStr.equals("18")) {
                    registers.soundTimer = registers.variableRegisters[x];

                    // Fx1E ADD I, Vx - Set I = I + Vx
                } else if (nnStr.equals("1E")) {
                    registers.indexRegister += registers.variableRegisters[x];

                    // Fx29 LD F, Vx - Set I = location of sprite digit Vx
                } else if (nnStr.equals("29")) {
                    registers.indexRegister = memory.getAddressOfDigit(registers.variableRegisters[x]);

                    //Fx33 LD B, Vx
                    //Store binary-coded decimal representation of Vx in memory locations I, I+1, and I+2.
                    //The interpreter takes the decimal value of Vx, and places the hundreds digit in memory at location in I, the tens digit at location I+1, and the ones digit at location I+2.
                } else if (nnStr.equals("33")) {
                    int dec = registers.variableRegisters[x] & 0xFF;
                    int hundreds = dec / 100;
                    int tens = (dec / 10) % 10;
                    int ones = dec % 10;
                    System.out.printf("Storing BCD of %d: [%d, %d, %d] at addresses I=%04X, I+1=%04X, I+2=%04X%n",
                            dec, hundreds, tens, ones, registers.indexRegister, registers.indexRegister + 1, registers.indexRegister + 2);
                    memory.write(registers.indexRegister, (byte) hundreds); // Hundreds place
                    memory.write(registers.indexRegister + 1, (byte) tens); // Tens place
                    memory.write(registers.indexRegister + 2, (byte) ones); // Ones place

                    //Fx55 LD [I], Vx
                    //Store registers V0 through Vx in memory starting at location I.
                    //The interpreter copies the values of registers V0 through Vx into memory, starting at the address in I.
                } else if (nnStr.equals("55")) {
                    if (x == 0) memory.write(registers.indexRegister, registers.variableRegisters[0]);
                    else {
                        for (int i = 0; i <= x; i++) {
                            memory.write(registers.indexRegister + i, registers.variableRegisters[i]);
                        }
                    }

                    //Fx65 - LD Vx, [I]
                    //Read registers V0 through Vx from memory starting at location I.
                    //The interpreter reads values from memory starting at location I into registers V0 through Vx.
                } else if (nnStr.equals("65")) {
                    if (x == 0) registers.variableRegisters[0] = memory.read(registers.indexRegister);
                    else {
                        for (int i = 0; i <= x; i++) {
                            registers.variableRegisters[i] = memory.read(registers.indexRegister + i);
                        }
                    }
                }
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
