public class CPU {
    //handles all CPU operations
    Memory memory;
    ProgramCounter programCounter;
    Registers registers;
    Timer timer;
    Input input;

    public CPU() {
        this.memory = new Memory();
        this.programCounter = new ProgramCounter();
        this.registers = new Registers();
        this.timer = new Timer();
        this.input = new Input();
        start();
    }

    public void start(){
        memory.loadChip8File();
    }

    public void cycle(){
        fetch();
        decode();
        execute();
    }

    public void fetch(){

    }

    public void decode(){

    }

    public void execute(){

    }
}
