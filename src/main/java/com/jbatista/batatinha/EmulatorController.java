package com.jbatista.batatinha;

import com.jbatista.batatinha.emulator.Chip8;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

public class EmulatorController implements Initializable {

    @FXML
    private Canvas canvas;

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

    private File program;
    private Chip8 chip8;
    private final AnimationTimer animationTimer;
    private int bufferPosition;
    private int scale;

    private Color backgroundColor;
    private Color pixelColor;

    public EmulatorController() {
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                chip8.timerTick();
                for (int i = 0; i < (MainApp.settings.getCpuSpeed() * 0.016); i++) {
                    chip8.cpuTick();
                }

                bufferPosition = 0;
                scale = (chip8.getDisplay().length == 2048) ? 8 : 4;

                // clear screen
                canvas.getGraphicsContext2D().setFill(backgroundColor);
                canvas.getGraphicsContext2D().fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

                // draw pixels
                canvas.getGraphicsContext2D().setFill(pixelColor);
                for (int iy = 0; iy < canvas.getHeight(); iy += scale) {
                    for (int ix = 0; ix < canvas.getWidth(); ix += scale) {
                        if (chip8.getDisplay()[bufferPosition++] == 1) {
                            canvas.getGraphicsContext2D().fillRect(ix, iy, scale, scale);
                        }
                    }
                }
            }
        };
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        backgroundColor = Color.web(MainApp.settings.getBackgroundColor());
        pixelColor = Color.web(MainApp.settings.getPixelColor());

        // <editor-fold defaultstate="collapsed" desc="button listeners, double click to expand (Netbeans)">
        btn0.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                MainApp.input.toggleKey(0);
            } else {
                MainApp.input.toggleKey(0);
            }
        });
        btn1.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                MainApp.input.toggleKey(1);
            } else {
                MainApp.input.toggleKey(1);
            }
        });
        btn2.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                MainApp.input.toggleKey(2);
            } else {
                MainApp.input.toggleKey(2);
            }
        });
        btn3.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                MainApp.input.toggleKey(3);
            } else {
                MainApp.input.toggleKey(3);
            }
        });
        btn4.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                MainApp.input.toggleKey(4);
            } else {
                MainApp.input.toggleKey(4);
            }
        });
        btn5.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                MainApp.input.toggleKey(5);
            } else {
                MainApp.input.toggleKey(5);
            }
        });
        btn6.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                MainApp.input.toggleKey(6);
            } else {
                MainApp.input.toggleKey(6);
            }
        });
        btn7.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                MainApp.input.toggleKey(7);
            } else {
                MainApp.input.toggleKey(7);
            }
        });
        btn8.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                MainApp.input.toggleKey(8);
            } else {
                MainApp.input.toggleKey(8);
            }
        });
        btn9.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                MainApp.input.toggleKey(9);
            } else {
                MainApp.input.toggleKey(9);
            }
        });
        btnA.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                MainApp.input.toggleKey(10);
            } else {
                MainApp.input.toggleKey(10);
            }
        });
        btnB.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                MainApp.input.toggleKey(11);
            } else {
                MainApp.input.toggleKey(1);
            }
        });
        btnC.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                MainApp.input.toggleKey(12);
            } else {
                MainApp.input.toggleKey(12);
            }
        });
        btnD.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                MainApp.input.toggleKey(13);
            } else {
                MainApp.input.toggleKey(13);
            }
        });
        btnE.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                MainApp.input.toggleKey(14);
            } else {
                MainApp.input.toggleKey(14);
            }
        });
        btnF.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                MainApp.input.toggleKey(15);
            } else {
                MainApp.input.toggleKey(15);
            }
        });
        // </ editor-fold>
    }

    @FXML
    private void startVM(ActionEvent event) throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open ROM");
        fileChooser.setInitialDirectory(new File(MainApp.settings.getLastDir()));
        program = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        load();
    }

    @FXML
    private void settings(ActionEvent event) throws Exception {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Settings");
        dialog.getDialogPane().setContent(FXMLLoader.load(getClass().getResource("/fxml/Settings.fxml")));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
        dialog.setResultConverter((param) -> !param.getButtonData().isCancelButton());

        final Optional<Boolean> result = dialog.showAndWait();
        if (result.get()) {
            backgroundColor = Color.web(MainApp.settings.getBackgroundColor());
            pixelColor = Color.web(MainApp.settings.getPixelColor());
            chip8.changeNote(MainApp.settings.getNote());

            MainApp.settings.save();
        }
    }

    @FXML
    private void about(ActionEvent event) throws Exception {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("About");
        dialog.setHeaderText("Batatinha, a CHIP-8 and Super CHIP emulator written in Java");
        dialog.setContentText("2018, github.com/jbatistareis");
        //dialog.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/icon.png"))));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
    }

    private void load() throws IOException {
        if (program != null) {
            MainApp.settings.setLastDir(program.getParent());
            MainApp.settings.save();

            animationTimer.stop();
            chip8 = new Chip8(
                    MainApp.input,
                    MainApp.settings.getNote(),
                    program);
            animationTimer.start();

            try {
                chip8.start();
            } catch (IOException ex) {
                Logger.getLogger(EmulatorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
