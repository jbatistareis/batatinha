package com.jbatista.batatinha;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

public class SettingsController implements Initializable {

    public SettingsController() throws IOException {
        MainApp.settings.load();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

}
