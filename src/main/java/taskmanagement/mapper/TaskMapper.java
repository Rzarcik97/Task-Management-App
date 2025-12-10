package taskmanagement.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import taskmanagement.config.MapperConfig;
import taskmanagement.dto.task.TaskPatchRequestDto;
import taskmanagement.dto.task.TaskRequestDto;
import taskmanagement.dto.task.TaskResponseDto;
import taskmanagement.model.Label;
import taskmanagement.model.Task;

@Mapper(config = MapperConfig.class)
public interface TaskMapper {

    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "assigneeEmail", source = "assignee.username")
    @Mapping(target = "labels", expression = "java(mapLabels(model.getLabels()))")
    TaskResponseDto toDto(Task model);

    Task toModel(TaskRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromPatch(TaskPatchRequestDto dto, @MappingTarget Task model);

    default Set<String> mapLabels(Set<Label> labels) {
        return labels == null
                ? Set.of()
                : labels.stream().map(Label::getName).collect(Collectors.toSet());
    }
}
