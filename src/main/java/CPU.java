public class CPU {
    //handles all CPU operations
    Memory memory;
    ProgramCounter programCounter;
    Registers registers;
    Timer timer;
    Input input;
    Stack stack;

    public CPU() {
        this.memory = new Memory();
        this.stack = new Stack();
        this.programCounter = new ProgramCounter();
        this.registers = new Registers();
        this.timer = new Timer();
        this.input = new Input();
    }

    public void start() {
        memory.initialize();
        memory.loadChip8File();
        long delay = 16; //Delay in ms - roughly 60hz
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
        int n = Integer.parseInt(nStr, 16);
        int nn = Integer.parseInt(nnStr, 16);
        int nnn = Integer.parseInt(nnnStr, 16);

        System.out.println("Decoding instruction: " + String.format("%04X", instruction) + " at PC: " + String.format("%04X", programCounter.getCurrentAddress()));

        switch (opCode) {

            case "0":
                //00E0 - clear screen
                if (xStr.equals("0") && yStr.equals("E") && nStr.equals("0")) {
                    // Clear the screen
                    //System.out.println("Screen cleared.");
                }
                //00EE - return
                else if (xStr.equals("0") && yStr.equals("E") && nStr.equals("E")) {
                    returnFromSubroutine();
                }
                break;
            //1NNN - jump
            case "1":
                //System.out.println("Jumping to address: " + String.format("%04X", nnn));
                programCounter.jump(nnn);
                break;
            //2NNN - call subroutine
            case "2":
                callSubroutine(nnn);
                break;
            //Set - set register vx to nn
            case "6":
                registers.variableRegisters[x] = (byte) nn;
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

    private void clearScreen() {
        //System.out.println("cleared the screen.");
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
    }


    public void execute() {

    }
}

class OpcodeUnimplementedException extends RuntimeException {
    public OpcodeUnimplementedException(int instruction) {
        System.out.println("ERROR: Instruction " + instruction + " unimplemented.");
    }
}
