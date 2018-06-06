package com.jbatista.batatinha;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jbatista.batatinha.emulator.Input;
import com.jbatista.batatinha.emulator.Settings;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(3, (runnable) -> {
        final Thread thread = new Thread(runnable);
        thread.setPriority(3);
        return thread;
    });

    public static final ObjectMapper objectMapper = new ObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true);
    public static final File settingsFile = new File("settings.json");
    public static Settings settings;
    public static Input input;

    public MainApp() throws IOException {
        settings = new Settings().load();
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Scene.fxml"));

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");

        scene.setOnKeyPressed((event) -> {
            switch (event.getCode()) {
                // <editor-fold defaultstate="collapsed" desc="keyboard statements, double click to expand (Netbeans)">               
                case DIGIT1:
                    MainApp.input.press(1);
                    break;
                case DIGIT2:
                    MainApp.input.press(2);
                    break;
                case DIGIT3:
                    MainApp.input.press(3);
                    break;
                case DIGIT4:
                    MainApp.input.press(12);
                    break;
                case Q:
                    MainApp.input.press(4);
                    break;
                case W:
                    MainApp.input.press(5);
                    break;
                case E:
                    MainApp.input.press(6);
                    break;
                case R:
                    MainApp.input.press(13);
                    break;
                case A:
                    MainApp.input.press(7);
                    break;
                case S:
                    MainApp.input.press(8);
                    break;
                case D:
                    MainApp.input.press(9);
                    break;
                case F:
                    MainApp.input.press(14);
                    break;
                case Z:
                    MainApp.input.press(10);
                    break;
                case X:
                    MainApp.input.press(0);
                    break;
                case C:
                    MainApp.input.press(11);
                    break;
                case V:
                    MainApp.input.press(15);
                    break;
                case NUMPAD0:
                    MainApp.input.press(0);
                    break;
                case NUMPAD1:
                    MainApp.input.press(1);
                    break;
                case NUMPAD2:
                    MainApp.input.press(2);
                    break;
                case NUMPAD3:
                    MainApp.input.press(3);
                    break;
                case NUMPAD4:
                    MainApp.input.press(4);
                    break;
                case NUMPAD5:
                    MainApp.input.press(5);
                    break;
                case NUMPAD6:
                    MainApp.input.press(6);
                    break;
                case NUMPAD7:
                    MainApp.input.press(7);
                    break;
                case NUMPAD8:
                    MainApp.input.press(8);
                    break;
                case NUMPAD9:
                    MainApp.input.press(9);
                    break;
                default:
                    break;
                // </ editor-fold>
                }

            event.consume();
        });

        scene.setOnKeyReleased((event) -> {
            switch (event.getCode()) {
                // <editor-fold defaultstate="collapsed" desc="keyboard statements, double click to expand (Netbeans)">               
                case DIGIT1:
                    MainApp.input.release(1);
                    break;
                case DIGIT2:
                    MainApp.input.release(2);
                    break;
                case DIGIT3:
                    MainApp.input.release(3);
                    break;
                case DIGIT4:
                    MainApp.input.release(12);
                    break;
                case Q:
                    MainApp.input.release(4);
                    break;
                case W:
                    MainApp.input.release(5);
                    break;
                case E:
                    MainApp.input.release(6);
                    break;
                case R:
                    MainApp.input.release(13);
                    break;
                case A:
                    MainApp.input.release(7);
                    break;
                case S:
                    MainApp.input.release(8);
                    break;
                case D:
                    MainApp.input.release(9);
                    break;
                case F:
                    MainApp.input.release(14);
                    break;
                case Z:
                    MainApp.input.release(10);
                    break;
                case X:
                    MainApp.input.release(0);
                    break;
                case C:
                    MainApp.input.release(11);
                    break;
                case V:
                    MainApp.input.release(15);
                    break;
                case NUMPAD0:
                    MainApp.input.release(0);
                    break;
                case NUMPAD1:
                    MainApp.input.release(1);
                    break;
                case NUMPAD2:
                    MainApp.input.release(2);
                    break;
                case NUMPAD3:
                    MainApp.input.release(3);
                    break;
                case NUMPAD4:
                    MainApp.input.release(4);
                    break;
                case NUMPAD5:
                    MainApp.input.release(5);
                    break;
                case NUMPAD6:
                    MainApp.input.release(6);
                    break;
                case NUMPAD7:
                    MainApp.input.release(7);
                    break;
                case NUMPAD8:
                    MainApp.input.release(8);
                    break;
                case NUMPAD9:
                    MainApp.input.release(9);
                    break;
                default:
                    break;
                // </ editor-fold>
                }

            event.consume();
        });

        stage.setTitle("Batatinha");
        stage.setScene(scene);
        stage.setOnCloseRequest((event) -> {
            executor.shutdownNow();
        });
        stage.setResizable(false);
        stage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
