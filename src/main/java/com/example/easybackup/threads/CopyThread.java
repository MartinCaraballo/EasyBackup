package com.example.easybackup.threads;

import com.example.easybackup.EasyBackupController;
import com.example.easybackup.dialogs.CopyConfirmationDialogController;
import javafx.scene.control.Label;

/**
 * Thread that iterate over the files to be copied to calculate the backup size.
 */
public class GetBackupSizeThread extends Thread {

    private final CopyConfirmationDialogController copyDialogInstance;

    public GetBackupSizeThread(CopyConfirmationDialogController instance) {
        copyDialogInstance = instance;
    }

    @Override
    public void run() {
        double backupSize = EasyBackupController.getOriginFolderSize() / (1024 * 1024);
        copyDialogInstance.setBackupSizeLabel(backupSize > 999
                ? Math.round((backupSize / 1024) * 100.0) / 100.0 + " GB"
                : Math.round(backupSize * 100.0) / 100.0 + " MB"
        );
    }

}
