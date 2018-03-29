package com.jbatista.batatinha.emulator;

import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class Display {

    private final WritableImage image = new WritableImage(64, 32);
    private final PixelReader reader = image.getPixelReader();
    private final PixelWriter writer = image.getPixelWriter();
    private final char[] v;
    private int x;
    private int y;

    public Display(ImageView screen, char[] v) {
        this.v = v;
        screen.setImage(image);
        clear();
    }

    public void draw(char opcode, char[] data) {
        x = v[(opcode & 0x0F00) >> 8];
        y = v[(opcode & 0x00F0) >> 4];

        for (int py = 0; py < data.length; py++) {
            for (int px = 0; px < 8; px++) {
                if ((data[py] & (0x80 >> px)) != 0) {
                    if (reader.getColor(x + px, y + py).equals(Color.BLACK)) {
                        writer.setColor(x + px, y + py, Color.WHITE);
                    } else {
                        v[0xf] = 1;
                        writer.setColor(x + px, y + py, Color.BLACK);
                    }
                }
            }
        }
    }

    public void clear() {
        for (int x = 63; x >= 0; x--) {
            for (int y = 0; y <= 31; y++) {
                writer.setColor(x, y, Color.BLACK);
            }
        }
    }

}
