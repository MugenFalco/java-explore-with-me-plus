package ewm.rating.service;

import ewm.rating.dto.EventRatingCount;
import ewm.rating.entity.RatingType;

import java.util.List;
import java.util.Map;

public interface RatingService {

    void rate(Long userId, Long eventId, RatingType type);

    void delete(Long userId, Long eventId);

    Map<Long, EventRatingCount> getRatingsForEvents(List<Long> eventIds);
}