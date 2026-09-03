package ewm.category;

import ewm.category.dto.CategoryDto;
import ewm.category.dto.NewCategoryDto;
import ewm.event.EventRepository;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto create(NewCategoryDto dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new ConflictException("Категория с таким названием уже существует.");
        }
        Category saved = categoryRepository.save(CategoryMapper.toCategory(dto));
        log.info("Создана категория с id {}", saved.getId());
        return CategoryMapper.toCategoryDto(saved);
    }

    @Override
    @Transactional
    public CategoryDto update(Long catId, CategoryDto dto) {
        Category category = findOrThrow(catId);

        if (categoryRepository.existsByNameAndIdNot(dto.getName(), catId)) {
            throw new ConflictException("Категория с таким названием уже существует.");
        }

        category.setName(dto.getName());
        log.info("Обновлена категория с id {}", catId);
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    @Transactional
    public void delete(Long catId) {
        findOrThrow(catId);
        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("Невозможно удалить категорию: с ней связаны события.");
        }
        categoryRepository.deleteById(catId);
        log.info("Удалена категория с id {}", catId);
    }

    @Override
    public List<CategoryDto> getAll(int from, int size) {
        return categoryRepository.findAll(PageRequest.of(from / size, size)).stream()
                .map(CategoryMapper::toCategoryDto)
                .toList();
    }

    @Override
    public CategoryDto getById(Long catId) {
        return CategoryMapper.toCategoryDto(findOrThrow(catId));
    }

    private Category findOrThrow(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с идентификатором " + catId + " не найдена."));
    }
}
