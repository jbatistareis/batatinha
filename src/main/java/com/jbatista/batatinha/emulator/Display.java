package com.jbatista.batatinha.emulator;

import com.jbatista.batatinha.MainApp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class Display {

    private final WritableImage image;
    private final PixelWriter writer;
    private final Color backgroundColor;
    private final Color pixelColor;
    private final char[] buffer = new char[2048];
    private final List<Character> sprite = new ArrayList<>();
    private char collision;
    private int imgX;
    private int imgY;
    private int xPos;
    private int yPos;
    private int pixel;
    private int scale;

    public Display(int scale) {
        this.scale = scale;
        this.backgroundColor = Color.valueOf(MainApp.settings.getBackgroundColor());
        this.pixelColor = Color.valueOf(MainApp.settings.getPixelColor());
        this.image = new WritableImage(64 * scale, 32 * scale);
        this.writer = this.image.getPixelWriter();
        clear();
    }

    public char draw(int x, int y) {
        collision = 0;

        for (int py = 0; py < sprite.size(); py++) {
            yPos = (y + py) % 32;
            for (int px = 0; px < 8; px++) {
                xPos = (x + px) % 64;
                if ((sprite.get(py) & (0x80 >> px)) != 0) {
                    pixel = xPos + (yPos * 64);
                    if (buffer[pixel] == 1) {
                        collision = 1;
                    }
                    buffer[pixel] ^= 1;
                }
            }
        }
        sprite.clear();

        return collision;
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

        for (int i = 0; i < 2048; i++) {
            for (int ix = 0; ix < scale; ix++) {
                for (int iy = 0; iy < scale; iy++) {
                    writer.setColor(imgX + ix, imgY + iy, (buffer[i] == 0) ? backgroundColor : pixelColor);
                }
            }

            imgX = (imgX > image.getWidth() - scale - 1) ? 0 : (imgX + scale);
            imgY = (imgY > image.getHeight() - 1) ? 0 : (imgX == 0) ? (imgY + scale) : imgY;
        }

        return image;
    }

}
