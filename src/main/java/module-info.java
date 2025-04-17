module com.example.rc4_des_project {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.rc4_des_project to javafx.fxml;
    exports com.example.rc4_des_project;
}