package taskmanagement.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import taskmanagement.config.MapperConfig;
import taskmanagement.dto.user.UserPatchRequestDto;
import taskmanagement.dto.user.UserRegistrationRequestDto;
import taskmanagement.dto.user.UserResponseDto;
import taskmanagement.model.User;

@Mapper(config = MapperConfig.class)
public interface UserMapper {

    @Mapping(target = "username", source = "usernameField")
    UserResponseDto toDto(User model);

    User registerModelFromDto(UserRegistrationRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromPatch(UserPatchRequestDto dto, @MappingTarget User model);
}
