module com.example.easybackup {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.easybackup to javafx.fxml;
    exports com.example.easybackup;
    exports com.example.easybackup.dialogs;
    opens com.example.easybackup.dialogs to javafx.fxml;
}