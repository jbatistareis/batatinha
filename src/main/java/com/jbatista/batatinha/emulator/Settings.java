package com.jbatista.batatinha.emulator;

import com.jbatista.batatinha.MainApp;
import java.io.IOException;

public class Settings {

    private String lastDir = System.getProperty("user.home");
    private short cpuSpeed = 500;
    private String backgroundColor = "BLACK";
    private String pixelColor = "WHITE";
    private String note = "A";

    public boolean save() throws IOException {
        MainApp.objectMapper.writeValue(MainApp.settingsFile, this);
        return true;
    }

    public Settings load() throws IOException {
        try {
            final Settings savedSettings = MainApp.objectMapper.readValue(MainApp.settingsFile, Settings.class);
            setLastDir(savedSettings.getLastDir());
            setCpuSpeed(savedSettings.getCpuSpeed());
            setBackgroudColor(savedSettings.getBackgroundColor());
            setPixelColor(savedSettings.getPixelColor());
            setNote(savedSettings.getNote());
        } catch (IOException ex) {
            save();
        }

        return this;
    }

    public String getLastDir() {
        return lastDir;
    }

    public void setLastDir(String lastDir) {
        this.lastDir = lastDir;
    }

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

    /**
     * @return the note
     */
    public String getNote() {
        return note;
    }

    /**
     * @param note the note to set
     */
    public void setNote(String note) {
        this.note = note;
    }

}
