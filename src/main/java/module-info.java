module com.example.maryshell {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires jdk.jfr;


    opens com.example.maryshell to javafx.fxml;
    exports com.example.maryshell;
}