package com.jbatista.batatinha;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;

public class SettingsController implements Initializable {

    @FXML
    private Slider sldCpuSpeed;
    @FXML
    private ColorPicker cpickerBackground;
    @FXML
    private ColorPicker cpickerPixel;

    public SettingsController() throws IOException {
        MainApp.settings.load();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sldCpuSpeed.valueProperty().bindBidirectional(new SimpleObjectProperty<Number>() {
            @Override
            public Number get() {
                return MainApp.settings.getCpuSpeed();
            }

            @Override
            public void set(Number newValue) {
                MainApp.settings.setCpuSpeed(newValue.shortValue());
            }

        });

        cpickerBackground.valueProperty().bindBidirectional(new SimpleObjectProperty<Color>() {
            @Override
            public Color get() {
                return Color.valueOf(MainApp.settings.getBackgroundColor());
            }

            @Override
            public void set(Color newValue) {
                MainApp.settings.setBackgroudColor(newValue.toString());
            }
        });

        cpickerPixel.valueProperty().bindBidirectional(new SimpleObjectProperty<Color>() {
            @Override
            public Color get() {
                return Color.valueOf(MainApp.settings.getPixelColor());
            }

            @Override
            public void set(Color newValue) {
                MainApp.settings.setPixelColor(newValue.toString());
            }
        });
    }

}
