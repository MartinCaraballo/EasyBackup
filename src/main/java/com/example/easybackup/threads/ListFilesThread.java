package com.example.easybackup.threads;

import com.example.easybackup.NotificationThread;
import javafx.scene.control.TreeItem;
import javafx.util.Pair;

import java.io.File;

public class ListFilesThread extends NotificationThread {
    private double backupSize;
    private int filesCount;
    private int foldersCount;

    private final File[] FILES;
    private final TreeItem<String> ROOT_ITEM;
    private final int VIEW_INDEX;

    public ListFilesThread(File[] filesToList, TreeItem<String> rootItem, int viewIndex) {
        FILES = filesToList;
        ROOT_ITEM = rootItem;
        VIEW_INDEX = viewIndex;
    }


    public int getViewIndex() {
        return VIEW_INDEX;
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

    public double getBackupSize() {
        return backupSize;
    }


    @Override
    public Pair<Boolean, Object> doWork() {
        boolean result = listFiles(FILES, ROOT_ITEM, VIEW_INDEX);
        return new Pair<>(result, this);
    }

    /**
     * @param files            folder with the files to list in the TreeView.
     * @param parentFolderItem TreeItem's parent to append the child file.
     * @param viewIndex        0 for origin list, 1 for target list.
     * @return true if OK, false if it not does.
     */
    private boolean listFiles(File[] files, TreeItem<String> parentFolderItem, int viewIndex) {
        try {
            for (File file : files) {
                if (file.isDirectory()) {
                    foldersCount++;
                    TreeItem<String> parent = new TreeItem<>(file.getName());
                    parentFolderItem.getChildren().add(parent);
                    File[] fileFiles = file.listFiles();
                    if (fileFiles != null) {
                        listFiles(fileFiles, parent, viewIndex);
                    }
                } else {
                    filesCount++;
                    if (viewIndex == 0) {
                        backupSize += file.length();
                    }
                    parentFolderItem.getChildren().add(new TreeItem<>("» " + file.getName()));
                }
            }
            return true;
        } catch (Exception e) {
            getListener().appendErrors(e.getMessage());
        }
        return false;
    }

}
