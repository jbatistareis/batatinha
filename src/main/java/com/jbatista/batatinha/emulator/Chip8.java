package com.jbatista.batatinha.emulator;

import com.jbatista.batatinha.emulator.Input.Key;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.canvas.GraphicsContext;

public class Chip8 extends Service<Short> {

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
    private final short cpuSpeed;
    private short cycle = 0;

    // CPU, memory, registers, font
    private char opcode = 0;
    private final char[] memory = new char[4096];
    private final char[] v = new char[16];
    private char i = 0;
    private char programCounter = 512;
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

    // GO TO stack
    private final char[] stack = new char[16];
    private char stackPointer = 0;

    // timers
    private short soundTimer = 0;
    private short delayTimer = 0;

    // auxiliary
    private final Display display;
    private final Input input = new Input();
    private final Map<Character, Consumer<Character>> opcodesMap = new HashMap<>();
    private char decodedOpcode;

    public Chip8(short cpuSpeed, File program, GraphicsContext screen) throws Exception {
        this.cpuSpeed = cpuSpeed;
        this.display = new Display(screen);

        Arrays.fill(v, (char) 0);
        Arrays.fill(stack, (char) 0);

        // load font
        for (int i = 0; i < 80; i++) {
            memory[i] = font[i];
        }

        // load program
        final FileInputStream fileInputStream = new FileInputStream(program);
        int data;
        int index = 0;
        while ((data = fileInputStream.read()) != -1) {
            memory[index + 512] = (char) data;
            index++;
        }
        fileInputStream.close();

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
        opcodesMap.put((char) 0x8006, this::printOpcode);
        opcodesMap.put((char) 0x8007, this::printOpcode);
        opcodesMap.put((char) 0x800E, this::printOpcode);
        opcodesMap.put((char) 0x9000, this::printOpcode);
        opcodesMap.put((char) 0xA000, this::printOpcode);
        opcodesMap.put((char) 0xB000, this::printOpcode);
        opcodesMap.put((char) 0xC000, this::printOpcode);
        opcodesMap.put((char) 0xD000, this::printOpcode);
        opcodesMap.put((char) 0xE09E, this::printOpcode);
        opcodesMap.put((char) 0xE0A1, this::printOpcode);
        opcodesMap.put((char) 0xF007, this::printOpcode);
        opcodesMap.put((char) 0xF00A, this::printOpcode);
        opcodesMap.put((char) 0xF015, this::printOpcode);
        opcodesMap.put((char) 0xF018, this::printOpcode);
        opcodesMap.put((char) 0xF01E, this::printOpcode);
        opcodesMap.put((char) 0xF029, this::printOpcode);
        opcodesMap.put((char) 0xF033, this::printOpcode);
        opcodesMap.put((char) 0xF055, this::printOpcode);
        opcodesMap.put((char) 0xF065, this::printOpcode);

        // TODO superchip opcodes
        // </editor-fold>
    }

    @Override
    protected Task<Short> createTask() {
        return new Task<Short>() {
            @Override
            protected Short call() throws Exception {
                // 60Hz timer
                executor.scheduleWithFixedDelay(() -> {
                    timerTick();
                }, 1000 / 60, 1000 / 60, TimeUnit.MILLISECONDS);

                // CPU timer
                executor.scheduleWithFixedDelay(() -> {
                    cpuTick();
                    updateValue(changeCycle());
                }, 1000 / cpuSpeed, 1000 / cpuSpeed, TimeUnit.MILLISECONDS);

                while (true) {
                }
            }
        };
    }

    @Override
    public void restart() {
        Arrays.fill(v, (char) 0);
        i = 0;
        programCounter = 512;
        Arrays.fill(stack, (char) 0);
        stackPointer = 0;
        soundTimer = 0;
        delayTimer = 0;
        super.restart();
    }

    @Override
    public boolean cancel() {
        executor.shutdownNow();
        return super.cancel();
    }

    // 500Hz ~ 1000Hz
    private void cpuTick() {
        opcode = (char) (memory[programCounter] << 8 | memory[programCounter + 1]);
        decodedOpcode = (char) (opcode & 0xF000);

        System.out.println("OPC: 0x" + Integer.toHexString(opcode).toUpperCase());
        System.out.println("DEC OPC: 0x" + Integer.toHexString(decodedOpcode).toUpperCase() + '\n');

        if (opcodesMap.containsKey(decodedOpcode)) {
            opcodesMap.get(decodedOpcode).accept(opcode);
        } else {
            System.out.println("UNKNOWN OPCODE - 0x" + Integer.toHexString(decodedOpcode).toUpperCase());
        }
    }

    // 60Hz
    private void timerTick() {
        if (soundTimer > 0) {
            soundTimer--;
        }

        if (delayTimer > 0) {
            delayTimer--;
        }
    }

    private short changeCycle() {
        if (cycle > cpuSpeed) {
            cycle = 0;
        } else {
            cycle++;
        }

        return cycle;
    }

    public void keyPress(Key key) {
        input.register(key);
    }

    // <editor-fold defaultstate="collapsed" desc="opcode methods, double click to expand (Netbeans)">
    // debug
    private void printOpcode(char arg) {
        System.out.println("0x" + Integer.toHexString(arg).toUpperCase() + '\n');
        programCounter += 2;
    }

    private void emptyRegion(char arg) {
        System.out.println("EMPTY MEMORY ADDRESS REACHED - 0x" + Integer.toHexString(programCounter));
        cancel();
    }

    // 0000
    private void call(char arg) {
        // not used (?)
    }

    // 00E0
    private void dispClear(char arg) {
        display.clear();
        programCounter += 2;
    }

    // 00EE
    private void returnSubRoutine(char arg) {
        // ?
    }

    // 1000
    private void goTo(char arg) {
        programCounter = (char) (arg & 0x0FFF);
    }

    // 2000
    private void callSubroutine(char arg) {
        stack[stackPointer] = programCounter;
        stackPointer++;
        programCounter = (char) (arg & 0x0FFF);
    }

    // 3000
    private void skipVxEqNN(char arg) {
        if (v[(arg & 0x0F00) / 256] == (arg & 0x00FF)) {
            programCounter += 4;
        } else {
            programCounter += 2;
        }
    }

    // 4000
    private void skipVxNotEqNN(char arg) {
        if (v[(arg & 0x0F00) / 256] != (arg & 0x00FF)) {
            programCounter += 4;
        } else {
            programCounter += 2;
        }
    }

    // 5000
    private void skipVxEqVy(char arg) {
        if (v[(arg & 0x0F00) / 256] == v[(arg & 0x0F00) / 256]) {
            programCounter += 4;
        } else {
            programCounter += 2;
        }
    }

    // 6000
    private void setVx(char arg) {
        v[(arg & 0x0F00) / 256] = (char) (arg & 0x00FF);
        programCounter += 2;
    }

    // 7000
    private void addNNtoVx(char arg) {
        v[(arg & 0x0F00) / 256] += (char) (arg & 0x00FF);
        if (v[(arg & 0x0F00) / 256] > 255) {
            v[(arg & 0x0F00) / 256] = 0;
        }
        programCounter += 2;
    }

    // 8000
    private void setVxTovY(char arg) {
        v[(arg & 0x0F00) / 256] = v[arg & 0x00F0];
        programCounter += 2;
    }

    // 8001
    private void setVxToVxOrVy(char arg) {
        v[(arg & 0x0F00) / 256] = (char) (v[arg & 0x0F00] | v[arg & 0x00F0]);
        programCounter += 2;
    }

    // 8002
    private void setVxToVxAndVy(char arg) {
        v[(arg & 0x0F00) / 256] = (char) (v[arg & 0x0F00] & v[arg & 0x00F0]);
        programCounter += 2;
    }

    // 8003
    private void setVxToVxXorVy(char arg) {
        v[(arg & 0x0F00) / 256] = (char) (v[arg & 0x0F00] ^ v[arg & 0x00F0]);
        programCounter += 2;
    }

    // 8004
    private void addVxToVyCarry(char arg) {
        v[(arg & 0x0F00) / 256] += v[arg & 0x00F0];
        if (v[(arg & 0x0F00) / 256] > 255) {
            v[(arg & 0x0F00) / 256] = 0;
            v[0xF] = 1;
        } else {
            v[0xF] = 0;
        }
        programCounter += 2;
    }

    // 8005
    private void subtractVxToVyCarry(char arg) {
        v[arg & 0x0F00] -= v[arg & 0x00F0];
        if (v[(arg & 0x0F00) / 256] < 0) {
            v[(arg & 0x0F00) / 256] = 0;
            v[0xF] = 1;
        } else {
            v[0xF] = 0;
        }
        programCounter += 2;
    }

    /*
    // 8006
    private void call(char arg) {

    }

    // 8007
    private void call(char arg) {

    }

    // 800E
    private void call(char arg) {

    }

    // 9000
    private void call(char arg) {

    }

    // A000
    private void call(char arg) {

    }

    // B000
    private void call(char arg) {

    }

    // C000
    private void call(char arg) {

    }

    // D000
    private void call(char arg) {

    }

    // E09E
    private void call(char arg) {

    }

    // E0A1
    private void call(char arg) {

    }

    // F007
    private void call(char arg) {

    }

    // F00A
    private void call(char arg) {

    }

    // F015
    private void call(char arg) {

    }

    // F018
    private void call(char arg) {

    }

    // F01E
    private void call(char arg) {

    }

    // F029
    private void call(char arg) {

    }

    // F033
    private void call(char arg) {

    }

    // F055
    private void call(char arg) {

    }

    // F065
    private void call(char arg) {

     */
    // TODO superchip opcodes
    // </editor-fold>
}
