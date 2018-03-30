package com.jbatista.batatinha.emulator;

import java.text.DecimalFormat;
import java.util.List;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class Display {

    private final WritableImage image = new WritableImage(450, 225);
    private final PixelReader reader = image.getPixelReader();
    private final PixelWriter writer = image.getPixelWriter();
    private final List<Character> v;
    private int x;
    private int y;
    private int scale;

    public Display(ImageView screen, List<Character> v) {
        this.v = v;
        this.scale = Integer.valueOf(new DecimalFormat("#").format(image.getWidth() / 64));
        screen.setImage(image);

        clear();
    }

    public void draw(char opcode, char[] data) {
        x = v.get((opcode & 0x0F00) >> 8) * scale;
        y = v.get((opcode & 0x00F0) >> 4) * scale;
        v.set(0xf, (char) 0x0);

        for (int py = 0; py < data.length; py++) {
            for (int px = 0; px < 8; px++) {
                if ((data[py] & (0x80 >> px)) != 0) {
                    if (reader.getColor(x + (px * scale), y + (py * scale)).equals(Color.BLACK)) {
                        for (int ix = 0; ix < scale; ix++) {
                            for (int iy = 0; iy < scale; iy++) {
                                writer.setColor((x + ix) + (px * scale), (y + iy) + (py * scale), Color.WHITE);
                            }
                        }
                    } else {
                        v.set(0xf, (char) 0x1);
                        for (int ix = 0; ix < scale; ix++) {
                            for (int iy = 0; iy < scale; iy++) {
                                writer.setColor((x + ix) + (px * scale), (y + iy) + (py * scale), Color.BLACK);
                            }
                        }
                    }
                }
            }
        }
    }

    public void clear() {
        for (int y = 0; y <= (image.getHeight() - 1); y++) {
            for (int x = (int) (image.getWidth() - 1); x >= 0; x--) {
                writer.setColor(x, y, Color.BLACK);
            }
        }
    }

}
