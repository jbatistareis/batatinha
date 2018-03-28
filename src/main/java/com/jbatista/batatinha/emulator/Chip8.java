package com.jbatista.batatinha.emulator;

import java.io.File;
import java.util.Arrays;
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

    private final File program;
    private final short cpuSpeed;
    private short cycle = 0;

    //CPU, memory, registers, font
    private short opcode = 0x0;
    private final char[] memory = new char[4096];
    private final char[] v = new char[16];
    private short i = 0;
    private short pc = 0x200;
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
    private final short[] stack = new short[16];
    private short stackPointer = 0;

    //timers
    private short soundTimer = 0;
    private short delayTimer = 0;

    //auxiliary
    private final Display display;
    private final Input input = new Input();
    private final Map<Short, Opcode> opcodesMap = new HashMap<>();

    public Chip8(short cpuSpeed, File program, GraphicsContext screen) throws Exception {
        this.program = program;
        this.cpuSpeed = cpuSpeed;
        this.display = new Display(screen);

        //zero fill memory and registers
        Arrays.fill(memory, (char) 0x0);
        Arrays.fill(v, (char) 0x0);

        //load font
        for (int i = 0; i < 80; i++) {
            memory[i] = font[i];
        }

        //opcodes list
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
        opcodesMap.put((short) 0x0, (arg) -> {
            testOpcode(arg);
        });
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
        opcode = (short) (memory[pc] << 8 | memory[pc + 1]);

        opcodesMap.get(opcode).execute(opcode);
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

    //opcode methods
    public void testOpcode(Short arg) {
        System.out.println(arg);
    }

}
