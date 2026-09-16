package ewm.rating.mapper;

import ewm.rating.dto.AuthorRatingCount;
import ewm.rating.dto.AuthorRatingDto;

public final class RatingMapper {

    private RatingMapper() {
    }

    public static AuthorRatingDto toAuthorRatingDto(Long userId, double rating, long ratedEventsCount) {
        return new AuthorRatingDto(userId, rating, ratedEventsCount);
    }

    public static AuthorRatingDto toAuthorRatingDto(AuthorRatingCount count) {
        return new AuthorRatingDto(count.getUserId(), count.average(), count.getEventsCount());
    }
}