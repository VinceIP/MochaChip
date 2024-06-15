package mochachip;

import mochachip.gui.DebugGUI;

public class ProgramCounter {
    //Used to point at current instruction in memory
    int currentAddress;
    private DebugGUI debugGUI;

    public ProgramCounter() {
        currentAddress = 0x200;
    }


    public void incrementPC() {
        currentAddress += 2;
        debugGUI.updateRegister(DebugGUI.RegisterType.PC, currentAddress);
    }


    public void jump(int address) {
        currentAddress = address;
    }

    public int getCurrentAddress() {
        return currentAddress;
    }

    public void setDebugGUI(DebugGUI debugGUI) {
        this.debugGUI = debugGUI;
    }

}
