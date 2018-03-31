package com.jbatista.batatinha.emulator;

import com.jbatista.batatinha.MainApp;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.scene.image.ImageView;

public class Chip8 {

    private File program;
    private final Random random = new Random();
    private final short cpuSpeed;
    private final ReadOnlyIntegerWrapper cycle = new ReadOnlyIntegerWrapper();

    // CPU, memory, registers, font
    private char opcode;
    private final List<Character> memory = new ArrayList<>(4096);
    private final List<Character> v = new ArrayList<>(16);
    private char i;
    private char programCounter;
    // hardcoded font
    private final char[] font = {
        0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
        0x20, 0x60, 0x20, 0x20, 0x70, // 1
        0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
        0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
        0x90, 0x90, 0xF0, 0x10, 0x10, // 4
        0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
        0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
        0xF0, 0x10, 0x20, 0x40, 0x40, // 7
        0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
        0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
        0xF0, 0x90, 0xF0, 0x90, 0x90, // A
        0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
        0xF0, 0x80, 0x80, 0x80, 0xF0, // C
        0xE0, 0x90, 0x90, 0x90, 0xE0, // D
        0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
        0xF0, 0x80, 0xF0, 0x80, 0x80 // F
    };

    // jump to routine stack
    private final List<Character> stack = new ArrayList<>(16);
    private char stackPointer;

    // timers
    private ReadOnlyIntegerWrapper soundTimer = new ReadOnlyIntegerWrapper(0);
    private ReadOnlyIntegerWrapper delayTimer = new ReadOnlyIntegerWrapper(0);

    // auxiliary
    private ScheduledFuture timer60Hz;
    private ScheduledFuture timerCPU;
    private final Display display;
    private final Input input = new Input();
    private final Map<Character, Consumer<Character>> opcodesMap = new HashMap<>();
    private char decodedOpcode;

    public Chip8(short cpuSpeed, File program, ImageView screen, int scale) {
        this.program = program;
        this.cpuSpeed = cpuSpeed;
        this.display = new Display(screen, v, scale);

        // <editor-fold defaultstate="collapsed" desc="hardcoded opcode functions, double click to expand (Netbeans)">
        // debug
        opcodesMap.put((char) 0xF000, this::emptyRegion);

        // chip-8 opcodes
        opcodesMap.put((char) 0x00E0, this::dispClear);
        opcodesMap.put((char) 0x00EE, this::returnSubRoutine);
        opcodesMap.put((char) 0x1000, this::goTo);
        opcodesMap.put((char) 0x2000, this::callSubroutine);
        opcodesMap.put((char) 0x3000, this::skipVxEqNN);
        opcodesMap.put((char) 0x4000, this::skipVxNotEqNN);
        opcodesMap.put((char) 0x5000, this::skipVxEqVy);
        opcodesMap.put((char) 0x6000, this::setVx);
        opcodesMap.put((char) 0x7000, this::addNNtoVx);
        opcodesMap.put((char) 0x8000, this::setVxTovY);
        opcodesMap.put((char) 0x8001, this::setVxToVxOrVy);
        opcodesMap.put((char) 0x8002, this::setVxToVxAndVy);
        opcodesMap.put((char) 0x8003, this::setVxToVxXorVy);
        opcodesMap.put((char) 0x8004, this::addVxToVyCarry);
        opcodesMap.put((char) 0x8005, this::subtractVxToVyCarry);
        opcodesMap.put((char) 0x8006, this::shiftRightVyToVx);
        opcodesMap.put((char) 0x8007, this::subtractVyAndVx2nd);
        opcodesMap.put((char) 0x800E, this::shiftLeftVyToVx);
        opcodesMap.put((char) 0x9000, this::skipVxNotEqVy);
        opcodesMap.put((char) 0xA000, this::setI);
        opcodesMap.put((char) 0xB000, this::goToV0);
        opcodesMap.put((char) 0xC000, this::rand);
        opcodesMap.put((char) 0xD000, this::draw);
        opcodesMap.put((char) 0xE09E, this::skipVxEqKey);
        opcodesMap.put((char) 0xE0A1, this::skipVxNotEqKey);
        opcodesMap.put((char) 0xF007, this::vxToDelay);
        opcodesMap.put((char) 0xF00A, this::waitKey);
        opcodesMap.put((char) 0xF015, this::setDelayTimer);
        opcodesMap.put((char) 0xF018, this::setSoundTimer);
        opcodesMap.put((char) 0xF01E, this::addsVxToI);
        opcodesMap.put((char) 0xF029, this::setIToSpriteInVx);
        opcodesMap.put((char) 0xF033, this::bcd);
        opcodesMap.put((char) 0xF055, this::dump);
        opcodesMap.put((char) 0xF065, this::load);

        // TODO superchip opcodes
        // </editor-fold>
    }

    public void start() throws IOException {
        if (timer60Hz != null) {
            timer60Hz.cancel(true);
        }
        if (timerCPU != null) {
            timerCPU.cancel(true);
        }

        cycle.set(1);
        v.clear();
        stack.clear();
        memory.clear();
        for (int i = 0; i < 16; i++) {
            v.add((char) 0);
            stack.add((char) 0);
        }
        for (int i = 0; i < 4096; i++) {
            memory.add((char) 0);
        }

        i = 0;
        programCounter = 512;
        stackPointer = 0;
        soundTimer.set(0);
        delayTimer.set(0);
        display.clear();

        // load font
        for (int i = 0; i < 80; i++) {
            memory.set(i, font[i]);
        }

        // load program
        final FileInputStream fileInputStream = new FileInputStream(program);
        int data;
        int index = 0;
        while ((data = fileInputStream.read()) != -1) {
            memory.set(index + 512, (char) data);
            index++;
        }
        fileInputStream.close();

        // 60Hz timer
        timer60Hz = MainApp.executor.scheduleWithFixedDelay(() -> {
            timerTick();
        }, 1000 / 60, 1000 / 60, TimeUnit.MILLISECONDS);

        // CPU timer
        timerCPU = MainApp.executor.scheduleWithFixedDelay(() -> {
            cpuTick();
        }, 1000 / cpuSpeed, 1000 / cpuSpeed, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        timer60Hz.cancel(true);
        timerCPU.cancel(true);
    }

    // 500Hz ~ 1000Hz
    private void cpuTick() {
        opcode = (char) (memory.get(programCounter) << 8 | memory.get(programCounter + 1));
        decodedOpcode = (char) (opcode & 0xF000);

        // special cases
        if (opcode == 0xEE) {
            decodedOpcode = 0x00EE;
        } else if (opcode == 0xE0) {
            decodedOpcode = 0x00E0;
        } else if (decodedOpcode == 0x8000) {
            decodedOpcode = (char) (opcode & 0xF00F);
        } else if ((decodedOpcode == 0xE000) || (decodedOpcode == 0xF000)) {
            decodedOpcode = (char) (opcode & 0xF0FF);
        }

        if (opcodesMap.containsKey(decodedOpcode)) {
            opcodesMap.get(decodedOpcode).accept(opcode);
        } else {
            System.out.println("UNKNOWN OPCODE - 0x" + Integer.toHexString(decodedOpcode).toUpperCase());
        }

        changeCycle();
    }

    // 60Hz
    private void timerTick() {
        if (soundTimer.get() > 0) {
            soundTimer.set(soundTimer.get() - 1);
        }

        if (delayTimer.get() > 0) {
            delayTimer.set(delayTimer.get() - 1);
        }
    }

    private void changeCycle() {
        if (cycle.get() > cpuSpeed) {
            cycle.set(1);
        } else {
            cycle.set(cycle.get() + 1);
        }
    }

    public ReadOnlyIntegerProperty getCycle() {
        return cycle.getReadOnlyProperty();
    }

    public List<Character> getV() {
        return v;
    }

    public List<Character> getMemory() {
        return memory;
    }

    public char getOpcode() {
        return opcode;
    }

    public char getDecodedOpcode() {
        return decodedOpcode;
    }

    public char getI() {
        return i;
    }

    public char getProgramCounter() {
        return programCounter;
    }

    public List<Character> getStack() {
        return stack;
    }

    public char getStackPointer() {
        return stackPointer;
    }

    public ReadOnlyIntegerProperty getSoundTimer() {
        return soundTimer.getReadOnlyProperty();
    }

    public ReadOnlyIntegerProperty getDelayTimer() {
        return delayTimer.getReadOnlyProperty();
    }

    public void toggleKey(String key) {
        input.toggleKey((char) Integer.parseInt("0x" + key));
    }

    // <editor-fold defaultstate="collapsed" desc="opcode methods, double click to expand (Netbeans)">
    // debug
    private void printOpcode(char arg) {
        System.out.println("0x" + Integer.toHexString(arg).toUpperCase() + '\n');
        programCounter += 2;
    }

    private void emptyRegion(char arg) {
        System.out.println("EMPTY MEMORY ADDRESS REACHED - 0x" + Integer.toHexString(programCounter));
        programCounter += 2;
    }

    // 0000
    private void call(char opc) {
        // not used (?)
    }

    // 00E0
    private void dispClear(char opc) {
        System.out.println("DESIP CLEAR - 0x" + Integer.toHexString(opc));

        display.clear();
        programCounter += 2;
    }

    // 00EE
    private void returnSubRoutine(char opc) {
        System.out.println("RETURN - 0x" + Integer.toHexString(opc));

        programCounter = stack.get(stackPointer - 1);
        stackPointer--;
    }

    // 1NNN
    private void goTo(char opc) {
        System.out.println("GOTO - 0x" + Integer.toHexString(opc));

        programCounter = (char) (opc & 0x0FFF);
    }

    // 2NNN
    private void callSubroutine(char opc) {
        System.out.println("CALL - 0x" + Integer.toHexString(opc));

        stack.set(stackPointer, programCounter);
        stackPointer++;
        programCounter = (char) (opc & 0x0FFF);
    }

    // 3XNN
    private void skipVxEqNN(char opc) {
        System.out.println("SKIP IF VX==NN - 0x" + Integer.toHexString(opc));

        if (v.get((opc & 0x0F00) >> 8) == (opc & 0x00FF)) {
            programCounter += 4;
        } else {
            programCounter += 2;
        }
    }

    // 4XNN
    private void skipVxNotEqNN(char opc) {
        System.out.println("SKIP IF VX!=NN - 0x" + Integer.toHexString(opc));

        if (v.get((opc & 0x0F00) >> 8) != (opc & 0x00FF)) {
            programCounter += 4;
        } else {
            programCounter += 2;
        }
    }

    // 5XY0
    private void skipVxEqVy(char opc) {
        System.out.println("SKIP IF VX==VY - 0x" + Integer.toHexString(opc));

        if (v.get((opc & 0x0F00) >> 8) == v.get((opc & 0x00F0) >> 4)) {
            programCounter += 4;
        } else {
            programCounter += 2;
        }
    }

    // 6XNN
    private void setVx(char opc) {
        System.out.println("SET VX - 0x" + Integer.toHexString(opc));

        v.set((opc & 0x0F00) >> 8, (char) (opc & 0x00FF));
        programCounter += 2;
    }

    // 7XNN
    private void addNNtoVx(char opc) {
        System.out.println("VX+=NN - 0x" + Integer.toHexString(opc));

        v.set((opc & 0x0F00) >> 8, (char) (v.get((opc & 0x0F00) >> 8) + (opc & 0x00FF)));
        programCounter += 2;
    }

    // 8XY0
    private void setVxTovY(char opc) {
        System.out.println("VX=VY - 0x" + Integer.toHexString(opc));

        v.set((opc & 0x0F00) >> 8, v.get((opc & 0x00F0) >> 4));
        programCounter += 2;
    }

    // 8XY1
    private void setVxToVxOrVy(char opc) {
        System.out.println("VX=VX|VY - 0x" + Integer.toHexString(opc));

        v.set((opc & 0x0F00) >> 8, (char) (v.get((opc & 0x0F00) >> 8) | v.get((opc & 0x00F0) >> 4)));
        programCounter += 2;
    }

    // 8XY2
    private void setVxToVxAndVy(char opc) {
        System.out.println("VX=VX&VY - 0x" + Integer.toHexString(opc));

        v.set((opc & 0x0F00) >> 8, (char) (v.get((opc & 0x0F00) >> 8) & v.get((opc & 0x00F0) >> 4)));
        programCounter += 2;
    }

    // 8XY3
    private void setVxToVxXorVy(char opc) {
        System.out.println("VX=VX^VY - 0x" + Integer.toHexString(opc));

        v.set((opc & 0x0F00) >> 8, (char) (v.get((opc & 0x0F00) >> 8) ^ v.get((opc & 0x00F0) >> 4)));
        programCounter += 2;
    }

    // 8XY4
    private void addVxToVyCarry(char opc) {
        System.out.println("(!) VX+=VY CARRY - 0x" + Integer.toHexString(opc));

        if ((v.get((opc & 0x0F00) >> 8) + v.get((opc & 0x00F0) >> 4)) > 255) {
            v.set(0xF, (char) 1);
        } else {
            v.set(0xF, (char) 0);
        }
        v.set((opc & 0x0F00) >> 8, (char) (v.get((opc & 0x0F00) >> 8) + v.get((opc & 0x00F0) >> 4)));
        programCounter += 2;
    }

    // 8XY5
    private void subtractVxToVyCarry(char opc) {
        System.out.println("(!) VX-=VY CARRY - 0x" + Integer.toHexString(opc));

        if (v.get((opc & 0x0F00) >> 8) > v.get((opc & 0x00F0) >> 4)) {
            v.set(0xF, (char) 1);
        } else {
            v.set(0xF, (char) 0);
        }
        v.set((opc & 0x00F0) >> 4, (char) (v.get((opc & 0x00F0) >> 4) - v.get((opc & 0x0F00) >> 8)));
        programCounter += 2;
    }

    // 8XY6
    private void shiftRightVyToVx(char opc) {
        System.out.println("VX=VY>>1 - 0x" + Integer.toHexString(opc));

        v.set(0xF, (char) (v.get((opc & 0x00F0) >> 4) & 0x7));
        v.set((opc & 0x0F00) >> 8, (char) (v.get((opc & 0x00F0) >> 4) >> 1));
        programCounter += 2;
    }

    // 8XY7
    private void subtractVyAndVx2nd(char opc) {
        System.out.println("(!) VX=VX-VY - 0x" + Integer.toHexString(opc));

        if (v.get((opc & 0x0F00) >> 8) > v.get((opc & 0x00F0) >> 4)) {
            v.set(0xF, (char) 1);
        } else {
            v.set(0xF, (char) 0);
        }
        v.set((opc & 0x0F00) >> 8, (char) (v.get((opc & 0x00F0) >> 4) - v.get((opc & 0x0F00) >> 8)));
        programCounter += 2;
    }

    // 8XYE
    private void shiftLeftVyToVx(char opc) {
        System.out.println("VX=VY<<1 - 0x" + Integer.toHexString(opc));
        
        v.set(0xF, (char) (v.get((opc & 0x00F0) >> 4) >> 0x7));
        v.set((opc & 0x0F00) >> 8, (char) (v.get((opc & 0x00F0) >> 4) << 1));
        programCounter += 2;
    }

    // 9XY0
    private void skipVxNotEqVy(char opc) {
        if (v.get((opc & 0x0F00) >> 8) != v.get((opc & 0x00F0) >> 4)) {
            programCounter += 4;
        } else {
            programCounter += 2;
        }
    }

    // ANNN
    private void setI(char opc) {
        System.out.println("SET I - 0x" + Integer.toHexString(opc));
        
        i = (char) (opc & 0x0FFF);
        programCounter += 2;
    }

    // BNNN
    private void goToV0(char opc) {
        System.out.println("GOTO V0+NNN - 0x" + Integer.toHexString(opc));
        
        programCounter = (char) (v.get(0x0) + (opc & 0x0FFF));
    }

    // CXNN
    private void rand(char opc) {
        System.out.println("RAND - 0x" + Integer.toHexString(opc));
        
        v.set((opc & 0x0F00) >> 8, (char) (random.nextInt(255) & (opc & 0x00FF)));
        programCounter += 2;
    }

    // DXYN
    private void draw(char opc) {
        System.out.println("DRAW - 0x" + Integer.toHexString(opc));
        
        final char[] lines = new char[opc & 0x000F];
        for (int index = 0; index < (opc & 0x000F); index++) {
            lines[index] = memory.get(i + index);
        }

        display.draw(opc, lines);
        programCounter += 2;
    }

    // EX9E
    private void skipVxEqKey(char opc) {
        System.out.println("SKIP IF VX==KEY - 0x" + Integer.toHexString(opc));
        
        if (input.isPressed((char) ((opc & 0x0F00) >> 8))) {
            programCounter += 4;
        } else {
            programCounter += 2;
        }
    }

    // EXA1
    private void skipVxNotEqKey(char opc) {
        System.out.println("SKIP IF VX!=KEY - 0x" + Integer.toHexString(opc));
        
        if (!input.isPressed((char) ((opc & 0x0F00) >> 8))) {
            programCounter += 4;
        } else {
            programCounter += 2;
        }
    }

    // FX07
    private void vxToDelay(char opc) {
        System.out.println("VX=DELAY - 0x" + Integer.toHexString(opc));
        
        v.set((opc & 0x0F00) >> 8, (char) delayTimer.get());
        programCounter += 2;
    }

    // FX0A
    private void waitKey(char opc) {
        System.out.println("WAIT KEY - 0x" + Integer.toHexString(opc));
        
        input.resetPressResgister();

        // blocks with an infinite loop
        while (!input.pressRegistred()) {
        }

        v.set((opc & 0x0F00) >> 8, input.getLastKey());
        input.resetPressResgister();
        programCounter += 2;
    }

    // FX15
    private void setDelayTimer(char opc) {
        System.out.println("SET DELAY - 0x" + Integer.toHexString(opc));
        
        delayTimer.set((opc & 0x0F00) >> 8);
        programCounter += 2;
    }

    // FX18
    private void setSoundTimer(char opc) {
        System.out.println("SET TIMER - 0x" + Integer.toHexString(opc));
        
        soundTimer.set((opc & 0x0F00) >> 8);
        programCounter += 2;
    }

    // FX1E
    private void addsVxToI(char opc) {
        System.out.println("I+=VX - 0x" + Integer.toHexString(opc));
        
        i += v.get((opc & 0x0F00) >> 8);
        programCounter += 2;
    }

    // FX29
    private void setIToSpriteInVx(char opc) {
        System.out.println("I=SPR VX - 0x" + Integer.toHexString(opc));
        
        i = (char) (v.get((opc & 0x0F00) >> 8) * 5);
        programCounter += 2;
    }

    // FX33
    private void bcd(char opc) {
        System.out.println("BCD - 0x" + Integer.toHexString(opc));
        v.set(i, (char) (v.get((opc & 0x0F00) >> 8) / 100));
        v.set(i + 1, (char) ((v.get((opc & 0x0F00) >> 8) / 10) % 10));
        v.set(i + 2, (char) ((v.get((opc & 0x0F00) >> 8) % 100) % 10));
        programCounter += 2;
    }

    // FX55
    private void dump(char opc) {
        System.out.println("DUMP - 0x" + Integer.toHexString(opc));
        
        for (int vi = 0; vi <= ((opc & 0x0F00) >> 8); vi++) {
            memory.set(i + vi, v.get(vi));
        }
        programCounter += 2;
    }

    // FX65
    private void load(char opc) {
        System.out.println("LOAD - 0x" + Integer.toHexString(opc));
        
        for (int vi = 0; vi <= ((opc & 0x0F00) >> 8); vi++) {
            v.set(i, memory.get(i + vi));
        }
        programCounter += 2;
    }
    // TODO superchip opcodes
    // </editor-fold>
}
