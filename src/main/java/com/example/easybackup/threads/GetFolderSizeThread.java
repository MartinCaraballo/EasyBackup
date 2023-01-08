package com.example.easybackup.threads;

import java.io.File;

public class GetFolderSizeThread extends Thread {

    private final File[] FILES;

    private double folderSize;

    public GetFolderSizeThread(File[] filesToCalcSize) {
        FILES = filesToCalcSize;
    }

    @Override
    public void run() {
        folderSize = getFolderSize(FILES);
    }

    public double getSizeValue() {
        return folderSize;
    }

    /**
     * returns the folder size in bytes.
     *
     * @param folderFileList Array of type File with all the elements in the folder.
     * @return size of the folder in bytes.
     */
    private double getFolderSize(File[] folderFileList) {
        double size = 0;
        for (File file : folderFileList) {
            if (file.isDirectory()) {
                File[] fileFiles = file.listFiles();
                if (fileFiles != null) {
                    size += getFolderSize(fileFiles);
                }
            } else {
                size += file.length();
            }
        }
        return size;
    }
}
