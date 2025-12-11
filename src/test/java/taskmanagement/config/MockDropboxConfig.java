package taskmanagement.config;

import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadBuilder;
import java.util.Date;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class MockDropboxConfig {

    @Bean
    public DbxClientV2 dropboxClient() throws Exception {
        DbxClientV2 client = Mockito.mock(DbxClientV2.class);
        DbxUserFilesRequests files = Mockito.mock(DbxUserFilesRequests.class);
        UploadBuilder uploadBuilder = Mockito.mock(UploadBuilder.class);

        Mockito.when(client.files()).thenReturn(files);
        Mockito.when(files.uploadBuilder(Mockito.anyString()))
                .thenReturn(uploadBuilder);
        Mockito.when(uploadBuilder.uploadAndFinish(Mockito.any()))
                .thenReturn(
                        new FileMetadata("testFile",
                                "testfileid",
                                new Date(2005, 10, 2),
                                new Date(2005, 11, 2),
                                "abc123456789",
                                100L)
                );

        return client;
    }
}
