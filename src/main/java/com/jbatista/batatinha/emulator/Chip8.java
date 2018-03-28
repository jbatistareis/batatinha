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
    private char opcode = 0x0;
    private final char[] memory = new char[4096];
    private final char[] v = new char[16];
    private char i = 0;
    private char pc = 0x200;
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
    private final Map<String, Opcode> opcodesMap = new HashMap<>();
    private final StringBuilder decodedOpcode = new StringBuilder();

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

        //opcodes list
        //<editor-fold defaultstate="collapsed" desc="double click to expand">
        opcodesMap.put("0000", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("00e0", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("00ee", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("1000", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("2000", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("3000", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("4000", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("5000", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("6000", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("7000", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("8000", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("8001", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("8002", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("8003", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("8004", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("8005", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("8006", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("8007", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("800e", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("9000", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("a000", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("b000", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("c000", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("d000", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("e09e", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("e0a1", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("f007", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("f00a", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("f015", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("f018", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("f01e", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("f029", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("f033", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("f055", (arg) -> {
            printOpcode(arg);
        });
        opcodesMap.put("f065", (arg) -> {
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
        opcode = (char) (memory[pc] << 8 | memory[pc + 1]);
        decodedOpcode.setLength(0);
        decodedOpcode.append(Integer.toHexString(opcode & 0xF000));

        if (opcodesMap.containsKey(decodedOpcode.toString())) {
            opcodesMap.get(decodedOpcode.toString()).execute(opcode);
        } else {
            System.out.println("UNK OPC - " + decodedOpcode);
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
        System.out.println(Integer.toHexString(arg));
        pc += 2;
    }

}
