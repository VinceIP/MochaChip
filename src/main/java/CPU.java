public class CPU {
    //handles all CPU operations
    Memory memory;
    ProgramCounter programCounter;
    Registers registers;
    Timer timer;

    public CPU() {
        this.memory = new Memory();
        this.programCounter = new ProgramCounter();
        this.registers = new Registers();
        this.timer = new Timer();
    }
}
