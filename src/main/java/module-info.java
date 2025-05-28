module controller 
{
    requires com.gluonhq.richtextarea;
    requires transitive javafx.controls;
    requires com.gluonhq.emoji;
    requires javafx.graphics;
    requires javafx.fxml;
    requires java.base;
    requires java.sql;

    opens controllers to javafx.fxml;
    exports controllers;
}