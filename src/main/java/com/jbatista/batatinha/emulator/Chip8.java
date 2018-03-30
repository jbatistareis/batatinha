package com.jbatista.batatinha.emulator;

import com.jbatista.batatinha.MainApp;
import com.jbatista.batatinha.emulator.Input.Key;
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

    public Chip8(short cpuSpeed, File program, ImageView screen) {
        this.program = program;
        this.cpuSpeed = cpuSpeed;
        this.display = new Display(screen, v);

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
        opcodesMap.put((char) 0xE09E, this::printOpcode);
        opcodesMap.put((char) 0xE0A1, this::printOpcode);
        opcodesMap.put((char) 0xF007, this::printOpcode);
        opcodesMap.put((char) 0xF00A, this::printOpcode);
        opcodesMap.put((char) 0xF015, this::printOpcode);
        opcodesMap.put((char) 0xF018, this::printOpcode);
        opcodesMap.put((char) 0xF01E, this::addsVxToI);
        opcodesMap.put((char) 0xF029, this::printOpcode);
        opcodesMap.put((char) 0xF033, this::printOpcode);
        opcodesMap.put((char) 0xF055, this::printOpcode);
        opcodesMap.put((char) 0xF065, this::printOpcode);

        // TODO superchip opcodes
        // </editor-fold>
    }

    public void start() throws IOException {
        cycle.set(1);

        if (timer60Hz != null) {
            timer60Hz.cancel(true);
        }
        if (timerCPU != null) {
            timerCPU.cancel(true);
        }

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

        // System.out.println("OPC: 0x" + Integer.toHexString(opcode).toUpperCase());
        // System.out.println("DEC OPC: 0x" + Integer.toHexString(decodedOpcode).toUpperCase());
        if (opcodesMap.containsKey(decodedOpcode)) {
            opcodesMap.get(decodedOpcode).accept(opcode);
        } else {
            // System.out.println("UNKNOWN OPCODE - 0x" + Integer.toHexString(decodedOpcode).toUpperCase());
        }

        changeCycle();
    }

    // 60Hz
    private void timerTick() {
        if (soundTimer.get() > 0) {
            soundTimer.subtract(1);
        }

        if (delayTimer.get() > 0) {
            delayTimer.subtract(1);
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

    public void keyPress(Key key) {
        input.register(key);
    }

    // <editor-fold defaultstate="collapsed" desc="opcode methods, double click to expand (Netbeans)">
    // debug
    private void printOpcode(char arg) {
        // System.out.println("0x" + Integer.toHexString(arg).toUpperCase() + '\n');
        programCounter += 2;
    }

    private void emptyRegion(char arg) {
        // System.out.println("EMPTY MEMORY ADDRESS REACHED - 0x" + Integer.toHexString(programCounter));
        programCounter += 2;
    }

    // 0000
    private void call(char opc) {
        // not used (?)
    }

    // 00E0
    private void dispClear(char opc) {
        // System.out.println("DISP CLEAR\n");

        display.clear();
        programCounter += 2;
    }

    // 00EE
    private void returnSubRoutine(char opc) {
        // System.out.println("RETURN SUBROUTINE\n");

        programCounter = stack.get(stackPointer - 1);
        stackPointer--;
    }

    // 1000
    private void goTo(char opc) {
        // System.out.println("GOTO\n");

        programCounter = (char) (opc & 0x0FFF);
    }

    // 2000
    private void callSubroutine(char opc) {
        // System.out.println("CALL SUBROUTINE\n");

        stack.set(stackPointer, programCounter);
        stackPointer++;
        programCounter = (char) (opc & 0x0FFF);
    }

    // 3000
    private void skipVxEqNN(char opc) {
        // System.out.println("SKIP IF VX == NN\n");

        if (v.get((opc & 0x0F00) >> 8) == (opc & 0x00FF)) {
            programCounter += 4;
        } else {
            programCounter += 2;
        }
    }

    // 4000
    private void skipVxNotEqNN(char opc) {
        // System.out.println("SKIP IF VX != NN\n");

        if (v.get((opc & 0x0F00) >> 8) != (opc & 0x00FF)) {
            programCounter += 4;
        } else {
            programCounter += 2;
        }
    }

    // 5000
    private void skipVxEqVy(char opc) {
        // System.out.println("SKIP IF VX == VY\n");

        if (v.get((opc & 0x0F00) >> 8) == v.get((opc & 0x0F00) >> 8)) {
            programCounter += 4;
        } else {
            programCounter += 2;
        }
    }

    // 6000
    private void setVx(char opc) {
        // System.out.println("SET VX\n");

        v.set((opc & 0x0F00) >> 8, (char) (opc & 0x00FF));
        programCounter += 2;
    }

    // 7000
    private void addNNtoVx(char opc) {
        // System.out.println("VX += NN\n");

        v.set((opc & 0x0F00) >> 8, (char) (v.get((opc & 0x0F00) >> 8) + (opc & 0x00FF)));
        programCounter += 2;
    }

    // 8000
    private void setVxTovY(char opc) {
        // System.out.println("VX = VY\n");

        v.set((opc & 0x0F00) >> 8, v.get((opc & 0x00F0) >> 4));
        programCounter += 2;
    }

    // 8001
    private void setVxToVxOrVy(char opc) {
        // System.out.println("VX = VX | VY\n");

        v.set((opc & 0x0F00) >> 8, (char) (v.get((opc & 0x0F00) >> 8) | v.get((opc & 0x00F0) >> 4)));
        programCounter += 2;
    }

    // 8002
    private void setVxToVxAndVy(char opc) {
        // System.out.println("VX = VX & VY\n");

        v.set((opc & 0x0F00) >> 8, (char) (v.get((opc & 0x0F00) >> 8) & v.get((opc & 0x00F0) >> 4)));
        programCounter += 2;
    }

    // 8003
    private void setVxToVxXorVy(char opc) {
        // System.out.println("VX = VX ^ VY\n");

        v.set((opc & 0x0F00) >> 8, (char) (v.get((opc & 0x0F00) >> 8) ^ v.get((opc & 0x00F0) >> 4)));
        programCounter += 2;
    }

    // 8004
    private void addVxToVyCarry(char opc) {
        // System.out.println("VX += VY (CARRY)\n");

        if ((v.get((opc & 0x0F00) >> 8) + v.get((opc & 0x00F0) >> 4)) > 255) {
            v.set(0xF, (char) 1);
        } else {
            v.set(0xF, (char) 0);
        }
        v.set((opc & 0x0F00) >> 8, (char) (v.get((opc & 0x0F00) >> 8) + v.get((opc & 0x00F0) >> 4)));
        programCounter += 2;
    }

    // 8005
    private void subtractVxToVyCarry(char opc) {
        // System.out.println("VX -= VY (CARRY)\n");

        if (v.get((opc & 0x0F00) >> 8) > v.get((opc & 0x00F0) >> 4)) {
            v.set(0xF, (char) 1);
        } else {
            v.set(0xF, (char) 0);
        }
        v.set((opc & 0x00F0) >> 4, (char) (v.get((opc & 0x00F0) >> 4) - v.get((opc & 0x0F00) >> 8)));
        programCounter += 2;
    }

    // 8006
    private void shiftRightVyToVx(char opc) {
        // System.out.println("VX = VY = VY >> 1, VF = VY LSB\n");

        v.set(0xF, (char) (v.get((opc & 0x00F0) >> 4) & 0x7));
        v.set((opc & 0x0F00) >> 8, (char) (v.get((opc & 0x00F0) >> 4) >> 1));
        programCounter += 2;
    }

    // 8007
    private void subtractVyAndVx2nd(char opc) {
        // System.out.println("VX -= VY (CARRY)(2nd)\n");

        if (v.get((opc & 0x0F00) >> 8) > v.get((opc & 0x00F0) >> 4)) {
            v.set(0xF, (char) 1);
        } else {
            v.set(0xF, (char) 0);
        }
        v.set((opc & 0x0F00) >> 8, (char) (v.get((opc & 0x00F0) >> 4) - v.get((opc & 0x0F00) >> 8)));
        programCounter += 2;
    }

    // 800E
    private void shiftLeftVyToVx(char opc) {
        // System.out.println("VX = VY = VY << 1, VF = VY MSB\n");

        v.set(0xF, (char) (v.get((opc & 0x00F0) >> 4) >> 0x7));
        v.set((opc & 0x0F00) >> 8, (char) (v.get((opc & 0x00F0) >> 4) << 1));
        programCounter += 2;
    }

    // 9000
    private void skipVxNotEqVy(char opc) {
        // System.out.println("SKIP IF VX != VY\n");

        if (v.get((opc & 0x0F00) >> 8) != v.get((opc & 0x00F0) >> 4)) {
            programCounter += 4;
        } else {
            programCounter += 2;
        }
    }

    // A000
    private void setI(char opc) {
        // System.out.println("SET I\n");

        i = (char) (opc & 0x0FFF);
        programCounter += 2;
    }

    // B000
    private void goToV0(char opc) {
        // System.out.println("GOTO + V0\n");

        programCounter = (char) (v.get(0x0) + (opc & 0x0FFF));
    }

    // C000
    private void rand(char opc) {
        // System.out.println("RAND\n");
        v.set((opc & 0x0F00) >> 8, (char) (random.nextInt(255) & (opc & 0x00FF)));
        programCounter += 2;
    }

    // D000
    private void draw(char opc) {
        // System.out.println("DRAW\n");

        final char[] lines = new char[opc & 0x000F];
        for (int index = 0; index < (opc & 0x000F); index++) {
            lines[index] = memory.get(i + index);
        }

        display.draw(opc, lines);
        programCounter += 2;
    }

    /*
    // E09E
    private void call(char opc) {

    }

    // E0A1
    private void call(char opc) {

    }

    // F007
    private void call(char opc) {

    }

    // F00A
    private void call(char opc) {

    }

    // F015
    private void call(char opc) {

    }

    // F018
    private void call(char opc) {

    }
     */
    // F01E
    private void addsVxToI(char opc) {
        // System.out.println("I += VX\n");

        i += v.get((opc & 0x0F00) >> 8);
        programCounter += 2;
    }

    /*
    // F029
    private void call(char opc) {

    }

    // F033
    private void call(char opc) {

    }

    // F055
    private void call(char opc) {

    }

    // F065
    private void call(char opc) {

     */
    // TODO superchip opcodes
    // </editor-fold>
}
