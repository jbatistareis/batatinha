package com.jbatista.batatinha.emulator;

import com.jbatista.batatinha.emulator.Input.Key;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.canvas.GraphicsContext;

public class Chip8 extends Service<Integer> {

    //child threads
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

    private final short cpuSpeed;
    private short cycle = 0;

    //CPU, memory, registers, font
    private char opcode = 0;
    private final char[] memory = new char[4096];
    private final char[] v = new char[16];
    private char i = 0;
    private char programCounter = 0x200;
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

    //GO TO stack
    private final char[] stack = new char[16];
    private char stackPointer = 0;

    //timers
    private short soundTimer = 0;
    private short delayTimer = 0;

    //auxiliary
    private final Display display;
    private final Input input = new Input();
    private final Map<Short, Supplier<Opcode>> opcodesMap = new HashMap<>();
    private char decodedOpcode;

    public Chip8(short cpuSpeed, File program, GraphicsContext screen) throws Exception {
        this.cpuSpeed = cpuSpeed;
        this.display = new Display(screen);

        //load font
        for (int i = 0; i < 80; i++) {
            memory[i] = font[i];
        }

        //load program
        final BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(program), 512);
        int data;
        int index = 0;
        while ((data = bufferedInputStream.read()) != -1) {
            memory[index + 512] = (char) data;
            index++;
        }
        bufferedInputStream.close();
        //<editor-fold defaultstate="collapsed" desc="opcodes list, converted to short because java doesnt support unsigned numbers, double click to expand (Netbeans)">
        // empty address
        opcodesMap.put((short) -4096, () -> (arg) -> {
            System.out.println("EMPTY MEMORY ADDRESS REACHED - " + Integer.toHexString(programCounter));
            executor.shutdownNow();
        });

        // 0000
        opcodesMap.put((short) 0, () -> this::printOpcode);

        // 00E0
        opcodesMap.put((short) 224, () -> this::printOpcode);

        // 00EE
        opcodesMap.put((short) 238, () -> this::printOpcode);

        // 1000
        opcodesMap.put((short) 4096, () -> this::printOpcode);

        // 2000
        opcodesMap.put((short) 8192, () -> this::printOpcode);

        // 3000
        opcodesMap.put((short) 12288, () -> this::printOpcode);

        // 4000
        opcodesMap.put((short) 16384, () -> this::printOpcode);

        // 5000
        opcodesMap.put((short) 20480, () -> this::printOpcode);

        // 6000
        opcodesMap.put((short) 24576, () -> this::printOpcode);

        // 7000
        opcodesMap.put((short) 28672, () -> this::printOpcode);

        // 8000
        opcodesMap.put((short) 32768, () -> this::printOpcode);

        // 8001
        opcodesMap.put((short) 32769, () -> this::printOpcode);

        // 8002
        opcodesMap.put((short) 32770, () -> this::printOpcode);

        // 8003
        opcodesMap.put((short) 32771, () -> this::printOpcode);

        // 8004
        opcodesMap.put((short) 32772, () -> this::printOpcode);

        // 8005
        opcodesMap.put((short) 32773, () -> this::printOpcode);

        // 8006
        opcodesMap.put((short) 32774, () -> this::printOpcode);

        // 8007
        opcodesMap.put((short) 32775, () -> this::printOpcode);

        // 800E
        opcodesMap.put((short) 32782, () -> this::printOpcode);

        // 9000
        opcodesMap.put((short) 36864, () -> this::printOpcode);

        // A000
        opcodesMap.put((short) 40960, () -> this::printOpcode);

        // B000
        opcodesMap.put((short) 45056, () -> this::printOpcode);

        // C000
        opcodesMap.put((short) 49152, () -> this::printOpcode);

        // D000
        opcodesMap.put((short) 53248, () -> this::printOpcode);

        // E09E
        opcodesMap.put((short) 57502, () -> this::printOpcode);

        // E0A1
        opcodesMap.put((short) 57505, () -> this::printOpcode);

        // F007
        opcodesMap.put((short) 61447, () -> this::printOpcode);

        // F00A
        opcodesMap.put((short) 61450, () -> this::printOpcode);

        // F015
        opcodesMap.put((short) 61461, () -> this::printOpcode);

        // F018
        opcodesMap.put((short) 61464, () -> this::printOpcode);

        // F01E
        opcodesMap.put((short) 61470, () -> this::printOpcode);

        // F029
        opcodesMap.put((short) 61481, () -> this::printOpcode);

        // F033
        opcodesMap.put((short) 61491, () -> this::printOpcode);

        // F055
        opcodesMap.put((short) 61525, () -> this::printOpcode);

        // F065
        opcodesMap.put((short) 61541, () -> this::printOpcode);

        // </editor-fold>
    }

    @Override
    protected Task<Integer> createTask() {
        return new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                //60Hz timer
                executor.scheduleWithFixedDelay(() -> {
                    timerTick();
                }, 1000 / 60, 1000 / 60, TimeUnit.MILLISECONDS);

                //CPU timer
                executor.scheduleWithFixedDelay(() -> {
                    cpuTick();
                    updateValue(changeCycle());
                }, 1000 / cpuSpeed, 1000 / cpuSpeed, TimeUnit.MILLISECONDS);

                //so it doesnt finalyze, ever...
                while (true) {
                }
            }
        };
    }

    @Override
    public boolean cancel() {
        executor.shutdownNow();
        return super.cancel();
    }

    //500Hz ~ 1000Hz
    private void cpuTick() {
        opcode = (char) (memory[programCounter] << 8 | memory[programCounter + 1]);
        decodedOpcode = (char) (opcode & 0xF000);

        if (opcodesMap.containsKey((short) decodedOpcode)) {
            opcodesMap.get((short) decodedOpcode).get().execute(opcode);
        } else {
            System.out.println("UNKNOWN OPCODE - " + Integer.toHexString(decodedOpcode).toUpperCase());
        }
    }

    //60Hz
    private void timerTick() {
        if (soundTimer > 0) {
            soundTimer--;
        }

        if (delayTimer > 0) {
            delayTimer--;
        }
    }

    private int changeCycle() {
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

    //opcode methods
    //debug
    private void printOpcode(char arg) {
        System.out.println(Integer.toHexString(arg).toUpperCase());
        programCounter += 2;
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
