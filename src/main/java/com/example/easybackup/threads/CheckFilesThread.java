package com.example.easybackup.threads;

import com.example.easybackup.NotificationThread;
import javafx.util.Pair;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CheckFilesThread extends NotificationThread {
    private final File[] originFiles;
    private final Map<String, File> targetFilesMap = new HashMap<>();
    private final Map<String, File> filesNotFound = new HashMap<>();

    public CheckFilesThread(File[] originFilesList, File[] targetFilesList) {
        originFiles = originFilesList;
        convertFileArrayIntoMap(targetFilesList, targetFilesMap);
    }

    public Map<String, File> getFilesNotFound() {
        return filesNotFound;
    }


    @Override
    public Pair<Boolean, Object> doWork() {
        boolean result = checkFiles(originFiles);
        return new Pair<>(result, this);
    }


    /**
     * Method to convert an array into a map to reduce big O of the check files method
     * from O(n2) to O(n).
     * @param fileArray file array to convert.
     * @param fileMap   map to save the elements of the input array.
     */
    private void convertFileArrayIntoMap(File[] fileArray, Map<String, File> fileMap) {
        try {
            for (File file : fileArray) {
                if (file.isDirectory()) {
                    File[] fileFilesList = file.listFiles();
                    if (fileFilesList != null && fileFilesList.length > 0) {
                        convertFileArrayIntoMap(fileFilesList, fileMap);
                    }
                } else {
                    fileMap.put(file.getName(), file);
                }
            }
        } catch (Exception e) {
            getListener().appendErrors("Errores durante la conversión de array a mapa en el hilo de checkeo:\n" + e.getMessage());
        }
    }

    /**
     * Search's the origin elements in the map of the target files.
     * @param filesInOrigin Files in the origin folder to search in the map.
     * @return true if OK, false if it not does.
     */
    private boolean checkFiles(File[] filesInOrigin) {
        try {
            if (!targetFilesMap.isEmpty()) {
                for (File file : filesInOrigin) {
                    if (file.isDirectory()) {
                        File[] fileFilesList = file.listFiles();
                        if (fileFilesList != null && fileFilesList.length > 0) {
                            checkFiles(file.listFiles());
                        }
                    } else {
                        if (targetFilesMap.get(file.getName()) == null) {
                            filesNotFound.put(file.getName(), file);
                        }
                    }
                }
                return true;
            } {
                throw new Exception("No se pudo realizar el checkeo ya que no existen archivos en el destino.");
            }
        } catch (Exception e) {
            getListener().appendErrors(e.getMessage());
        }
        return false;
    }
}
