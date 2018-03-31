package com.jbatista.batatinha.emulator;

import java.util.Arrays;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class Display {

    private final WritableImage image;
    private final PixelWriter writer;
    private final List<Character> v;
    private final char[] buffer = new char[2048];
    private int x;
    private int y;
    private int imgX;
    private int imgY;
    private int scale;

    public Display(List<Character> v, int scale) {
        this.v = v;
        this.scale = scale;

        image = new WritableImage(64 * scale, 32 * scale);
        writer = image.getPixelWriter();

        clear();
    }

    public void draw(char opcode, char[] data) {
        x = v.get((opcode & 0x0F00) >> 8);
        y = v.get((opcode & 0x00F0) >> 4);
        v.set(0xf, (char) 0x0);

        for (int py = 0; py < data.length; py++) {
            for (int px = 0; px < 8; px++) {
                if ((data[py] & (0x80 >> px)) != 0) {
                    if (buffer[(x + px + ((y + py) * 64))] == 1) {
                        v.set(0xF, (char) 1);
                    }
                    buffer[x + px + ((y + py) * 64)] ^= 1;
                }
            }
        }
    }

    public void clear() {
        Arrays.fill(buffer, (char) 0);
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
