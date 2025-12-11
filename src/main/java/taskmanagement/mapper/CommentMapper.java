package taskmanagement.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import taskmanagement.config.MapperConfig;
import taskmanagement.dto.comment.CommentRequestDto;
import taskmanagement.dto.comment.CommentResponseDto;
import taskmanagement.model.Comment;

@Mapper(config = MapperConfig.class)
public interface CommentMapper {

    @Mapping(source = "task.name", target = "taskName")
    @Mapping(source = "user.username", target = "authorUsername")
    CommentResponseDto toDto(Comment model);
    
    Comment toModel(CommentRequestDto dto);

    void updateFromPatch(String text, @MappingTarget Comment model);
}
