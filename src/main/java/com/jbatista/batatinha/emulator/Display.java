package com.jbatista.batatinha.emulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Display {

    private final List<Character> sprite = new ArrayList<>();
    private char[] buffer;
    private char[] tempBuffer;
    private char[] xLine;
    private char[] yLine;
    private char collision;

    private int reducedWidth;
    private int reducedHeight;
    private int width;
    private int height;
    private int xPos;
    private int yPos;
    private int pixel;

    private int pyOffset;
    private int spriteHexComparator;

    private Mode mode = Mode.LOW_RES;

    public enum Mode {
        LOW_RES, HIGH_RES
    }

    public Display() {
        changeDisplayMode(mode);
    }

    void changeDisplayMode(Mode mode) {
        this.mode = mode;
        switch (this.mode) {
            case LOW_RES:
                width = 64;
                height = 32;
                break;
            case HIGH_RES:
                width = 128;
                height = 64;
                break;
        }

        reducedWidth = width - 4;
        buffer = new char[width * height];
        tempBuffer = new char[buffer.length];
        xLine = new char[width];
        yLine = new char[height];
        clear();
    }

    Mode getDisplayMode() {
        return this.mode;
    }

    char draw(int x, int y, int spriteWidth) {
        collision = 0;
        spriteHexComparator = (spriteWidth == 8) ? 0x80 : 0x8000;

        for (int py = 0; py < sprite.size(); py++) {
            yPos = (y + py) % height;
            pyOffset = yPos * width;
            for (int px = 0; px < spriteWidth; px++) {
                xPos = (x + px) % width;
                if ((sprite.get(py) & (spriteHexComparator >> px)) != 0) {
                    pixel = xPos + pyOffset;
                    collision |= buffer[pixel];
                    buffer[pixel] ^= 1;
                }
            }
        }
        sprite.clear();

        return collision;
    }

    void scrollR4() {
        Arrays.fill(tempBuffer, (char) 0);
        Arrays.fill(xLine, (char) 0);

        for (int sy = 0; sy < height; sy++) {
            for (int sx = 0; sx < reducedWidth; sx++) {
                xLine[sx] = buffer[sx + (sy * width)];
            }

            for (int sx = 4; sx < width; sx++) {
                tempBuffer[sx + (sy * width)] = xLine[sx - 4];
            }
        }

        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = tempBuffer[i];
        }
    }

    void scrollL4() {
        Arrays.fill(tempBuffer, (char) 0);
        Arrays.fill(xLine, (char) 0);

        for (int sy = 0; sy < height; sy++) {
            for (int sx = 4; sx < width; sx++) {
                xLine[sx - 4] = buffer[sx + (sy * width)];
            }

            for (int sx = 0; sx < reducedWidth; sx++) {
                tempBuffer[sx + (sy * width)] = xLine[sx];
            }
        }

        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = tempBuffer[i];
        }
    }

    void scrollDown(int amount) {
        reducedHeight = height - amount;
        Arrays.fill(tempBuffer, (char) 0);
        Arrays.fill(yLine, (char) 0);

        for (int sx = 0; sx < width; sx++) {
            for (int sy = 0; sy < reducedHeight; sy++) {
                yLine[sy] = buffer[sx + (sy * width)];
            }

            for (int sy = amount; sy < height; sy++) {
                tempBuffer[sx + (sy * width)] = yLine[sy - amount];
            }
        }

        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = tempBuffer[i];
        }
    }

    // XO-CHIP, unofficial
    void scrollUp(int amount) {
        reducedHeight = height - amount;
        Arrays.fill(tempBuffer, (char) 0);
        Arrays.fill(yLine, (char) 0);

        for (int sx = 0; sx < width; sx++) {
            for (int sy = amount; sy < height; sy++) {
                yLine[sy - amount] = buffer[sx + (sy * width)];
            }

            for (int sy = 0; sy < reducedHeight; sy++) {
                tempBuffer[sx + (sy * width)] = yLine[sy];
            }
        }

        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = tempBuffer[i];
        }
    }

    void clear() {
        Arrays.fill(buffer, (char) 0);
    }

    void addSpriteData(char data) {
        sprite.add(data);
    }

    public char[] getBuffer() {
        return this.buffer;
    }

}
