package com.jbatista.batatinha;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javax.sound.sampled.LineUnavailableException;

public class SettingsController implements Initializable {

    @FXML
    private Slider sldCpuSpeed;
    @FXML
    private ColorPicker cpickerBackground;
    @FXML
    private ColorPicker cpickerPixel;
    @FXML
    private ComboBox<String> cbTone;

    public SettingsController() throws IOException, LineUnavailableException {
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
                super.set(newValue);
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
                super.set(newValue);
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
                super.set(newValue);
                MainApp.settings.setPixelColor(newValue.toString());
            }
        });

        cbTone.getItems().addAll("A", "B", "C", "D", "E", "F", "G");
        cbTone.valueProperty().bindBidirectional(new SimpleObjectProperty<String>() {
            @Override
            public String get() {
                return MainApp.settings.getNote();
            }

            @Override
            public void set(String newValue) {
                super.set(newValue);
                MainApp.settings.setNote(newValue);
            }
        });

    }

}
