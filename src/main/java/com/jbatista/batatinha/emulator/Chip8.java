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
    private final Map<Short, Opcode> opcodesMap = new HashMap<>();
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
        opcodesMap.put((short) -4096, (arg) -> {
            System.out.println("EMPTY MEMORY ADDRESS REACHED - " + Integer.toHexString(programCounter));
            executor.shutdownNow();
        });

        // 0000
        opcodesMap.put((short) 0, (arg) -> {
            printOpcode(arg);
        });

        // 00e0
        opcodesMap.put((short) 224, (arg) -> {
            printOpcode(arg);
        });

        // 00ee
        opcodesMap.put((short) 238, (arg) -> {
            printOpcode(arg);
        });

        // 1000
        opcodesMap.put((short) 4096, (arg) -> {
            printOpcode(arg);
        });

        // 2000
        opcodesMap.put((short) 8192, (arg) -> {
            printOpcode(arg);
        });

        // 3000
        opcodesMap.put((short) 12288, (arg) -> {
            printOpcode(arg);
        });

        // 4000
        opcodesMap.put((short) 16384, (arg) -> {
            printOpcode(arg);
        });

        // 5000
        opcodesMap.put((short) 20480, (arg) -> {
            printOpcode(arg);
        });

        // 6000
        opcodesMap.put((short) 24576, (arg) -> {
            printOpcode(arg);
        });

        // 7000
        opcodesMap.put((short) 28672, (arg) -> {
            printOpcode(arg);
        });

        // 8000
        opcodesMap.put((short) 32768, (arg) -> {
            printOpcode(arg);
        });

        // 8001
        opcodesMap.put((short) 32769, (arg) -> {
            printOpcode(arg);
        });

        // 8002
        opcodesMap.put((short) 32770, (arg) -> {
            printOpcode(arg);
        });

        // 8003
        opcodesMap.put((short) 32771, (arg) -> {
            printOpcode(arg);
        });

        // 8004
        opcodesMap.put((short) 32772, (arg) -> {
            printOpcode(arg);
        });

        // 8005
        opcodesMap.put((short) 32773, (arg) -> {
            printOpcode(arg);
        });

        // 8006
        opcodesMap.put((short) 32774, (arg) -> {
            printOpcode(arg);
        });

        // 8007
        opcodesMap.put((short) 32775, (arg) -> {
            printOpcode(arg);
        });

        // 800e
        opcodesMap.put((short) 32782, (arg) -> {
            printOpcode(arg);
        });

        // 9000
        opcodesMap.put((short) 36864, (arg) -> {
            printOpcode(arg);
        });

        // a000
        opcodesMap.put((short) 40960, (arg) -> {
            printOpcode(arg);
        });

        // b000
        opcodesMap.put((short) 45056, (arg) -> {
            printOpcode(arg);
        });

        // c000
        opcodesMap.put((short) 49152, (arg) -> {
            printOpcode(arg);
        });

        // d000
        opcodesMap.put((short) 53248, (arg) -> {
            printOpcode(arg);
        });

        // e09e
        opcodesMap.put((short) 57502, (arg) -> {
            printOpcode(arg);
        });

        // e0a1
        opcodesMap.put((short) 57505, (arg) -> {
            printOpcode(arg);
        });

        // f007
        opcodesMap.put((short) 61447, (arg) -> {
            printOpcode(arg);
        });

        // f00a
        opcodesMap.put((short) 61450, (arg) -> {
            printOpcode(arg);
        });

        // f015
        opcodesMap.put((short) 61461, (arg) -> {
            printOpcode(arg);
        });

        // f018
        opcodesMap.put((short) 61464, (arg) -> {
            printOpcode(arg);
        });

        // f01e
        opcodesMap.put((short) 61470, (arg) -> {
            printOpcode(arg);
        });

        // f029
        opcodesMap.put((short) 61481, (arg) -> {
            printOpcode(arg);
        });

        // f033
        opcodesMap.put((short) 61491, (arg) -> {
            printOpcode(arg);
        });

        // f055
        opcodesMap.put((short) 61525, (arg) -> {
            printOpcode(arg);
        });

        // f065
        opcodesMap.put((short) 61541, (arg) -> {
            printOpcode(arg);
        });

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
            opcodesMap.get((short) decodedOpcode).execute(opcode);
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
    private void printOpcode(char arg) {
        System.out.println(Integer.toHexString(arg).toUpperCase());
        programCounter += 2;
    }

}
