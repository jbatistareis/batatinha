package com.jbatista.batatinha.emulator;

import java.awt.Color;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

public class Display {

    private final GraphicsContext screen;
    private char[] pixels = new char[2048];

    public Display(GraphicsContext screen) {
        this.screen = screen;
        this.screen.setFill(Paint.valueOf("black"));
    }

    public void setPixel(int x, int y) {

    }

    public void clear() {
        screen.fill();
    }

}
