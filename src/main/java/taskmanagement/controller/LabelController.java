package taskmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import taskmanagement.dto.label.LabelPatchRequestDto;
import taskmanagement.dto.label.LabelRequestDto;
import taskmanagement.dto.label.LabelResponseDto;
import taskmanagement.service.LabelService;

@Log4j2
@RestController
@RequestMapping("/labels")
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create Label",
            description = "Create a new label")
    public LabelResponseDto createLabel(
            @RequestBody @Valid LabelRequestDto request,
            Authentication authentication) {
        String email = authentication.getName();
        log.info("Creating Label {}, by User {}", request.name(), email);
        return labelService.createLabel(request);
    }

    @GetMapping
    @Operation(summary = "Get Labels",
            description = "Retrieve all available labels")
    public List<LabelResponseDto> getLabels() {
        return labelService.getAllLabels();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    @Operation(summary = "Update Label",
            description = "Update an existing label by ID")
    public LabelResponseDto updateLabel(@PathVariable Long id,
                                        @RequestBody LabelPatchRequestDto request,
                                        Authentication authentication) {
        String email = authentication.getName();
        log.info("Editing Label {}, by User {}", request.name(), email);
        return labelService.updateLabel(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{labelId}")
    @Operation(summary = "Delete Label",
            description = "Delete a label by ID")
    public void deleteLabel(@PathVariable Long labelId,
                            Authentication authentication) {
        String email = authentication.getName();
        log.info("Deleting Label {}, by User {}", labelId, email);
        labelService.deleteLabel(labelId);
    }
}
