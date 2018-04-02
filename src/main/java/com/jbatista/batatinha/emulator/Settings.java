package com.jbatista.batatinha.emulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;

public class Settings {

    private final ObjectMapper objectMapper = new ObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true);

    private short cpuSpeed = 500;
    private String backgroundColor = "BLACK";
    private String pixelColor = "WHITE";

    public short getCpuSpeed() {
        return cpuSpeed;
    }

    public void setCpuSpeed(short cpuSpeed) {
        this.cpuSpeed = cpuSpeed;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroudColor(String backgroundColor) {
        this.backgroundColor = backgroundColor.toUpperCase();
    }

    public String getPixelColor() {
        return pixelColor;
    }

    public void setPixelColor(String pixelColor) {
        this.pixelColor = pixelColor.toUpperCase();
    }

    public void save() throws IOException {
        objectMapper.writeValue(new File("settings.json"), this);
    }

    public Settings load() throws IOException {
        try {
            final Settings savedSettings = objectMapper.readValue(new File("settings.json"), Settings.class);
            setCpuSpeed(savedSettings.getCpuSpeed());
            setBackgroudColor(savedSettings.getBackgroundColor());
            setPixelColor(savedSettings.getPixelColor());
        } catch (IOException ex) {
            save();
        }

        return this;
    }

}
