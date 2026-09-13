package ewm.rating.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthorRatingCount {
    private Long userId;
    private Long likes;
    private Long dislikes;
}