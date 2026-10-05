package com.example.maryshell.launch;

import com.example.maryshell.functionality.Commands;
import com.example.maryshell.ui.Output;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;

/**
 * FXML-контроллер главного окна эмулятора.
 * <p>
 * Связывает UI ({@link Label} вывода, {@link ScrollPane}) с логикой
 * ({@link Output}, {@link Commands}). Создаётся автоматически
 * {@code FXMLLoader} через {@code setControllerFactory}.
 * <p>
 * В {@link #initialize()} создаётся {@link Output}, который сразу
 * регистрируется в {@link Commands}. Ввод пользователя обрабатывается
 * через {@link #setScene(Scene)} — событие {@code KEY_TYPED}
 * перенаправляется в {@link Output#handleSceneKeyType(KeyEvent)}.
 *
 * @author Cortofanchic
 * @version 1.0
 * @see Shell
 * @see Output
 * @see Commands
 */
public class Controller {
    @FXML
    private Label labelText;
    @FXML
    private ScrollPane scrollPane;

    private Output outputModule;
    private final Shell shellModule;

    /**
     * Привязывает обработчик ввода к сцене.
     * <p>
     * Каждое событие {@code KEY_TYPED} перенаправляется в
     * {@link Output#handleSceneKeyType(KeyEvent)}.
     *
     * @param scene сцена главного окна
     */
    public void setScene(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_TYPED, (event) -> outputModule.handleSceneKeyType(event));
    }

    /**
     * Создаёт контроллер, привязанный к приложению.
     *
     * @param shell ссылка на приложение (для доступа к VFS)
     */
    Controller(Shell shell){
        shellModule = shell;
    }

    /**
     * Инициализация контроллера после загрузки FXML.
     * <p>
     * Создаёт {@link Output}, регистрирует его в {@link Commands}
     * и выводит первичное приглашение.
     */
    @FXML
    private void initialize() {
        outputModule = new Output(labelText, scrollPane, Commands.getCommands(), shellModule);
        Commands.setOutputModule(outputModule);
        outputModule.start(); //вывод первичного
    }

    /**
     * Возвращает модуль вывода.
     *
     * @return модуль вывода
     */
    public Output getOutput() { return outputModule; }
}
