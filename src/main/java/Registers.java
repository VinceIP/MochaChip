public class Registers {

    //One 16-bit index register "I", points to locations in memory
    //A stack for 16-bit addresses, makes calls to subroutines and returns from them
    //VF - 16 8-bit variable registers - general purpose hex variables. V0 - VF representing 0-15
    //  Also used a flag register, or boolean register. ex 0 or 1 for use as a carry flag.

    byte delayTimer;
    byte soundTimer;
    int indexRegister;
    Stack stack;
    byte[] variableRegisters;

    public Registers(){
        delayTimer = 0;
        soundTimer = 0;
        indexRegister = 0x00;
        variableRegisters = new byte[16];
    }

    //Called 60 times per second
    public void update(){
        //Timers always decrementing if not zero
        if(delayTimer > 0) delayTimer--;
        if(soundTimer > 0) soundTimer--;
    }
}
