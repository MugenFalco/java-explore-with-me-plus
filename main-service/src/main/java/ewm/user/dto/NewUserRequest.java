package ewm.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequest {

    @NotBlank(message = "не должно быть пустым")
    @Size(min = 2, max = 250, message = "длина должна быть от 2 до 250 символов")
    private String name;

    @NotBlank(message = "не должно быть пустым")
    @Email(message = "должен иметь корректный формат")
    @Size(min = 6, max = 254, message = "длина должна быть от 6 до 254 символов")
    private String email;
}
