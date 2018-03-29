package com.jbatista.batatinha.emulator;

import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class Display {

    private final WritableImage image = new WritableImage(32, 64);
    private final PixelReader reader = image.getPixelReader();
    private final PixelWriter writer = image.getPixelWriter();
    private final StringBuilder sbPixel = new StringBuilder();

    public Display(ImageView screen) {
        screen.setImage(image);
        clear();
    }

    public void draw(int x, int y, char... data) {
        for (int px = 0; px < 8; px++) {
            for (int py = 0; py < data.length; py++) {
                sbPixel.append(reader.getColor(x + px, y + py).equals(Color.BLACK) ? "0" : "1");

                Integer.parseInt(reader.getColor(x + px, y + py).equals(Color.BLACK) ? "0" : "1");
            }
        }

        sbPixel.setLength(0);
    }

    public void clear() {
        for (int x = 31; x >= 0; x--) {
            for (int y = 0; y <= 63; y++) {
                writer.setColor(x, y, Color.BLACK);
            }
        }
    }

}
