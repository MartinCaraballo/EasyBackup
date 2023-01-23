package com.example.easybackup.threads;

import com.example.easybackup.NotificationThread;
import javafx.util.Pair;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class CopyThread extends NotificationThread {

    private static int filesCopied;

    private final File originFile;
    private final File targetFile;
    private final String targetFolderName;
    private final boolean separateImgsAndVideos;

    public CopyThread(File aOriginFile, File aTargetFile, String aFolderName, boolean separate) {
        originFile = aOriginFile;
        targetFile = aTargetFile;
        targetFolderName = aFolderName;
        separateImgsAndVideos = separate;
    }

    public File getTargetFile() { return targetFile; }


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
            if (originFileList != null && originFileList.length > 0) {
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
                } else {
                    throw new Exception("No se pudo crear la carpeta \"" + targetFolder.getName() + "\" en {" + targetFolder.getPath() + "}.");
                }
            } else {
                if (originFileList == null) {
                    throw new Exception("No se copió el archivo ubicado en {" + originFile.getPath() + "} porque no es un directorio.");
                } else if (originFileList.length == 0) {
                    throw new Exception("No se copió la carpeta ubicada en {" + originFile.getPath() + "} porque está vacía.");
                }
            }
        } catch(Exception e) {
            getListener().appendErrors(e.getMessage() + '\n');
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
        try {
            File[] originFileList = originFile.listFiles();
            if (originFileList != null && originFileList.length > 0) {
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
                            copyFilesSeparated(file, targetFolder, file.getName());
                        } else {
                            String fileType = Files.probeContentType(file.toPath());
                            if (fileType != null) {
                                if (fileType.startsWith("image")) {
                                    File imagesFolder = new File(targetFolder.getPath() + "/images");
                                    if (!imagesFolder.exists()) {
                                        boolean result = imagesFolder.mkdir();
                                        if (!result)
                                            throw new Exception("Error al crear la carpeta {/images} en la ruta {" + targetFolder.getPath() + "}.");
                                    }
                                    File trgFile = new File(imagesFolder.getPath() + "/" + file.getName());
                                    Files.copy(file.toPath(), trgFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                    filesCopied++;

                                } else if (fileType.startsWith("video")) {
                                    File videosFolder = new File(targetFolder.getPath() + "/videos");
                                    if (!videosFolder.exists()) {
                                        boolean result = videosFolder.mkdir();
                                        if (!result)
                                            throw new Exception("Error al crear la carpeta {/videos} en la ruta {" + targetFolder.getPath() + "}.");
                                    }
                                    File trgFile = new File(videosFolder.getPath() + "/" + file.getName());
                                    Files.copy(file.toPath(), trgFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                    filesCopied++;
                                } else {
                                    File otherFilesFolder = new File(targetFolder.getPath() + "/otros");
                                    if (!otherFilesFolder.exists()) {
                                        boolean result = otherFilesFolder.mkdir();
                                        if (!result)
                                            throw new Exception("Error al crear la carpeta {/otros} en la ruta {" + targetFolder.getPath() + "}.");
                                    }
                                    File trgFile = new File(otherFilesFolder.getPath() + "/" + file.getName());
                                    Files.copy(file.toPath(), trgFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                    filesCopied++;
                                }
                            } else {
                                getListener().appendErrors("No se pudo comprobar el tipo de archivo de {" + file.getPath() + "}.");
                            }
                        }
                    }
                    return true;
                } else {
                    throw new Exception("No se pudo crear la carpeta \"" + targetFolder.getName() + "\" en {" + targetFolder.getPath() + "}.");
                }
            } else {
                if (originFileList == null) {
                    throw new Exception("No se copió el archivo ubicado en {" + originFile.getPath() + "} porque no es un directorio.");
                } else if (originFileList.length == 0) {
                    throw new Exception("No se copió la carpeta ubicada en {" + originFile.getPath() + "} porque está vacía.");
                }
            }
        } catch (Exception e) {
            getListener().appendErrors(e.getMessage() + '\n');
        }
        return false;
    }

}
