public class ProgramCounter {
    //Used to point at current instruction in memory
    int currentAddress;

    public ProgramCounter() {
        currentAddress = 0x200;
    }


    public void incrementPC() {
        currentAddress += 2;
    }

    public int getCurrentAddress() {
        return currentAddress;
    }

    public void jump(int address){
        currentAddress = address;
    }
}
