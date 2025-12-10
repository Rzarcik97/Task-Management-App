package taskmanagement.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dropbox.core.v2.files.FileMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import taskmanagement.dto.attachment.AttachmentResponseDto;
import taskmanagement.service.dropbox.DropboxService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@Testcontainers
public class AttachmentControllerTest {

    protected static MockMvc mockMvc;

    @MockitoBean
    private DropboxService dropboxService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    public static void setUp(
            @Autowired WebApplicationContext ctx
    ) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(ctx)
                .apply(springSecurity())
                .build();
    }

    @WithMockUser(username = "john.doe@example.com")
    @Test
    @DisplayName("Upload attachment - valid request - success")
    void uploadAttachment_validRequest_success() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "testfile.txt",
                "text/plain",
                "Hello world".getBytes()
        );

        FileMetadata metadata = mock(FileMetadata.class);

        when(metadata.getId()).thenReturn("fake-id-123");
        when(metadata.getPathLower()).thenReturn("/fake/path.txt");
        when(metadata.getName()).thenReturn("path.txt");

        when(dropboxService.uploadFile(any(), any()))
                .thenReturn(metadata);

        MvcResult result = mockMvc.perform(
                        multipart("/attachments")
                                .file(file)
                                .param("taskId", "2")
                )
                .andExpect(status().isOk())
                .andReturn();

        AttachmentResponseDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AttachmentResponseDto.class
        );

        assertNotNull(response.id());
        assertNotNull(response.dropboxFileId());
        assertNotNull(response.filename());
    }

    @Test
    @DisplayName("Upload file - without authentication - should return 401")
    void upload_Unauthorized_Returns401() throws Exception {
        MockMultipartFile file =
                new MockMultipartFile("file", "a.txt", "text/plain", "aaa".getBytes());

        mockMvc.perform(multipart("/attachments").file(file))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser
    @Test
    @DisplayName("Upload file - empty file - should return 400")
    void upload_EmptyFile_ReturnsBadRequest() throws Exception {
        MockMultipartFile emptyFile =
                new MockMultipartFile("file", "", "text/plain", new byte[0]);

        mockMvc.perform(multipart("/attachments")
                        .file(emptyFile))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(username = "john.doe@example.com")
    @Test
    @DisplayName("Get attachments for task - returns list")
    void getAttachments_validRequest_success() throws Exception {

        MvcResult result = mockMvc.perform(
                        get("/attachments/1")

                )
                .andExpect(status().isOk())
                .andReturn();

        List<AttachmentResponseDto> list = Arrays.asList(
                objectMapper.readValue(
                        result.getResponse().getContentAsString(),
                        AttachmentResponseDto[].class
                )
        );

        assertFalse(list.isEmpty());
    }

    @WithMockUser(username = "john.doe@example.com")
    @Test
    @DisplayName("Download attachment - returns file")
    void downloadAttachment_validRequest_success() throws Exception {

        mockMvc.perform(
                        get("/attachments/1/download")
                )
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Type"))
                .andExpect(content().bytes(new byte[]{}));
    }

    @WithMockUser(username = "john.doe@example.com")
    @Test
    @DisplayName("Get attachment by task id - non-existing attachment - should return 404")
    void getAttachment_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/attachments/99999"))
                .andExpect(status().isNotFound());
    }

    @WithMockUser("anna@example.com")
    @Test
    @DisplayName("Get attachment by task id - access denied - should return 403")
    void getAttachment_Forbidden_Returns403() throws Exception {

        mockMvc.perform(get("/attachments/1"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(username = "john.doe@example.com")
    @Test
    @DisplayName("Delete attachment - success")
    void deleteAttachment_validRequest_success() throws Exception {

        mockMvc.perform(
                        delete("/attachments/2/delete")
                )
                .andExpect(status().isOk());
    }

    @WithMockUser
    @Test
    @DisplayName("Delete attachment - non-existing attachment - should return 404")
    void deleteAttachment_NotFound_Returns404() throws Exception {
        mockMvc.perform(delete("/attachments/9999/delete"))
                .andExpect(status().isNotFound());
    }
}
