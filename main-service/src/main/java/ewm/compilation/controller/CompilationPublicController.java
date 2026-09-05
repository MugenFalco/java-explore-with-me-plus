package ewm.compilation.controller;

import ewm.compilation.dto.CompilationDto;
import ewm.compilation.dto.CompilationSearchParams;
import ewm.compilation.service.CompilationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class CompilationPublicController {
    private final CompilationService compilationService;

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable final Long compId) {
        log.info("Полученине подборки событий c id {}", compId);
        return compilationService.getCompilationById(compId);
    }

    @GetMapping
    public List<CompilationDto> getCompilations(@Valid @ModelAttribute CompilationSearchParams params) {
        log.info("Получение подборок событий по параметрам from={}, size={}, pinned={}",
                params.getFrom(), params.getSize(), params.getPinned());
        return compilationService.getCompilationsByParams(params);
    }
}
