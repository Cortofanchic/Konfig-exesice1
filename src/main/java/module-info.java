module com.example.maryshell {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires jdk.jfr;
    requires com.fasterxml.jackson.databind;


    opens com.example.maryshell to javafx.fxml;
    exports com.example.maryshell.build;
    opens com.example.maryshell.build to javafx.fxml;
    exports com.example.maryshell.ui;
    opens com.example.maryshell.ui to javafx.fxml;
    exports com.example.maryshell.functionality;
    opens com.example.maryshell.functionality to javafx.fxml;
    exports com.example.maryshell.launch;
    opens com.example.maryshell.launch to javafx.fxml;
    opens com.example.maryshell.vfs to javafx.fxml;
}