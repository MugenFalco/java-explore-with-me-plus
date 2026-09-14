package ewm.rating.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthorRatingCount {
    private Long userId;
    private Long likes;
    private Long dislikes;
    private Long eventsCount;

    public double average() {
        return eventsCount == 0 ? 0.0 : (double) (likes - dislikes) / eventsCount;
    }
}