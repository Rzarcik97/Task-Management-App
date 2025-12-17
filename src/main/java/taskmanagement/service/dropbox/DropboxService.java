package taskmanagement.service.dropbox;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import taskmanagement.exceptions.FileStorageException;

@Service
@RequiredArgsConstructor
public class DropboxService {

    private final DbxClientV2 dropboxClient;

    public FileMetadata uploadFile(MultipartFile file, String folderPath)
            throws IOException, DbxException {
        String dropboxPath = folderPath + "/" + file.getOriginalFilename();
        try (InputStream inputStream = file.getInputStream()) {
            return dropboxClient.files()
                    .uploadBuilder(dropboxPath)
                    .uploadAndFinish(inputStream);
        }
    }

    public byte[] downloadFile(String filePath) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            dropboxClient.files().downloadBuilder(filePath).download(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new FileStorageException("Failed to download file from Dropbox", e);
        }
    }

    public void deleteFile(String dropboxFilePath) {
        try {
            dropboxClient.files().deleteV2(dropboxFilePath);
        } catch (DbxException e) {
            throw new FileStorageException(
                    "Failed to delete file from Dropbox: " + e.getMessage(), e);
        }
    }
}
