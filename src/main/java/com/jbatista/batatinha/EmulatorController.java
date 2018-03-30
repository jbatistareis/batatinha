package com.jbatista.batatinha;

import com.jbatista.batatinha.emulator.Chip8;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class EmulatorController implements Initializable {

    private Chip8 chip8;
    private File program = new File("D:\\Users\\joao\\Desktop", "BREAKOUT");
    private short cpuSpeed = 500;

    @FXML
    private Label label;
    @FXML
    private ImageView screen;

    @FXML
    private void handleButtonAction(ActionEvent event) throws Exception {
        System.out.println("You clicked me!");
        label.setText("Hello World!");

        chip8 = new Chip8(cpuSpeed, program, screen);
        chip8.start();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

}
