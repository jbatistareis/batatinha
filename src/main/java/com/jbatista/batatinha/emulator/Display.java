package com.jbatista.batatinha.emulator;

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
    private final char[] v;
    private final char[] buffer = new char[2048];
    private final List<Character> sprite = new ArrayList<>();
    private char collision;
    private int imgX;
    private int imgY;
    private int scale;

    public Display(char[] v, int scale) {
        this.v = v;
        this.scale = scale;

        image = new WritableImage(64 * scale, 32 * scale);
        writer = image.getPixelWriter();

        clear();
    }

    public char draw(int vx, int vy) {
        collision = 0;

        for (int py = 0; py < sprite.size(); py++) {
            for (int px = 0; px < 8; px++) {
                if ((sprite.get(py) & (0x80 >> px)) != 0) {
                    if (buffer[(v[vx] + px + ((v[vy] + py) * 64))] == 1) {
                        collision = 1;
                    }
                    buffer[v[vx] + px + ((v[vy] + py) * 64)] ^= 1;
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
                    writer.setColor(imgX + ix, imgY + iy, (buffer[i] == 0) ? Color.BLACK : Color.WHITE);
                }
            }

            imgX = (imgX > image.getWidth() - scale - 1) ? 0 : (imgX + scale);
            imgY = (imgY > image.getHeight() - 1) ? 0 : (imgX == 0) ? (imgY + scale) : imgY;
        }

        return image;
    }

}
