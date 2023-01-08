package com.example.easybackup.controllers;

import com.example.easybackup.TaskListener;
import com.example.easybackup.dialogs.CopyConfirmationDialog;
import com.example.easybackup.threads.CopyThread;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import javafx.util.Pair;

import java.io.File;
import java.util.Optional;

public class EasyBackupController implements TaskListener {
    @FXML
    private TextArea errorsBox;
    @FXML
    private Label originFilesCountLabel;
    @FXML
    private Label targetFilesCountLabel;
    @FXML
    private Pane mainPanel;
    @FXML
    private Button searchButtonOrigin;
    @FXML
    private TextField targetPath;
    @FXML
    private Button searchButtonTarget;
    @FXML
    private TreeView<String> originFileList;
    @FXML
    private ProgressBar copyProgressBar;
    @FXML
    private TreeView<String> targetFileList;
    @FXML
    private Button copyButton;
    @FXML
    private Button checkButton;
    @FXML
    private ProgressIndicator checkProgressIndicator;
    @FXML
    private TextField originPath;

    private final DirectoryChooser dirChooser = new DirectoryChooser();
    private CopyThread copyThread;

    /**
     * array for count the items in origin and target folders.
     * pos 0 for origin files count,
     * pos 1 for target files count.
     */
    private final int[] itemsCounter = new int[2];
    private double backupSize;


    @FXML
    private void onOriginSearchButtonClick() {
        Window window = mainPanel.getScene().getWindow();
        File selectedFolder = dirChooser.showDialog(window);
        if (selectedFolder != null) {
            originPath.setText(selectedFolder.getPath());
            TreeItem<String> root = new TreeItem<>(selectedFolder.getName());
            originFileList.setRoot(root);
            itemsCounter[0] = 0;
            listFiles(selectedFolder.listFiles(), root, 0, true);
            originFilesCountLabel.setText(String.valueOf(itemsCounter[0]));
        } else {
            errorsBox.appendText("La carpeta seleccionada no existe\n");
        }
    }

    @FXML
    private void onTargetSearchButtonClick() {
        Window window = mainPanel.getScene().getWindow();
        File selectedFolder = dirChooser.showDialog(window);
        if (selectedFolder != null) {
            targetPath.setText(selectedFolder.getPath());
            TreeItem<String> root = new TreeItem<>(selectedFolder.getName());
            targetFileList.setRoot(root);
            itemsCounter[1] = 0;
            listFiles(selectedFolder.listFiles(), root, 1, false);
            targetFilesCountLabel.setText(String.valueOf(itemsCounter[1]));
        } else {
            errorsBox.appendText("La carpeta seleccionada no existe\n");
        }
    }

    @FXML
    private void onCopyButtonClick() {
        try {
            File originFolder = new File(originPath.getText());
            File targetFolder = new File(targetPath.getText());
            if (originFolder.exists() && targetFolder.exists()) {
                double folderSize = backupSize / (1024 * 1024);
                String sizeText = folderSize >= 1024
                        ? Math.round(folderSize / 1024) + " GB."
                        : Math.round(folderSize) + " MB.";
                String defaultFolderName = "Respaldo de " + originFolder.getName();
                Dialog<Pair<String, Boolean>> copyConfirmationDialog = CopyConfirmationDialog.createDialog(defaultFolderName, sizeText);
                Optional<Pair<String, Boolean>> dialogResult = copyConfirmationDialog.showAndWait();
                dialogResult.ifPresent(result -> {
                    // COPIAR
                    copyThread = new CopyThread(originFolder, targetFolder, result.getKey(), result.getValue());
                });

            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error!");
                errorAlert.setHeaderText("Error al presionar el botón copiar");
                errorAlert.setContentText("Antes de pulsar el botón para copiar, debes seleccionar una carpeta de origen y una de destino.");
                errorAlert.show();
            }
        } catch (Exception e) {
            errorsBox.appendText(e.getMessage() + "\n");
        }
    }

    /**
     * @param files            folder with the files to list in the TreeView.
     * @param parentFolderItem TreeItem's parent to append the child file.
     * @param posToIncrement   index of the item counter in the array of files counters.
     */
    private void listFiles(File[] files, TreeItem<String> parentFolderItem, int posToIncrement, boolean cleanBackupSizeCounter) {
        try {
            if (cleanBackupSizeCounter) {
                backupSize = 0;
            }
            for (File file : files) {
                itemsCounter[posToIncrement]++;
                if (file.isDirectory()) {
                    TreeItem<String> parent = new TreeItem<>(file.getName());
                    parentFolderItem.getChildren().add(parent);
                    File[] fileFiles = file.listFiles();
                    if (fileFiles != null) {
                        listFiles(fileFiles, parent, posToIncrement, false);
                    }
                } else {
                    if (posToIncrement == 0) {
                        backupSize += file.length();
                    }
                    parentFolderItem.getChildren().add(new TreeItem<>("> " + file.getName()));
                }
            }
        } catch (Exception e) {
            errorsBox.appendText(e.getMessage() + "\n");
        }
    }

    @Override
    public void threadCompleteExecution(Runnable thread, boolean executionResult) {
        Alert copyResult = new Alert(Alert.AlertType.INFORMATION);
        copyResult.setTitle("Copy result information");
        copyResult.setHeaderText("Information about the copy process");
        if (executionResult) {
            copyResult.setContentText("The copy was successful!");
        } else {
            copyResult.setContentText("Something went wrong :(\n" +
                    "Consult the errors box!\n" +
                    "or the developer...");
        }
        copyResult.show();
    }
}