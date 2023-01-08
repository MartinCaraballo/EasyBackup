package com.example.easybackup;

import com.example.easybackup.dialogs.CopyConfirmationDialog;
import com.example.easybackup.threads.CopyThread;
import com.example.easybackup.threads.ListFilesThread;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import javafx.util.Pair;

import java.io.File;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

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
    private TextField targetPath;
    @FXML
    private TreeView<String> originFileList;
    @FXML
    private ProgressBar copyProgressBar;
    @FXML
    private TreeView<String> targetFileList;
    @FXML
    private ProgressIndicator checkProgressIndicator;
    @FXML
    private TextField originPath;

    private final DirectoryChooser dirChooser = new DirectoryChooser();
    private CopyThread copyThread;
    private ListFilesThread listFilesThread;
    private Timer progressBarUpdateTimer;
    private double backupSize;
    private int filesInOrigin;

    @FXML
    private void onOriginSearchButtonClick() {
        try {
            Window window = mainPanel.getScene().getWindow();
            File selectedFolder = dirChooser.showDialog(window);
            if (selectedFolder != null) {
                originPath.setText(selectedFolder.getPath());
                TreeItem<String> root = new TreeItem<>(selectedFolder.getName());
                listFilesThread = new ListFilesThread(selectedFolder.listFiles(), root, 0);
                listFilesThread.setListener(this);
                listFilesThread.run();
            } else {
                errorsBox.appendText("La carpeta seleccionada no existe\n");
            }
        } catch (Exception e) {
            errorsBox.appendText('\n' + e.getMessage());
        }
    }

    @FXML
    private void onTargetSearchButtonClick() {
        try {
            Window window = mainPanel.getScene().getWindow();
            File selectedFolder = dirChooser.showDialog(window);
            if (selectedFolder != null) {
                targetPath.setText(selectedFolder.getPath());
                TreeItem<String> root = new TreeItem<>(selectedFolder.getName());
                listFilesThread = new ListFilesThread(selectedFolder.listFiles(), root, 1);
                listFilesThread.setListener(this);
                listFilesThread.run();
            } else {
                errorsBox.appendText("La carpeta seleccionada no existe\n");
            }
        } catch (Exception e) {
            errorsBox.appendText('\n' + e.getMessage());
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
                progressBarUpdateTimer = new Timer();
                progressBarUpdateTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            int elementsCopied = CopyThread.getFilesCopiedValue();
                            double progress = (double) elementsCopied / filesInOrigin;
                            copyProgressBar.setProgress(progress);
                        });
                    }
                }, 0, 1);
                dialogResult.ifPresent(result -> {
                    // COPIAR
                    copyThread = new CopyThread(originFolder, targetFolder, result.getKey(), result.getValue());
                    copyThread.setListener(this);
                    copyThread.run();
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

    @FXML
    private void originTextFieldOnEnterKey() {
        try {
            File pathWritten = new File(originPath.getText());
            if (pathWritten.exists() && pathWritten.isDirectory()) {
                TreeItem<String> newRoot = new TreeItem<>(pathWritten.getName());
                listFilesThread = new ListFilesThread(pathWritten.listFiles(), newRoot, 0);
                listFilesThread.setListener(this);
                listFilesThread.run();
            } else {
                if (!pathWritten.exists()) {
                    throw new Exception("El path ingresado no existe.");
                } else if (!pathWritten.isDirectory()) {
                    throw new Exception("El path ingresado no hace referencia a una carpeta.");
                }
            }

        } catch (Exception e) {
            errorsBox.appendText('\n' + e.getMessage());
        }
    }

    @FXML
    private void targetTextFieldOnEnterKey() {
        try {
            File targetPathWritten = new File(targetPath.getText());
            if (targetPathWritten.exists() && targetPathWritten.isDirectory()) {
                TreeItem<String> newRoot = new TreeItem<>(targetPathWritten.getName());
                listFilesThread = new ListFilesThread(targetPathWritten.listFiles(), newRoot, 1);
                listFilesThread.setListener(this);
                listFilesThread.run();
            } else {
                if (!targetPathWritten.exists()) {
                    throw new Exception("El path ingresado no existe.");
                } else if (!targetPathWritten.isDirectory()) {
                    throw new Exception("El path ingresado no hace referencia a una carpeta.");
                }
            }
        } catch (Exception e) {
            errorsBox.appendText('\n' + e.getMessage());
        }
    }

    @Override
    public void threadCompleteExecution(Runnable thread, boolean executionResult, Object threadObject) {
        try {
            if (threadObject.getClass() == CopyThread.class) {
                onFinishCopyThread(executionResult);
            } else if (threadObject.getClass() == ListFilesThread.class) {
                onFinishListFilesThread((ListFilesThread) threadObject);
            }
        } catch (Exception e) {
            errorsBox.appendText('\n' + e.getMessage());
        }
    }

    private void onFinishCopyThread(boolean executionResult) {
        progressBarUpdateTimer.cancel();
        progressBarUpdateTimer.purge();
        Alert copyResult = new Alert(Alert.AlertType.INFORMATION);
        copyResult.setTitle("Copy result information");
        copyResult.setHeaderText("Information about the copy process");
        if (executionResult) {
            copyResult.setContentText("The copy was successful!\n\t(" + CopyThread.getFilesCopiedValue() + " elements copied)");
        } else {
            copyResult.setContentText("""
                    Something went wrong :(
                    Consult the errors box!
                    \tor the developer...""");
        }
        copyProgressBar.setProgress(1.0);
        File targetFolder = new File(targetPath.getText());
        TreeItem<String> newRoot = new TreeItem<>(targetFolder.getName());
        listFilesThread = new ListFilesThread(targetFolder.listFiles(), newRoot, 1);
        listFilesThread.setListener(this);
        listFilesThread.run();
        copyResult.showAndWait();
        copyProgressBar.setProgress(0);
    }

    private void onFinishListFilesThread(ListFilesThread threadObject) {
        boolean isOriginFilesView = threadObject.getPosIncremented() == 0;
        if (isOriginFilesView) {
            originFileList.setRoot(threadObject.getTreeViewRoot());
            int foldersCount = threadObject.getFoldersCount();
            int filesCount = threadObject.getFilesCount();
            originFilesCountLabel.setText("Carpetas: " + foldersCount + " | Archivos: " + filesCount);
            filesInOrigin = filesCount;
        } else {
            int foldersCount = threadObject.getFoldersCount();
            int filesCount = threadObject.getFilesCount();
            targetFileList.setRoot(threadObject.getTreeViewRoot());
            targetFilesCountLabel.setText("Carpetas: " + foldersCount + " | Archivos: " + filesCount);
        }
    }

    @Override
    public void setBackupSize(double value) {
        if (value >= 0) {
            backupSize = value;
        }
    }

    @Override
    public void appendErrors(StringBuilder errors) {
        errorsBox.appendText(errors.toString());
    }


}