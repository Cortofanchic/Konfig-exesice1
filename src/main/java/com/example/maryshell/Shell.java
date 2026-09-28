package com.example.maryshell;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

public class Shell extends Application {
    private Controller controller;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view.fxml"));
        Parent root = loader.load();

        // Получаем контроллер и передаём ему сцену
        controller = loader.getController();

        int SCENE_WIDTH = 500;
        int SCENE_HEIGHT = 300;

        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT); // создание сцены приложения
        controller.setScene(scene);

        customStage(stage); // настрока вывода окна
        stage.setScene(scene); // вывод scene в stage
        stage.show(); // вывод окна

        List<String> script = getParameters().getRaw();
        if (!script.isEmpty()) {
            runScript(script);
        }
    }

    public static void main(String[] args) {
        launch();
    }

    public void customStage(Stage stage){
        stage.centerOnScreen();
        stage.setTitle("MaryVFS");
        stage.toFront(); // на передний план
    }

    public void runScript(List<String> lines) {
        Timeline timeline = new Timeline();
        int TIME_BREAK = 500;
        String ENTER = "\r";

        double t = TIME_BREAK;

        for (String line : lines) {
            if (line.isBlank()) continue;

            final String cmd = line;
            timeline.getKeyFrames().add(new KeyFrame(
                    Duration.millis(t),
                    e -> {
                        for (char c : cmd.toCharArray()) {
                            controller.getOutput().handleSceneKeyType(
                                    keyEvent(String.valueOf(c)));
                        }
                        controller.getOutput().handleSceneKeyType(keyEvent(ENTER));
                    }
            ));

            t += TIME_BREAK;
            timeline.getKeyFrames().add(new KeyFrame(
                    Duration.millis(t),
                    e -> {
                        String response = controller.getOutput().getLastStr();
                        if (!response.isBlank()) System.out.println(response.replace("\n", ""));
                    }
            ));

            t += TIME_BREAK;
        }
        timeline.play();
    }

    private static KeyEvent keyEvent(String character) {
        String EMPTY_STRING = "";
        return new KeyEvent(
                KeyEvent.KEY_TYPED, character, EMPTY_STRING, KeyCode.UNDEFINED,
                false, false, false, false
        );
    }
}