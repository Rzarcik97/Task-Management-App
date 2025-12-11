package taskmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import taskmanagement.config.MockDropboxConfig;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = MockDropboxConfig.class)
class TaskManagementAppApplicationTests {

    @Test
    void contextLoads() {
    }

}
