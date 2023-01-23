package com.example.easybackup.dialogs;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import javafx.util.Pair;

import java.io.File;
import java.util.Map;
import java.awt.Desktop;


public class CheckFilesResultDialog {

    public static Dialog<String> createDialog(Map<String, File> filesNotFound) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Resultado del checkeo de archivos");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 50, 20, 50));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        dialog.getDialogPane().getStylesheets().add(String.valueOf(CopyConfirmationDialog.class.getResource("CheckFilesResultDialogStyle.css")));
        dialog.getDialogPane().getStyleClass().add("dialog");

        ListView<String> filesNotFoundView = new ListView<>();
        filesNotFoundView.setPrefWidth(500);
        filesNotFoundView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                try {
                    if (mouseEvent.getButton() == MouseButton.MIDDLE ||
                            (mouseEvent.getButton() == MouseButton.PRIMARY
                                    && mouseEvent.getClickCount() == 2)) {
                        String item = filesNotFoundView.getSelectionModel().getSelectedItem();
                        if (!item.trim().isEmpty()) {
                            File target = new File(item);
                            if (target.isDirectory()) {
                                Desktop.getDesktop().open(target);
                            } else {
                                Desktop.getDesktop().open(target.getParentFile());
                            }
                        }
                    }
                } catch (Exception e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Ocurrió un error");
                    errorAlert.setHeaderText("Error al intentar abrir el path seleccionado");
                    errorAlert.setContentText(e.getMessage());
                }
            }
        });
        filesNotFound.forEach((fileName, file) -> {
            filesNotFoundView.getItems().add(file.getPath());
        });
        grid.add(new Label("Archivos que no fueron encontrados en el destino:"), 0, 0);
        grid.add(filesNotFoundView, 0, 1);

        Window window = dialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(windowEvent -> {
            dialog.close();
        });

        dialog.getDialogPane().setContent(grid);

        return dialog;
    }
}
