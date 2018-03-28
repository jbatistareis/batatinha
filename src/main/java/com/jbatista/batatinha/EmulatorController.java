package com.jbatista.batatinha;

import com.jbatista.batatinha.emulator.Chip8;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;

public class EmulatorController implements Initializable {

    private Chip8 chip8;
    private File program = new File("D:\\Users\\joao\\Desktop", "BREAKOUT");
    private short cpuSpeed = 500;

    @FXML
    private Label label;
    private Canvas screen = new Canvas(32, 64);

    @FXML
    private void handleButtonAction(ActionEvent event) throws Exception {
        System.out.println("You clicked me!");
        label.setText("Hello World!");

        chip8 = new Chip8(cpuSpeed, program, screen.getGraphicsContext2D());
        chip8.start();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @Override
    protected void finalize() throws Throwable {
        chip8.cancel();
        super.finalize(); //To change body of generated methods, choose Tools | Templates.
    }

}
