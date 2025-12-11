package taskmanagement.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import taskmanagement.config.MapperConfig;
import taskmanagement.dto.label.LabelPatchRequestDto;
import taskmanagement.dto.label.LabelRequestDto;
import taskmanagement.dto.label.LabelResponseDto;
import taskmanagement.model.Label;

@Mapper(config = MapperConfig.class)
public interface LabelMapper {

    LabelResponseDto toDto(Label model);

    Label toModel(LabelRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromPatch(LabelPatchRequestDto dto, @MappingTarget Label model);
}
