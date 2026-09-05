package ewm.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {
    private Set<Long> events;

    private Boolean pinned = false;

    @NotBlank(message = "не должно быть пустым")
    @Size(min = 1, max = 50, message = "длина должна быть от 1 до 50 символов")
    private String title;
}
