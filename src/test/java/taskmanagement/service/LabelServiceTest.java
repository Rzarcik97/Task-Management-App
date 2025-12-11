package taskmanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import taskmanagement.dto.label.LabelPatchRequestDto;
import taskmanagement.dto.label.LabelRequestDto;
import taskmanagement.dto.label.LabelResponseDto;
import taskmanagement.exceptions.EntityNotFoundException;
import taskmanagement.mapper.LabelMapper;
import taskmanagement.mapper.impl.LabelMapperImpl;
import taskmanagement.model.Label;
import taskmanagement.repository.LabelRepository;
import taskmanagement.service.impl.LabelServiceImpl;

@ExtendWith(MockitoExtension.class)
class LabelServiceTest {

    @Mock
    private LabelRepository labelRepository;

    @Spy
    private LabelMapper labelMapper = new LabelMapperImpl();

    @InjectMocks
    private LabelServiceImpl labelService;

    @Test
    @DisplayName("""
            createLabel | validate that method create label successfully
             when name is unique
            """)
    void createLabel_validEntry_success() {
        // given

        Label toSave = new Label();
        toSave.setName("Bug");

        Label saved = new Label();
        saved.setId(1L);
        saved.setName("Bug");

        LabelRequestDto request = new LabelRequestDto(
                "Bug",
                null);

        when(labelRepository.existsByName(toSave.getName()))
                .thenReturn(false);
        when(labelMapper.toModel(request)).thenReturn(toSave);
        when(labelRepository.save(toSave)).thenReturn(saved);

        // when
        LabelResponseDto result = labelService.createLabel(request);

        // then
        assertEquals(1L, result.id());
        assertEquals(request.name(), result.name());

        verify(labelRepository).existsByName(toSave.getName());
        verify(labelRepository).save(toSave);
        verify(labelMapper).toModel(request);
        verify(labelMapper).toDto(saved);
    }

    @Test
    @DisplayName("""
            createLabel | validate that method throw IllegalArgumentException
             when label name already exists
            """)
    void createLabel_nameExists_throwsException() {
        // given
        LabelRequestDto request = new LabelRequestDto(
                "Bug",
                null);
        when(labelRepository.existsByName(request.name())).thenReturn(true);

        // when + then
        assertThrows(IllegalArgumentException.class,
                () -> labelService.createLabel(request));

        verify(labelRepository).existsByName(request.name());
        verify(labelRepository, never()).save(any());
        verify(labelMapper, never()).toModel(any());
    }

    @Test
    @DisplayName("""
            updateLabel | validate that method update label successfully
            """)
    void updateLabel_validEntry_success() {
        // given

        Label label = new Label();
        label.setId(1L);
        label.setName("OldName");

        Label updated = new Label();
        updated.setId(1L);
        updated.setName("NewName");

        Long labelId = 1L;

        LabelPatchRequestDto request = new LabelPatchRequestDto(
                "NewName",
                null);

        when(labelRepository.findById(labelId)).thenReturn(Optional.of(label));
        when(labelRepository.save(label)).thenReturn(updated);

        // when
        LabelResponseDto result = labelService.updateLabel(labelId, request);

        // then
        assertEquals(request.name(), result.name());

        verify(labelRepository).findById(labelId);
        verify(labelMapper).updateFromPatch(request, label);
        verify(labelRepository).save(label);
        verify(labelMapper).toDto(updated);
    }

    @Test
    @DisplayName("""
            updateLabel | validate that method throw IllegalArgumentException
             when label name already exists
            """)
    void updateLabel_nameExists_throwsException() {
        // given
        LabelRequestDto request = new LabelRequestDto(
                "Bug",
                null);
        when(labelRepository.existsByName(request.name())).thenReturn(true);

        // when + then
        assertThrows(IllegalArgumentException.class,
                () -> labelService.createLabel(request));

        verify(labelRepository).existsByName(request.name());
        verify(labelRepository, never()).save(any());
        verify(labelRepository, never()).findById(any());
        verify(labelMapper, never()).toModel(any());
    }

    @Test
    @DisplayName("""
            updateLabel | validate that method throw EntityNotFoundException
             when label does not exist
            """)
    void updateLabel_notFound_throwsException() {
        // given
        Long id = 1L;
        LabelPatchRequestDto request = new LabelPatchRequestDto(
                "missing",
                null);

        when(labelRepository.findById(id)).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> labelService.updateLabel(id, request));

        verify(labelRepository).findById(id);
        verify(labelRepository, never()).save(any());
    }

    @Test
    @DisplayName("""
            getAllLabels | validate that method return list of mapped labels
            """)
    void getAllLabels_validEntry_success() {
        // given
        Label l1 = new Label();
        l1.setId(1L);
        l1.setName("Bug");
        Label l2 = new Label();
        l2.setId(2L);
        l2.setName("Feature");

        when(labelRepository.findAll()).thenReturn(List.of(l1, l2));

        // when
        List<LabelResponseDto> result = labelService.getAllLabels();

        // then
        assertEquals(2, result.size());
        assertEquals("Bug", result.get(0).name());
        assertEquals("Feature", result.get(1).name());

        verify(labelRepository).findAll();
        verify(labelMapper, times(2)).toDto(any());
    }

    @Test
    @DisplayName("deleteLabel | validate that method delete label when exists")
    void deleteLabel_validEntry_success() {
        // given
        Long id = 1L;

        Label label = new Label();
        label.setId(1L);
        label.setName("Bug");

        when(labelRepository.findById(id)).thenReturn(Optional.of(label));

        // when
        labelService.deleteLabel(id);

        // then
        verify(labelRepository).findById(id);
        verify(labelRepository).delete(label);
    }

    @Test
    @DisplayName("""
            deleteLabel | validate that method throw EntityNotFoundException
             when label does not exist
            """)
    void deleteLabel_notFound_throwsException() {
        // given
        Long id = 1L;
        when(labelRepository.findById(id)).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> labelService.deleteLabel(id));

        verify(labelRepository).findById(id);
        verify(labelRepository, never()).delete(any());
    }
}

