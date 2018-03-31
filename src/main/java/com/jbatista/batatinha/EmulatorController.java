package com.jbatista.batatinha;

import com.jbatista.batatinha.emulator.Chip8;
import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import javafx.animation.AnimationTimer;
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

    private File program = new File("D:\\Users\\joao\\Desktop", "BREAKOUT");
    private Chip8 chip8;

    private final AnimationTimer animationTimer;
    private final DecimalFormat decimalFormat = new DecimalFormat("#");

    public EmulatorController() {
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                screen.setImage(chip8.getImage());
            }
        };
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        slCPUSpeed.valueProperty().addListener((observable) -> {
            lbCPUSpeed.setText(decimalFormat.format(slCPUSpeed.valueProperty().get()) + "Hz");
        });

        chip8 = new Chip8((short) slCPUSpeed.getValue(), program, 7);
    }

    @FXML
    private void startVM(ActionEvent event) throws Exception {
        animationTimer.stop();
        animationTimer.start();
        chip8.start();
    }

    @FXML
    private void pressButton(ActionEvent event) throws Exception {
        chip8.toggleKey(((Button) event.getSource()).getText());
    }

}
