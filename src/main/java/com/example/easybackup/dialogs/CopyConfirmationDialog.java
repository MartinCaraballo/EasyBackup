package com.example.easybackup.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import javafx.util.Pair;

public class CopyConfirmationDialog {

    /**
     * Method that creates and returns a confirmation dialog to show before do the copy.
     * @param defaultFolderName
     * @param backupSize
     * @return
     */
    public static Dialog<Pair<String, Boolean>> createDialog(String defaultFolderName, String backupSize) {
        Dialog<Pair<String, Boolean>> dialog = new Dialog<>();
        dialog.setTitle("Confirmación");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 50, 20, 50));

        dialog.getDialogPane().getStylesheets().add(String.valueOf(CopyConfirmationDialog.class.getResource("CopyConfirmationDialogStyle.css")));
        dialog.getDialogPane().getStyleClass().add("dialog");

        Label sizeLabel = new Label(backupSize);
        ToggleButton separate = new ToggleButton("NO SEPARAR");
        TextField folderName = new TextField(defaultFolderName);
        ButtonType continueButton = new ButtonType("CONTINUAR", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("CANCELAR", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(continueButton, cancelButton);

        grid.add(new Label("Nombre de la carpeta del respaldo: "), 0, 0);
        grid.add(folderName, 1, 0);
        grid.add(new Label("Separar fotos y vídeos: "), 0, 1);
        grid.add(separate, 1, 1);
        grid.add(new Label("TAMAÑO DEL RESPALDO: "), 0, 2);
        grid.add(sizeLabel, 1, 2);

        Window window = dialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(windowEvent -> {
            dialog.close();
        });

        separate.setOnAction(actionEvent -> {
            if (separate.isSelected()) {
                separate.setText("SEPARAR");
            } else {
                separate.setText("NO SEPARAR");
            }
        });


        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            String finalName = (folderName.getText().isEmpty()) ? defaultFolderName : folderName.getText();
            if (buttonType == continueButton) {
                return new Pair<>(finalName, separate.isSelected());
            }
            return null;
        });
        return dialog;
    }
}
