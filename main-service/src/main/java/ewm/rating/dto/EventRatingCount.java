package ewm.rating.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventRatingCount {
    private Long eventId;
    private Long likes;
    private Long dislikes;
}