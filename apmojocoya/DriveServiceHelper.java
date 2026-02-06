package com.example.apmojocoya;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveServiceHelper {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;

    public DriveServiceHelper(Drive driveService) {
        this.mDriveService = driveService;
    }

    public Task<String> uploadFile(java.io.File localFile, String mimeType, String folderName) {
        return Tasks.call(mExecutor, () -> {
            String folderId = getOrCreateFolder(folderName);

            File metadata = new File()
                    .setName(localFile.getName())
                    .setParents(Collections.singletonList(folderId));

            FileContent content = new FileContent(mimeType, localFile);

            File googleFile = mDriveService.files().create(metadata, content).execute();
            if (googleFile == null) {
                throw new IOException("Error al crear el archivo en Drive.");
            }

            return googleFile.getId();
        });
    }

    private String getOrCreateFolder(String folderName) throws IOException {
        String query = "mimeType = 'application/vnd.google-apps.folder' and name = '" + folderName + "' and trashed = false";
        FileList result = mDriveService.files().list().setQ(query).setSpaces("drive").execute();

        if (result.getFiles() != null && result.getFiles().size() > 0) {
            return result.getFiles().get(0).getId();
        } else {
            File folderMetadata = new File();
            folderMetadata.setName(folderName);
            folderMetadata.setMimeType("application/vnd.google-apps.folder");

            File folder = mDriveService.files().create(folderMetadata).setFields("id").execute();
            return folder.getId();
        }
    }
}