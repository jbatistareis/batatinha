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
        opcodesMap.put((char) 0x00E0, this::printOpcode);
        opcodesMap.put((char) 0x00EE, this::printOpcode);
        opcodesMap.put((char) 0x1000, this::printOpcode);
        opcodesMap.put((char) 0x2000, this::printOpcode);
        opcodesMap.put((char) 0x3000, this::printOpcode);
        opcodesMap.put((char) 0x4000, this::printOpcode);
        opcodesMap.put((char) 0x5000, this::printOpcode);
        opcodesMap.put((char) 0x6000, this::printOpcode);
        opcodesMap.put((char) 0x7000, this::printOpcode);
        opcodesMap.put((char) 0x8000, this::printOpcode);
        opcodesMap.put((char) 0x8001, this::printOpcode);
        opcodesMap.put((char) 0x8002, this::printOpcode);
        opcodesMap.put((char) 0x8003, this::printOpcode);
        opcodesMap.put((char) 0x8004, this::printOpcode);
        opcodesMap.put((char) 0x8005, this::printOpcode);
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

    // opcode methods
    // debug
    private void printOpcode(char arg) {
        System.out.println("0x" + Integer.toHexString(arg).toUpperCase());
        programCounter += 2;
    }

    private void emptyRegion(char arg) {
        System.out.println("EMPTY MEMORY ADDRESS REACHED - 0x" + Integer.toHexString(programCounter));
        cancel();
    }

    // 0000
    // 00E0
    // 00EE
    // 1000
    // 2000
    // 3000
    // 4000
    // 5000
    // 6000
    // 7000
    // 8000
    // 8001
    // 8002
    // 8003
    // 8004
    // 8005
    // 8006
    // 8007
    // 800E
    // 9000
    // A000
    // B000
    // C000
    // D000
    // E09E
    // E0A1
    // F007
    // F00A
    // F015
    // F018
    // F01E
    // F029
    // F033
    // F055
    // F065
}
