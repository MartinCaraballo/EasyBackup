package com.example.easybackup.threads;

import com.example.easybackup.NotificationThread;
import javafx.util.Pair;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Thread that iterate over the files to be copied to calculate the backup size.
 */
public class CopyThread extends NotificationThread {

    private static int filesCopied;
    private final StringBuilder messagesBox;

    private final File originFile;
    private final File targetFile;
    private final String targetFolderName;
    private final boolean separateImgsAndVideos;

    public CopyThread(File aOriginFile, File aTargetFile, String aFolderName, boolean separate) {
        originFile = aOriginFile;
        targetFile = aTargetFile;
        targetFolderName = aFolderName;
        separateImgsAndVideos = separate;
        messagesBox = new StringBuilder("ERRORES DURANTE LA COPIA:");
    }

    @Override
    public Pair<Boolean, Object> doWork() {
        filesCopied = 0;
        boolean result;
        if (separateImgsAndVideos) {
            result = copyFilesSeparated(originFile, targetFile, targetFolderName);
        } else {
            result = copyFiles(originFile, targetFile, targetFolderName);
        }
        return new Pair<>(result, this);
    }

    public static int getFilesCopiedValue() {
        return filesCopied;
    }


    /**
     * @param originFile Origin folder of the copy.
     * @param targetFile Target folder of the copy (backup folder).
     * @param targetFolderName Name of the backup folder (destiny folder).
     * @return boolean: true if OK, false if it not does.
     */
    private boolean copyFiles(File originFile, File targetFile, String targetFolderName) {
        try {
            File[] originFileList = originFile.listFiles();
            if (originFileList != null) {
                File targetFolder = new File(targetFile.getPath() + "/" + targetFolderName);
                byte repeatedFolderCount = 1;
                while (targetFolder.exists()) {
                    String newName = targetFolder.getPath() + " (" + repeatedFolderCount++ + ")";
                    targetFolder = new File(newName);
                }
                boolean targetFolderCreatedResult = targetFolder.mkdir();
                if (targetFolderCreatedResult) {
                    for (File file : originFileList) {
                        if (file.isDirectory()) {
                            copyFiles(file, targetFolder, file.getName());
                        } else {
                            File trgFile = new File(targetFolder.getPath() + "/" + file.getName());
                            Files.copy(file.toPath(), trgFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            filesCopied++;
                        }
                    }
                    return true;
                }
                return false;
            }
            return false;
        } catch(Exception e) {
            messagesBox.append("\n").append(e.getMessage());
            getListener().appendErrors(messagesBox);
        }
        return false;
    }

    /**
     * @param originFile Origin folder of the copy.
     * @param targetFile Destiny folder of the copy (backup folder).
     * @param targetFolderName Name of the backup folder (destiny folder).
     * @return boolean: true if the copy was successful, false if it not does.
     */
    private boolean copyFilesSeparated(File originFile, File targetFile, String targetFolderName) {
        return true;
    }

}
