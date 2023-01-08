package com.example.easybackup.threads;

import com.example.easybackup.NotificationThread;
import com.example.easybackup.TaskListener;
import javafx.scene.control.TreeItem;
import javafx.util.Pair;

import java.io.File;

public class ListFilesThread extends NotificationThread {
    private double backupSize;
    private int filesCount;
    private int foldersCount;
    private final StringBuilder errorsBox = new StringBuilder("ERRORES DURANTE EL LISTADO DE ARCHIVOS:");

    private final File[] FILES;
    private final TreeItem<String> ROOT_ITEM;
    private final int POS_TO_INCREMENT;

    public ListFilesThread(File[] filesToList, TreeItem<String> rootItem, int posToIncrement) {
        FILES = filesToList;
        ROOT_ITEM = rootItem;
        POS_TO_INCREMENT = posToIncrement;
    }

    public int getPosIncremented() {
        return POS_TO_INCREMENT;
    }

    public TreeItem<String> getTreeViewRoot() {
        return ROOT_ITEM;
    }

    public int getFoldersCount() {
        return foldersCount;
    }

    public int getFilesCount() {
        return filesCount;
    }


    @Override
    public Pair<Boolean, Object> doWork() {
        boolean result = listFiles(FILES, ROOT_ITEM, POS_TO_INCREMENT);
        TaskListener listener = getListener();
        if (POS_TO_INCREMENT == 0) {
            listener.setBackupSize(backupSize);
        }
        return new Pair<>(result, this);
    }

    /**
     * @param files            folder with the files to list in the TreeView.
     * @param parentFolderItem TreeItem's parent to append the child file.
     * @param posToIncrement   index of the item counter in the array of files counters.
     */
    private boolean listFiles(File[] files, TreeItem<String> parentFolderItem, int posToIncrement) {
        try {
            for (File file : files) {
                if (file.isDirectory()) {
                    foldersCount++;
                    TreeItem<String> parent = new TreeItem<>(file.getName());
                    parentFolderItem.getChildren().add(parent);
                    File[] fileFiles = file.listFiles();
                    if (fileFiles != null) {
                        listFiles(fileFiles, parent, posToIncrement);
                    }
                } else {
                    filesCount++;
                    if (posToIncrement == 0) {
                        backupSize += file.length();
                    }
                    parentFolderItem.getChildren().add(new TreeItem<>("» " + file.getName()));
                }
            }
            return true;
        } catch (Exception e) {
            errorsBox.append("\n").append(e.getMessage());
            getListener().appendErrors(errorsBox);
        }
        return false;
    }
}
