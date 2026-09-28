package com.example.maryshell;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;

public class Controller {
    @FXML
    private Label labelText;
    @FXML
    private ScrollPane scrollPane;

    private Output outputModule;

    public void setScene(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_TYPED, (event) -> outputModule.handleSceneKeyType(event));
    }

    @FXML
    private void initialize() {
        outputModule = new Output(labelText, scrollPane, Commands.getCommands());
        Commands.setOutputModule(outputModule);
        outputModule.start(); //вывод первичного
    }

    public Output getOutput() { return outputModule; }
}
