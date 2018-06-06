package com.jbatista.batatinha.emulator;

import com.jbatista.batatinha.MainApp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class Display {

    private WritableImage image;
    private Color backgroundColor;
    private Color pixelColor;
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
    private int imgX;
    private int imgY;
    private int xPos;
    private int yPos;
    private int pixel;
    private int scale;

    public enum Mode {
        CHIP8, SCHIP
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public Display(Mode mode, int scale) {
        this.scale = scale;
        changeDisplayMode(mode);
    }

    public void changeDisplayMode(Mode mode) {
        switch (mode) {
            case CHIP8:
                width = 64;
                height = 32;
                break;
            case SCHIP:
                width = 128;
                height = 64;
                break;
        }

        reducedWidth = width - 4;
        buffer = new char[width * height];
        tempBuffer = new char[buffer.length];
        xLine = new char[width];
        yLine = new char[height];
        backgroundColor = Color.web(MainApp.settings.getBackgroundColor());
        pixelColor = Color.web(MainApp.settings.getPixelColor());
        image = new WritableImage(width * this.scale, height * this.scale);
        clear();
    }

    public char draw(int x, int y, int spriteWidth) {
        collision = 0;

        for (int py = 0; py < sprite.size(); py++) {
            yPos = (y + py) % height;
            for (int px = 0; px < spriteWidth; px++) {
                xPos = (x + px) % width;

                // evaluate
                if ((sprite.get(py) & (0x80 >> px)) != 0) {
                    pixel = xPos + (yPos * width);
                    collision |= buffer[pixel];
                    buffer[pixel] ^= 1;
                }
            }
        }
        sprite.clear();

        return collision;
    }

    public void scrollR4() {
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

    public void scrollL4() {
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

    public void scrollDown(int amount) {
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
    public void scrollUp(int amount) {
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

    public void clear() {
        Arrays.fill(buffer, (char) 0);
    }

    public void addSpriteData(char data) {
        sprite.add(data);
    }

    public Image getImage() {
        imgX = 0;
        imgY = 0;

        for (int i = 0; i < buffer.length; i++) {
            for (int ix = 0; ix < scale; ix++) {
                for (int iy = 0; iy < scale; iy++) {
                    image.getPixelWriter().setColor(
                            (int) ((imgX + ix) % image.getWidth()),
                            (int) ((imgY + iy) % image.getHeight()),
                            (buffer[i] == 0) ? backgroundColor : pixelColor);
                }
            }

            imgX = (imgX > image.getWidth() - scale - 1) ? 0 : (imgX + scale);
            imgY = (imgY > image.getHeight() - 1) ? 0 : (imgX == 0) ? (imgY + scale) : imgY;
        }

        return image;
    }

}
