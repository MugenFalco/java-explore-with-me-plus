package ewm.category;

import ewm.category.dto.CategoryDto;
import ewm.category.dto.NewCategoryDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CategoryMapper {

    public static Category toCategory(NewCategoryDto dto) {
        Category category = new Category();
        category.setName(dto.getName());
        return category;
    }

    public static CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }
}