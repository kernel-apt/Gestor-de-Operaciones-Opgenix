module mx.edu.uacm.is.slt.ds.gestor 
{
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.base;
    requires javafx.fxml;

    opens mx.edu.uacm.is.slt.ds.gestor to javafx.fxml;
    exports mx.edu.uacm.is.slt.ds.gestor;
   
    opens controllers to javafx.fxml;
    exports controllers;
}
