package com.jbatista.batatinha.emulator;

import javafx.scene.paint.Color;

public class Settings {

    private short cpuSpeed = 500;
    private Color backgroud = Color.BLACK;
    private Color pixel = Color.WHITE;

    public short getCpuSpeed() {
        return cpuSpeed;
    }

    public void setCpuSpeed(short cpuSpeed) {
        this.cpuSpeed = cpuSpeed;
    }

    public Color getBackgroud() {
        return backgroud;
    }

    public void setBackgroud(Color backgroud) {
        this.backgroud = backgroud;
    }

    public Color getPixel() {
        return pixel;
    }

    public void setPixel(Color pixel) {
        this.pixel = pixel;
    }

}
