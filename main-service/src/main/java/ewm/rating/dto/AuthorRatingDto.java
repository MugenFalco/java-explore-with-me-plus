package ewm.rating.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorRatingDto {
    private Long userId;
    private Double rating;
    private Long ratedEventsCount;
}