package com.example.easybackup;

import com.example.easybackup.dialogs.CheckFilesResultDialog;
import com.example.easybackup.dialogs.CopyConfirmationDialog;
import com.example.easybackup.threads.CheckFilesThread;
import com.example.easybackup.threads.CopyThread;
import com.example.easybackup.threads.ListFilesThread;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import javafx.util.Pair;

import java.io.File;
import java.util.Map;
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
    private CheckFilesThread checkFilesThread;
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
                listFilesThread.start();
            } else {
                errorsBox.appendText("La carpeta seleccionada no existe\n");
            }
        } catch (Exception e) {
            errorsBox.appendText(e.getMessage() + '\n');
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
                listFilesThread.start();
            } else {
                errorsBox.appendText("La carpeta seleccionada no existe.\n");
            }
        } catch (Exception e) {
            errorsBox.appendText(e.getMessage() + '\n');
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
                String defaultFolderName = "Respaldo de CD " + originFolder.getName();
                Dialog<Pair<String, Boolean>> copyConfirmationDialog = CopyConfirmationDialog.createDialog(defaultFolderName, sizeText, filesInOrigin);
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
                }, 0, 10);
                dialogResult.ifPresent(result -> {
                    // COPIAR
                    copyThread = new CopyThread(originFolder, targetFolder, result.getKey(), result.getValue());
                    copyThread.setListener(this);
                    copyThread.start();
                });

            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error!");
                errorAlert.setHeaderText("Error al presionar el botón copiar");
                errorAlert.setContentText("Antes de pulsar el botón para copiar, debes seleccionar una carpeta de origen y una de destino.");
                errorAlert.show();
            }
        } catch (Exception e) {
            errorsBox.appendText(e.getMessage() + '\n');
        }
    }

    @FXML
    private void onCheckButtonClick() {
        try {
            checkProgressIndicator.setVisible(true);
            checkProgressIndicator.setProgress(-1d);
            File[] filesInOriginArray = new File(originPath.getText()).listFiles();
            File[] filesInTargetArray = new File(targetPath.getText()).listFiles();
            if ((filesInOriginArray != null && filesInTargetArray != null)
                    && (filesInOriginArray.length > 0 && filesInTargetArray.length > 0)) {
                checkFilesThread = new CheckFilesThread(filesInOriginArray, filesInTargetArray);
                checkFilesThread.setListener(this);
                checkFilesThread.start();
            } else {
                if (filesInOriginArray == null) {
                    throw new Exception("La ruta de origen no hace referencia a un directorio.");
                } else if (filesInTargetArray == null) {
                    throw new Exception("La ruta de destino no hace referencia a un directorio.");
                } else if (filesInOriginArray.length == 0) {
                    throw new Exception("El directorio de origen no contiene elementos.");
                } else if (filesInTargetArray.length == 0) {
                    throw new Exception("El directorio de destino no contiene elementos.");
                }
            }
        } catch (Exception e) {
            errorsBox.appendText(e.getMessage() + '\n');
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
                listFilesThread.start();
            } else {
                if (!pathWritten.exists()) {
                    throw new Exception("El path ingresado no existe.");
                } else if (!pathWritten.isDirectory()) {
                    throw new Exception("El path ingresado no hace referencia a una carpeta.");
                }
            }
        } catch (Exception e) {
            errorsBox.appendText(e.getMessage() + '\n');
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
                listFilesThread.start();
            } else {
                if (!targetPathWritten.exists()) {
                    throw new Exception("El path ingresado no existe.");
                } else if (!targetPathWritten.isDirectory()) {
                    throw new Exception("El path ingresado no hace referencia a una carpeta.");
                }
            }
        } catch (Exception e) {
            errorsBox.appendText(e.getMessage() + '\n');
        }
    }

    @Override
    public void threadCompleteExecution(Runnable thread, boolean executionResult, Object threadObject) {
        try {
            Platform.runLater(() -> {
                if (threadObject.getClass() == CopyThread.class) {
                    onFinishCopyThread(executionResult, (CopyThread) threadObject);
                } else if (threadObject.getClass() == ListFilesThread.class) {
                    onFinishListFilesThread((ListFilesThread) threadObject);
                } else if (threadObject.getClass() == CheckFilesThread.class) {
                    onFinishCheckFilesThread((CheckFilesThread) threadObject);
                }
            });
        } catch (Exception e) {
            errorsBox.appendText(e.getMessage() + '\n');
        }
    }

    private void onFinishCopyThread(boolean executionResult, CopyThread copyThread) {
        try {
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
            File targetFolder = copyThread.getTargetFile();
            TreeItem<String> newRoot = new TreeItem<>(targetFolder.getName());
            listFilesThread = new ListFilesThread(targetFolder.listFiles(), newRoot, 1);
            listFilesThread.setListener(this);
            listFilesThread.start();
            copyResult.showAndWait();
            copyProgressBar.setProgress(0);
        } catch (Exception e) {
            errorsBox.appendText(e.getMessage() + '\n');
        }
    }

    private void onFinishListFilesThread(ListFilesThread threadObject) {
        boolean isOriginFilesView = threadObject.getViewIndex() == 0;
        if (isOriginFilesView) {
            backupSize = threadObject.getBackupSize();
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

    private void onFinishCheckFilesThread(CheckFilesThread checkFilesThread) {
        try {
            checkProgressIndicator.setProgress(0d);
            checkProgressIndicator.setVisible(false);
            Map<String, File> filesNotFound = checkFilesThread.getFilesNotFound();
            if (!filesNotFound.isEmpty()) {
                Dialog<String> checkFilesFinishDialog = CheckFilesResultDialog.createDialog(filesNotFound);
                checkFilesFinishDialog.show();
            } else {
                Alert finishCheckAlert = new Alert(Alert.AlertType.INFORMATION);
                finishCheckAlert.setTitle("Resultado del checkeo");
                finishCheckAlert.setHeaderText("Checkeo finalizado con éxito");
                finishCheckAlert.setContentText("Todos los archivos del origen se encuentran en el destino.");
                finishCheckAlert.show();
            }
        } catch (Exception e) {
            errorsBox.appendText(e.getMessage() + '\n');
        }
    }

    @Override
    public void appendErrors(String error) {
        errorsBox.appendText(error);
    }

}