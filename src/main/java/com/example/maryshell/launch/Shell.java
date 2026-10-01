package com.example.maryshell.launch;

import com.example.maryshell.ui.Output;
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import static java.util.Arrays.stream;

public class Shell extends Application {
    private Controller controller;
    private String vfsPath = "MaryShell";
    private String startScriptPath;
    private static final String NEW_LINE_SEP = "\n";
    private static final String ENTER = "\r";
    private static final String VFS_TEG = "VFS=";
    private static final String SCRIPT_TEG = "SCRIPT=";
    private static final String TEST_TEG = "TEST=";
    private static final String ERROR_PATH = "Error:";
    private static final String EMPTY_STRING = "";

    @Override
    public void start(Stage stage) throws IOException {
        URL url = getClass().getResource("/com/example/maryshell/view.fxml");
        if (url == null){
            throw new IOException("UI file is null.");
        }
        FXMLLoader loader = new FXMLLoader(url);

        // фабрика контроллеров
        loader.setControllerFactory(controllerClass -> {
            if (controllerClass == Controller.class) {
                return new Controller(this);
            } else {
                throw new RuntimeException(
                        "Controller init error " + controllerClass);
            }
        });

        Parent root = loader.load();
        controller = loader.getController();

        int SCENE_WIDTH = 500;
        int SCENE_HEIGHT = 300;

        if (controller == null) {
            throw new IllegalStateException("Controller is null");
        }

        Output output = controller.getOutput();
        output.start();

        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT); // создание сцены приложения
        controller.setScene(scene);

        customStage(stage); // настрока вывода окна
        stage.setScene(scene); // вывод scene в stage
        stage.show(); // вывод окна

        readArgs();
    }

    public String getVfsPath() {
        return vfsPath == null ? "\"\"" : vfsPath;
    }

    public String getStartScriptPath() {
        return startScriptPath == null ? "\"\"" : startScriptPath;
    }

    private void readArgs(){
        List<String> args = getParameters().getRaw();

        if (!args.isEmpty()) {
            List<String> scripts = new ArrayList<>();

            for (String arg: args){
                if (arg.startsWith(ERROR_PATH)){
                    controller.getOutput().printExtra(arg + Output.getShellStart());
                } else if (arg.startsWith(VFS_TEG)){
                    vfsPath = arg.substring(VFS_TEG.length());
                } else if (arg.startsWith(SCRIPT_TEG)){
                    startScriptPath = arg.substring(SCRIPT_TEG.length());
                    List<String> textFromPath = readFileLines(startScriptPath);
                    String clearScriptText = cleanStringFromComments(String.join(NEW_LINE_SEP, textFromPath));
                    scripts.add(clearScriptText);
                } else {
                    String clearScriptText = cleanStringFromComments(arg.substring(TEST_TEG.length()));
                    scripts.add(clearScriptText);
                }
            }
            System.out.println(startScriptPath);
            System.out.println(vfsPath);

            runScript(stream(String.join(NEW_LINE_SEP, scripts).split(NEW_LINE_SEP)).toList());
        }
    }

    private String cleanStringFromComments(String str){
        Pattern REGEX_MULTI_LINE_COMMENTS =  Pattern.compile("/\\*.*\\*/", Pattern.DOTALL);
        Pattern REGEX_INLINE_COMMENTS = Pattern.compile("//[^\\n\\r]*");

        str = REGEX_MULTI_LINE_COMMENTS.matcher(str).replaceAll(EMPTY_STRING);
        str = REGEX_INLINE_COMMENTS.matcher(str).replaceAll(EMPTY_STRING);
        return str;
    }

    private List<String> readFileLines(String path){
        File file = new File(path);

        if (file.exists()) {
            try{
                return Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                controller.getOutput().printExtra("Error: can't read start script" + ENTER + Output.getShellStart());
            }
        } else {
            controller.getOutput().printExtra("Error: can't open start script path" + ENTER + Output.getShellStart());
        }
        return List.of();
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