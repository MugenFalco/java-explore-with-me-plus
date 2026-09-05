package ewm.compilation.service;

import ewm.compilation.dto.CompilationDto;
import ewm.compilation.dto.CompilationSearchParams;
import ewm.compilation.dto.NewCompilationDto;
import ewm.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto create(NewCompilationDto compilation);

    CompilationDto getCompilationById(Long compId);

    List<CompilationDto> getCompilationsByParams(CompilationSearchParams compilationSearchParams);

    CompilationDto update(UpdateCompilationRequest updateCompilation, Long compId);

    void deleteCompilation(Long compId);
}
