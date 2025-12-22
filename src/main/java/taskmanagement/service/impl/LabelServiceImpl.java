package taskmanagement.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import taskmanagement.dto.label.LabelPatchRequestDto;
import taskmanagement.dto.label.LabelRequestDto;
import taskmanagement.dto.label.LabelResponseDto;
import taskmanagement.exceptions.EntityNotFoundException;
import taskmanagement.mapper.LabelMapper;
import taskmanagement.model.Label;
import taskmanagement.repository.LabelRepository;
import taskmanagement.service.LabelService;

@Log4j2
@RequiredArgsConstructor
@Service
public class LabelServiceImpl implements LabelService {

    private final LabelRepository labelRepository;
    private final LabelMapper labelMapper;

    @Override
    public LabelResponseDto createLabel(LabelRequestDto request) {
        log.info("Starting creating label: name = {}", request.name());
        if (labelRepository.existsByName(request.name())) {
            throw new IllegalArgumentException(
                    "Label with name '" + request.name() + "' already exists");
        }
        Label label = labelMapper.toModel(request);
        Label saved = labelRepository.save(label);
        log.info("label created successfully: id = {}", saved.getId());
        return labelMapper.toDto(saved);
    }

    @Override
    public LabelResponseDto updateLabel(Long labelId, LabelPatchRequestDto request) {
        log.info("Starting editing label: id={}", labelId);
        if (labelRepository.existsByName(request.name())) {
            throw new IllegalArgumentException(
                    "Label with name '" + request.name() + "' already exists");
        }
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Label with id " + labelId + " not found"));
        labelMapper.updateFromPatch(request, label);
        Label updated = labelRepository.save(label);
        log.info("label edited successfully");
        return labelMapper.toDto(updated);
    }

    @Override
    public List<LabelResponseDto> getAllLabels(Pageable pageable) {
        return labelRepository.findAll(pageable).stream()
                .map(labelMapper::toDto)
                .toList();
    }

    @Override
    public void deleteLabel(Long labelId) {
        log.info("Starting deleting label: id = {}", labelId);
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new EntityNotFoundException("Label not found"));
        labelRepository.delete(label);
        log.info("label deleted successfully");
    }
}
