package com.jbatista.batatinha.emulator;

import com.jbatista.batatinha.MainApp;
import com.jbatista.batatinha.emulator.Display.Mode;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;

public class Chip8 {

    private File program;
    private final Random random = new Random();
    private final short cpuSpeed;

    // CPU, memory, registers, font
    private char opcode;
    private final char[] memory = new char[4096];
    private final char[] v = new char[16];
    private char i;
    private char programCounter;

    // hardcoded font
    private final char[] chip8Font = {
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

    private final char[] superChipFont = {
        0xFF, 0xFF, 0xC3, 0xC3, 0xC3, 0xC3, 0xC3, 0xC3, 0xFF, 0xFF, // 0
        0x18, 0x78, 0x78, 0x18, 0x18, 0x18, 0x18, 0x18, 0xFF, 0xFF, // 1
        0xFF, 0xFF, 0x03, 0x03, 0xFF, 0xFF, 0xC0, 0xC0, 0xFF, 0xFF, // 2
        0xFF, 0xFF, 0x03, 0x03, 0xFF, 0xFF, 0x03, 0x03, 0xFF, 0xFF, // 3
        0xC3, 0xC3, 0xC3, 0xC3, 0xFF, 0xFF, 0x03, 0x03, 0x03, 0x03, // 4
        0xFF, 0xFF, 0xC0, 0xC0, 0xFF, 0xFF, 0x03, 0x03, 0xFF, 0xFF, // 5
        0xFF, 0xFF, 0xC0, 0xC0, 0xFF, 0xFF, 0xC3, 0xC3, 0xFF, 0xFF, // 6
        0xFF, 0xFF, 0x03, 0x03, 0x06, 0x0C, 0x18, 0x18, 0x18, 0x18, // 7
        0xFF, 0xFF, 0xC3, 0xC3, 0xFF, 0xFF, 0xC3, 0xC3, 0xFF, 0xFF, // 8
        0xFF, 0xFF, 0xC3, 0xC3, 0xFF, 0xFF, 0x03, 0x03, 0xFF, 0xFF, // 9
        0x7E, 0xFF, 0xC3, 0xC3, 0xC3, 0xFF, 0xFF, 0xC3, 0xC3, 0xC3, // A
        0xFC, 0xFC, 0xC3, 0xC3, 0xFC, 0xFC, 0xC3, 0xC3, 0xFC, 0xFC, // B
        0x3C, 0xFF, 0xC3, 0xC0, 0xC0, 0xC0, 0xC0, 0xC3, 0xFF, 0x3C, // C
        0xFC, 0xFE, 0xC3, 0xC3, 0xC3, 0xC3, 0xC3, 0xC3, 0xFE, 0xFC, // D
        0xFF, 0xFF, 0xC0, 0xC0, 0xFF, 0xFF, 0xC0, 0xC0, 0xFF, 0xFF, // E
        0xFF, 0xFF, 0xC0, 0xC0, 0xFF, 0xFF, 0xC0, 0xC0, 0xC0, 0xC0 // F
    };

    // jump to routine stack
    private final char[] stack = new char[16];
    private char stackPointer;

    // timers
    private char soundTimer;
    private char delayTimer;

    // auxiliary
    private ScheduledFuture timer60Hz;
    private ScheduledFuture timerCPU;
    private final Display display;
    private final Buzzer buzzer;
    private boolean beep;
    private final Map<Character, Consumer<Character>> opcodesMap = new HashMap<>();
    private char decodedOpcode;
    private char tempResult;
    private int drawN;

    public Chip8(File program, int scale) throws IOException {
        this.program = program;
        MainApp.settings = new Settings().load();
        cpuSpeed = MainApp.settings.getCpuSpeed();
        display = new Display(Mode.CHIP8, scale);
        buzzer = new Buzzer(MainApp.settings.getNote());

        // <editor-fold defaultstate="collapsed" desc="hardcoded opcode functions, double click to expand (Netbeans)">
        // debug
        opcodesMap.put((char) 0xF000, this::emptyRegion);

        // chip-8 opcodes
        opcodesMap.put((char) 0xE0, this::dispClear);
        opcodesMap.put((char) 0xEE, this::returnSubRoutine);
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
        opcodesMap.put((char) 0x8005, this::subtractVyFromVx);
        opcodesMap.put((char) 0x8006, this::shiftRightVxToVx);
        opcodesMap.put((char) 0x8007, this::subtractVxFromVy);
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
        opcodesMap.put((char) 0xF029, this::setIToSpriteInVx5bit);
        opcodesMap.put((char) 0xF033, this::bcd);
        opcodesMap.put((char) 0xF055, this::dump);
        opcodesMap.put((char) 0xF065, this::load);

        // superchip opcodes
        opcodesMap.put((char) 0x10, this::exitWithCode);
        opcodesMap.put((char) 0xC0, this::scrollDown);
        opcodesMap.put((char) 0xFA, this::compat);
        opcodesMap.put((char) 0xFB, this::scrollRight);
        opcodesMap.put((char) 0xFC, this::scrollLeft);
        opcodesMap.put((char) 0xFD, this::terminate);
        opcodesMap.put((char) 0xFE, this::loRes);
        opcodesMap.put((char) 0xFF, this::hiRes);
        opcodesMap.put((char) 0xF030, this::setIToSpriteInVx10bit);
        opcodesMap.put((char) 0xF075, this::flagSave);
        opcodesMap.put((char) 0xF085, this::flagRestore);
        // </editor-fold>
    }

    public void start() throws IOException {
        if (timer60Hz != null) {
            timer60Hz.cancel(true);
        }
        if (timerCPU != null) {
            timerCPU.cancel(true);
        }

        Arrays.fill(v, (char) 0);
        Arrays.fill(stack, (char) 0);
        Arrays.fill(memory, (char) 0);

        i = 0;
        programCounter = 512;
        stackPointer = 0;
        soundTimer = 0;
        delayTimer = 0;
        display.clear();

        // load both fonts
        for (int i = 0; i < chip8Font.length; i++) {
            memory[i] = chip8Font[i];
        }
        for (int i = 0; i < superChipFont.length; i++) {
            memory[i + chip8Font.length] = superChipFont[i];
        }

        // load program
        final FileInputStream fileInputStream = new FileInputStream(program);
        int data;
        int index = 0;
        while ((data = fileInputStream.read()) != -1) {
            memory[index++ + 512] = (char) data;
        }
        fileInputStream.close();

        // 60Hz timer
        timer60Hz = MainApp.executor.scheduleWithFixedDelay(this::timerTick, 16666, 16666, TimeUnit.MICROSECONDS);

        // CPU timer
        timerCPU = MainApp.executor.scheduleWithFixedDelay(() -> {
            for (int i = 0; i < (cpuSpeed * 0.016); i++) {
                cpuTick();
            }
        }, 16666, 16666, TimeUnit.MICROSECONDS);
    }

    public void shutdown() {
        timer60Hz.cancel(true);
        timerCPU.cancel(true);
    }

    // 500Hz ~ 1000Hz
    private void cpuTick() {
        opcode = (char) (memory[programCounter] << 8 | memory[programCounter + 1]);
        decodedOpcode = (char) (opcode & 0xF000);

        // special cases, for instructions that use the last and the last two values
        // namely 0x00XX, 0x00X#, 0x8##X, 0xF#XX and 0xE#XX
        if (decodedOpcode == 0x8000) {
            decodedOpcode = (char) (opcode & 0xF00F);
        } else if ((decodedOpcode == 0xE000) || (decodedOpcode == 0xF000)) {
            decodedOpcode = (char) (opcode & 0xF0FF);
        } else if (decodedOpcode == 0x0) {
            // special case 0x00C# and 0x001#
            decodedOpcode = (char) (opcode & 0x00F0);
            if ((decodedOpcode != 0xC0) && (decodedOpcode != 0x10)) {
                decodedOpcode = opcode;
            }
        }

        if (opcodesMap.containsKey(decodedOpcode)) {
            opcodesMap.get(decodedOpcode).accept(opcode);
        } else {
            System.out.println("UNKNOWN OPCODE - 0x" + Integer.toHexString(opcode).toUpperCase());
        }
    }

    // 60Hz
    private void timerTick() {
        if (soundTimer > 0) {
            if ((--soundTimer == 0) && beep) {
                buzzer.beep();
                beep = false;
            }
        }

        if (delayTimer > 0) {
            delayTimer--;
        }
    }

    public Image getImage() {
        return display.getImage();
    }

    // <editor-fold defaultstate="collapsed" desc="opcode methods, double click to expand (Netbeans)">
    // debug
    private void printOpcode(char arg) {
        System.out.println("0x" + Integer.toHexString(arg).toUpperCase());
    }

    private void emptyRegion(char arg) {
        programCounter += 2;
    }

    // 0000
    private void call(char opc) {
        // not used (?)
        // calls a routine on the RCA 1802 chip
        // acording to the internet nobody ever used it
    }

    // 00E0
    private void dispClear(char opc) {
        display.clear();
        programCounter += 2;
    }

    // 00EE
    private void returnSubRoutine(char opc) {
        programCounter = (char) (stack[--stackPointer] + 2);
    }

    // 1NNN
    private void goTo(char opc) {
        programCounter = (char) (opc & 0x0FFF);
    }

    // 2NNN
    private void callSubroutine(char opc) {
        stack[stackPointer++] = programCounter;
        programCounter = (char) (opc & 0x0FFF);
    }

    // 3XNN
    private void skipVxEqNN(char opc) {
        if (v[(opc & 0x0F00) >> 8] == (opc & 0x00FF)) {
            programCounter += 4;
        } else {
            programCounter += 2;
        }
    }

    // 4XNN
    private void skipVxNotEqNN(char opc) {
        if (v[(opc & 0x0F00) >> 8] != (opc & 0x00FF)) {
            programCounter += 4;
        } else {
            programCounter += 2;
        }
    }

    // 5XY0
    private void skipVxEqVy(char opc) {
        if (v[(opc & 0x0F00) >> 8] == v[(opc & 0x00F0) >> 4]) {
            programCounter += 4;
        } else {
            programCounter += 2;
        }
    }

    // 6XNN
    private void setVx(char opc) {
        v[(opc & 0x0F00) >> 8] = (char) (opc & 0x00FF);
        programCounter += 2;
    }

    // 7XNN
    private void addNNtoVx(char opc) {
        v[(opc & 0x0F00) >> 8] += (opc & 0x00FF);
        v[(opc & 0x0F00) >> 8] &= 0xFF;
        programCounter += 2;
    }

    // 8XY0
    private void setVxTovY(char opc) {
        v[(opc & 0x0F00) >> 8] = v[(opc & 0x00F0) >> 4];
        programCounter += 2;
    }

    // 8XY1
    private void setVxToVxOrVy(char opc) {
        v[(opc & 0x0F00) >> 8] = (char) (v[(opc & 0x0F00) >> 8] | v[(opc & 0x00F0) >> 4]);
        programCounter += 2;
    }

    // 8XY2
    private void setVxToVxAndVy(char opc) {
        v[(opc & 0x0F00) >> 8] &= v[(opc & 0x00F0) >> 4];
        programCounter += 2;
    }

    // 8XY3
    private void setVxToVxXorVy(char opc) {
        v[(opc & 0x0F00) >> 8] ^= v[(opc & 0x00F0) >> 4];
        programCounter += 2;
    }

    // 8XY4
    private void addVxToVyCarry(char opc) {
        tempResult = (char) (v[(opc & 0x0F00) >> 8] + v[(opc & 0x00F0) >> 4]);
        v[0xF] = (char) ((tempResult > 0xFF) ? 1 : 0);
        v[(opc & 0x0F00) >> 8] = (char) (tempResult & 0xFF);
        programCounter += 2;
    }

    // 8XY5    
    private void subtractVyFromVx(char opc) {
        v[0xF] = (char) ((v[(opc & 0x0F00) >> 8] >= v[(opc & 0x00F0) >> 4]) ? 1 : 0);
        v[(opc & 0x0F00) >> 8] -= v[(opc & 0x00F0) >> 4];
        programCounter += 2;
    }

    // 8XY6
    private void shiftRightVxToVx(char opc) {
        v[0xF] = (char) (v[(opc & 0x00F0) >> 4] & 1);
        v[(opc & 0x0F00) >> 8] >>= 1;
        programCounter += 2;
    }

    // 8XY7
    private void subtractVxFromVy(char opc) {
        v[0xF] = (char) ((v[(opc & 0x00F0) >> 4] >= v[(opc & 0x0F00) >> 8]) ? 1 : 0);
        v[(opc & 0x0F00) >> 8] = (char) (v[(opc & 0x00F0) >> 4] - v[(opc & 0x0F00) >> 8]);
        programCounter += 2;
    }

    // 8XYE
    private void shiftLeftVyToVx(char opc) {
        v[0xF] = (char) ((v[(opc & 0x00F0) >> 4] & 0x80) >> 7);
        v[(opc & 0x0F00) >> 8] <<= 1;
        programCounter += 2;
    }

    // 9XY0
    private void skipVxNotEqVy(char opc) {
        if (v[(opc & 0x0F00) >> 8] != v[(opc & 0x00F0) >> 4]) {
            programCounter += 4;
        } else {
            programCounter += 2;
        }
    }

    // ANNN
    private void setI(char opc) {
        i = (char) (opc & 0x0FFF);
        programCounter += 2;
    }

    // BNNN
    private void goToV0(char opc) {
        programCounter = (char) (v[0x0] + (opc & 0x0FFF));
    }

    // CXNN
    private void rand(char opc) {
        v[(opc & 0x0F00) >> 8] = (char) (random.nextInt(255) & (opc & 0x00FF));
        programCounter += 2;
    }

    // DXYN
    // if N = 0, it loads a 16 x 16 sprite
    private void draw(char opc) {
        drawN = opc & 0x000F;
        if (drawN > 0) {
            for (int index = 0; index < drawN; index++) {
                display.addSpriteData(memory[i + index]);
            }
        } else {
            for (int index = 0; index < 32; index += 2) {
                display.addSpriteData((char) (memory[i + index] << 8 | memory[i + index + 1]));
            }
        }

        v[0xF] = display.draw(v[(opcode & 0x0F00) >> 8], v[(opcode & 0x00F0) >> 4], (drawN == 0 ? 16 : 8));
        programCounter += 2;
    }

    // EX9E
    private void skipVxEqKey(char opc) {
        if (MainApp.input.isPressed(v[(opc & 0x0F00) >> 8])) {
            programCounter += 4;
        } else {
            programCounter += 2;
        }
    }

    // EXA1
    private void skipVxNotEqKey(char opc) {
        if (!MainApp.input.isPressed(v[((opc & 0x0F00) >> 8)])) {
            programCounter += 4;
        } else {
            programCounter += 2;
        }
    }

    // FX07
    private void vxToDelay(char opc) {
        v[(opc & 0x0F00) >> 8] = (char) delayTimer;
        programCounter += 2;
    }

    // FX0A
    private void waitKey(char opc) {
        MainApp.input.resetPressResgister();

        // blocks with an infinite loop
        while (!MainApp.input.pressRegistred()) {
        }

        v[(opc & 0x0F00) >> 8] = MainApp.input.getLastKey();
        programCounter += 2;
    }

    // FX15
    private void setDelayTimer(char opc) {
        delayTimer = v[(opc & 0x0F00) >> 8];
        programCounter += 2;
    }

    // FX18
    private void setSoundTimer(char opc) {
        beep = true;
        soundTimer = v[(opc & 0x0F00) >> 8];
        programCounter += 2;
    }

    // FX1E
    private void addsVxToI(char opc) {
        i += v[(opc & 0x0F00) >> 8];
        programCounter += 2;
    }

    // FX29
    private void setIToSpriteInVx5bit(char opc) {
        i = (char) ((v[(opc & 0x0F00) >> 8] * 5));
        programCounter += 2;
    }

    // FX33
    // this was copied, shame on me :( [ source: https://github.com/JohnEarnest/Octo ]
    private void bcd(char opc) {
        memory[i] = (char) ((v[(opc & 0x0F00) >> 8] / 100) % 10);
        memory[i + 1] = (char) ((v[(opc & 0x0F00) >> 8] / 10) % 10);
        memory[i + 2] = (char) (v[(opc & 0x0F00) >> 8] % 10);
        programCounter += 2;
    }

    // FX55
    private void dump(char opc) {
        for (int vx = 0; vx <= ((opc & 0x0F00) >> 8); vx++) {
            memory[i + vx] = v[vx];
        }
        programCounter += 2;
    }

    // FX65
    private void load(char opc) {
        for (int vx = 0; vx <= ((opc & 0x0F00) >> 8); vx++) {
            v[vx] = memory[i + vx];
        }
        programCounter += 2;
    }

    // superchip opcodes
    // DXY0 is implemented inside DXYN
    // 00CX
    // has to be synced with 60Hz
    private void scrollDown(char opc) {
        display.scrollDown(opc & 0x000F);
        programCounter += 2;
    }

    // 00FA
    // not used?, makes the i register read only
    private void compat(char opc) {
        // ?, create a flag or something
        programCounter += 2;
    }

    // 00FB
    // has to be synced with 60Hz
    private void scrollRight(char opc) {
        display.scrollR4();
        programCounter += 2;
    }

    // 00FC
    // has to be synced with 60Hz
    private void scrollLeft(char opc) {
        display.scrollL4();
        programCounter += 2;
    }

    // 00FE
    private void loRes(char opc) {
        display.changeDisplayMode(Mode.CHIP8);
        programCounter += 2;
    }

    // 00FF
    private void hiRes(char opc) {
        display.changeDisplayMode(Mode.SCHIP);
        programCounter += 2;
    }

    // F030
    private void setIToSpriteInVx10bit(char opc) {
        i = (char) ((v[(opc & 0x0F00) >> 8] * 10));
        programCounter += 2;
    }

    // FX75
    private void flagSave(char opc) {
        // HP48 function, i think nobody knows what it does
        programCounter += 2;
    }

    // FX85
    private void flagRestore(char opc) {
        // HP48 function, i think nobody knows what it does
        programCounter += 2;
    }

    // 00FD
    // exit 0 is too extreme, just reset the whole thing =^)
    private void terminate(char opc) {
        try {
            start();
        } catch (IOException ex) {
            Logger.getLogger(Chip8.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // 001X
    // exit with a code: 0 means normal, 1 means error
    // since it is not used (?), reset ;)
    private void exitWithCode(char opc) {
        // skeleton, in case i come up with some other idea 
        final int exitCode = opc & 0x000F;
        if (exitCode == 0) {
            try {
                start();
            } catch (IOException ex) {
                Logger.getLogger(Chip8.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (exitCode == 1) {
            try {
                start();
            } catch (IOException ex) {
                Logger.getLogger(Chip8.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // </editor-fold>
}
