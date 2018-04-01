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

    // <editor-fold defaultstate="collapsed" desc="buttons, double click to expand (Netbeans)">
    @FXML
    private Button btn0;
    @FXML
    private Button btn1;
    @FXML
    private Button btn2;
    @FXML
    private Button btn3;
    @FXML
    private Button btn4;
    @FXML
    private Button btn5;
    @FXML
    private Button btn6;
    @FXML
    private Button btn7;
    @FXML
    private Button btn8;
    @FXML
    private Button btn9;
    @FXML
    private Button btnA;
    @FXML
    private Button btnB;
    @FXML
    private Button btnC;
    @FXML
    private Button btnD;
    @FXML
    private Button btnE;
    @FXML
    private Button btnF;
    // </ editor-fold>

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

        // <editor-fold defaultstate="collapsed" desc="button listeners, double click to expand (Netbeans)">
        btn0.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                chip8.toggleKey(0);
            } else {
                chip8.toggleKey(0);
            }
        });
        btn1.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                chip8.toggleKey(1);
            } else {
                chip8.toggleKey(1);
            }
        });
        btn2.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                chip8.toggleKey(2);
            } else {
                chip8.toggleKey(2);
            }
        });
        btn3.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                chip8.toggleKey(3);
            } else {
                chip8.toggleKey(3);
            }
        });
        btn4.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                chip8.toggleKey(4);
            } else {
                chip8.toggleKey(4);
            }
        });
        btn5.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                chip8.toggleKey(5);
            } else {
                chip8.toggleKey(5);
            }
        });
        btn6.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                chip8.toggleKey(6);
            } else {
                chip8.toggleKey(6);
            }
        });
        btn7.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                chip8.toggleKey(7);
            } else {
                chip8.toggleKey(7);
            }
        });
        btn8.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                chip8.toggleKey(8);
            } else {
                chip8.toggleKey(8);
            }
        });
        btn9.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                chip8.toggleKey(9);
            } else {
                chip8.toggleKey(9);
            }
        });
        btnA.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                chip8.toggleKey(10);
            } else {
                chip8.toggleKey(10);
            }
        });
        btnB.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                chip8.toggleKey(11);
            } else {
                chip8.toggleKey(1);
            }
        });
        btnC.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                chip8.toggleKey(12);
            } else {
                chip8.toggleKey(12);
            }
        });
        btnD.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                chip8.toggleKey(13);
            } else {
                chip8.toggleKey(13);
            }
        });
        btnE.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                chip8.toggleKey(14);
            } else {
                chip8.toggleKey(14);
            }
        });
        btnF.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                chip8.toggleKey(15);
            } else {
                chip8.toggleKey(15);
            }
        });
        // </ editor-fold>

        chip8 = new Chip8((short) slCPUSpeed.getValue(), program, 7);
    }

    @FXML
    private void startVM(ActionEvent event) throws Exception {
        animationTimer.stop();
        animationTimer.start();
        chip8.start();
    }

}
