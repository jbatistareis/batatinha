package com.jbatista.batatinha.emulator;

import java.util.Arrays;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

public class Display {

    private final GraphicsContext screen;
    private final char[] pixels = new char[2048];

    public Display(GraphicsContext screen) {
        this.screen = screen;
        this.screen.setFill(Paint.valueOf("black"));
        clear();
    }

    public void draw(int x, int y, char... sprites) {
        for (int spriteIndex = 0; spriteIndex < sprites.length; spriteIndex++) {

        }
    }

    public void clear() {
        Arrays.fill(pixels, (char) 0);
        screen.fill();
    }

}
