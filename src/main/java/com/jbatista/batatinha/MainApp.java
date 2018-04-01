package com.jbatista.batatinha;

import com.jbatista.batatinha.emulator.Input;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
    public static Input input;

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Scene.fxml"));

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");

        scene.setOnKeyPressed((event) -> {
            switch (event.getCode()) {
                // <editor-fold defaultstate="collapsed" desc="keyboard statements, double click to expand (Netbeans)">
                case DIGIT1:
                    MainApp.input.toggleKey(0);
                    break;
                case DIGIT2:
                    MainApp.input.toggleKey(1);
                    break;
                case DIGIT3:
                    MainApp.input.toggleKey(2);
                    break;
                case DIGIT4:
                    MainApp.input.toggleKey(3);
                    break;
                case Q:
                    MainApp.input.toggleKey(4);
                    break;
                case W:
                    MainApp.input.toggleKey(5);
                    break;
                case E:
                    MainApp.input.toggleKey(6);
                    break;
                case R:
                    MainApp.input.toggleKey(7);
                    break;
                case A:
                    MainApp.input.toggleKey(8);
                    break;
                case S:
                    MainApp.input.toggleKey(9);
                    break;
                case D:
                    MainApp.input.toggleKey(10);
                    break;
                case F:
                    MainApp.input.toggleKey(11);
                    break;
                case Z:
                    MainApp.input.toggleKey(12);
                    break;
                case X:
                    MainApp.input.toggleKey(13);
                    break;
                case C:
                    MainApp.input.toggleKey(14);
                    break;
                case V:
                    MainApp.input.toggleKey(15);
                    break;
                case NUMPAD0:
                    MainApp.input.toggleKey(0);
                    break;
                case NUMPAD1:
                    MainApp.input.toggleKey(1);
                    break;
                case NUMPAD2:
                    MainApp.input.toggleKey(2);
                    break;
                case NUMPAD3:
                    MainApp.input.toggleKey(3);
                    break;
                case NUMPAD4:
                    MainApp.input.toggleKey(4);
                    break;
                case NUMPAD5:
                    MainApp.input.toggleKey(5);
                    break;
                case NUMPAD6:
                    MainApp.input.toggleKey(6);
                    break;
                case NUMPAD7:
                    MainApp.input.toggleKey(7);
                    break;
                case NUMPAD8:
                    MainApp.input.toggleKey(8);
                    break;
                case NUMPAD9:
                    MainApp.input.toggleKey(9);
                    break;
                default:
                    break;
                // </ editor-fold>
            }
        });

        scene.setOnKeyReleased((event) -> {
            switch (event.getCode()) {
                // <editor-fold defaultstate="collapsed" desc="keyboard statements, double click to expand (Netbeans)">
                case DIGIT1:
                    MainApp.input.toggleKey(0);
                    break;
                case DIGIT2:
                    MainApp.input.toggleKey(1);
                    break;
                case DIGIT3:
                    MainApp.input.toggleKey(2);
                    break;
                case DIGIT4:
                    MainApp.input.toggleKey(3);
                    break;
                case Q:
                    MainApp.input.toggleKey(4);
                    break;
                case W:
                    MainApp.input.toggleKey(5);
                    break;
                case E:
                    MainApp.input.toggleKey(6);
                    break;
                case R:
                    MainApp.input.toggleKey(7);
                    break;
                case A:
                    MainApp.input.toggleKey(8);
                    break;
                case S:
                    MainApp.input.toggleKey(9);
                    break;
                case D:
                    MainApp.input.toggleKey(10);
                    break;
                case F:
                    MainApp.input.toggleKey(11);
                    break;
                case Z:
                    MainApp.input.toggleKey(12);
                    break;
                case X:
                    MainApp.input.toggleKey(13);
                    break;
                case C:
                    MainApp.input.toggleKey(14);
                    break;
                case V:
                    MainApp.input.toggleKey(15);
                    break;
                case NUMPAD0:
                    MainApp.input.toggleKey(0);
                    break;
                case NUMPAD1:
                    MainApp.input.toggleKey(1);
                    break;
                case NUMPAD2:
                    MainApp.input.toggleKey(2);
                    break;
                case NUMPAD3:
                    MainApp.input.toggleKey(3);
                    break;
                case NUMPAD4:
                    MainApp.input.toggleKey(4);
                    break;
                case NUMPAD5:
                    MainApp.input.toggleKey(5);
                    break;
                case NUMPAD6:
                    MainApp.input.toggleKey(6);
                    break;
                case NUMPAD7:
                    MainApp.input.toggleKey(7);
                    break;
                case NUMPAD8:
                    MainApp.input.toggleKey(8);
                    break;
                case NUMPAD9:
                    MainApp.input.toggleKey(9);
                    break;
                default:
                    break;
                // </ editor-fold>
            }
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
