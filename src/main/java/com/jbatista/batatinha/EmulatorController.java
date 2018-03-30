package com.jbatista.batatinha;

import com.jbatista.batatinha.emulator.Chip8;
import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;

public class EmulatorController implements Initializable {

    @FXML
    private ImageView screen;
    @FXML
    private Slider slCPUSpeed;
    @FXML
    private Label lbCPUSpeed;

    private File program = new File("D:\\Users\\joao\\Desktop", "Zero Demo [zeroZshadow, 2007].ch8");
    private Chip8 chip8;
    private final DecimalFormat decimalFormat = new DecimalFormat("#");

    private final ChangeListener<Number> cycleListener = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            // TODO
        }
    };

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        slCPUSpeed.valueProperty().addListener((observable) -> {
            lbCPUSpeed.setText(decimalFormat.format(slCPUSpeed.valueProperty().get()) + "Hz");
        });

        chip8 = new Chip8((short) slCPUSpeed.getValue(), program, screen);
    }

    @FXML
    private void startVM(ActionEvent event) throws Exception {
        chip8.start();

        chip8.getCycle().removeListener(cycleListener);
        chip8.getCycle().addListener(cycleListener);
    }

    @FXML
    private void pressButton(ActionEvent event) throws Exception {
        System.out.println(((Button) event.getSource()).getText());
    }

}
