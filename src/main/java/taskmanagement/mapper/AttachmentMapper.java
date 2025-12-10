package taskmanagement.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import taskmanagement.config.MapperConfig;
import taskmanagement.dto.attachment.AttachmentResponseDto;
import taskmanagement.model.Attachment;

@Mapper(config = MapperConfig.class)
public interface AttachmentMapper {

    @Mapping(source = "task.name", target = "taskName")
    @Mapping(source = "uploadedBy.username", target = "uploadedBy")
    AttachmentResponseDto toDto(Attachment model);
}
