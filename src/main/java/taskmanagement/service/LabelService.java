package taskmanagement.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import taskmanagement.dto.label.LabelPatchRequestDto;
import taskmanagement.dto.label.LabelRequestDto;
import taskmanagement.dto.label.LabelResponseDto;

public interface LabelService {

    LabelResponseDto createLabel(LabelRequestDto request);

    LabelResponseDto updateLabel(Long labelId, LabelPatchRequestDto request);

    void deleteLabel(Long labelId);

    List<LabelResponseDto> getAllLabels(Pageable pageable);
}
