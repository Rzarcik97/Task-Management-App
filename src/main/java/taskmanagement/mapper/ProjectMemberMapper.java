package taskmanagement.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import taskmanagement.config.MapperConfig;
import taskmanagement.dto.projectmember.ProjectMemberDto;
import taskmanagement.model.ProjectMember;

@Mapper(config = MapperConfig.class)
public interface ProjectMemberMapper {
    @Mapping(source = "user.username", target = "username")
    ProjectMemberDto toDto(ProjectMember member);
}
