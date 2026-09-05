package ewm.compilation.controller;

import ewm.compilation.dto.CompilationDto;
import ewm.compilation.dto.NewCompilationDto;
import ewm.compilation.dto.UpdateCompilationRequest;
import ewm.compilation.service.CompilationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
public class CompilationAdminController {
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto create(@Valid @RequestBody final NewCompilationDto compilation) {
        log.info("Создание подборки событий {}", compilation);
        return compilationService.create(compilation);
    }

    @PatchMapping("/{compId}")
    public CompilationDto update(
            @PathVariable final Long compId,
            @Valid @RequestBody final UpdateCompilationRequest newCompilation
    ) {
        log.info("Обновление подборки событий с id {}", compId);
        return compilationService.update(newCompilation, compId);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final Long compId) {
        log.info("Удаление подборки событий c id {}", compId);
        compilationService.deleteCompilation(compId);
    }
}
