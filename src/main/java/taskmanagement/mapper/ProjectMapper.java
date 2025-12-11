package taskmanagement.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import taskmanagement.config.MapperConfig;
import taskmanagement.dto.project.ProjectPatchRequestDto;
import taskmanagement.dto.project.ProjectRequestDto;
import taskmanagement.dto.project.ProjectResponseDto;
import taskmanagement.model.Project;

@Mapper(config = MapperConfig.class,uses = {ProjectMemberMapper.class})
public interface ProjectMapper {

    ProjectResponseDto toDto(Project model);

    Project toModel(ProjectRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromPatch(ProjectPatchRequestDto dto, @MappingTarget Project model);
}
