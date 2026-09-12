package ewm.rating.repository;

import ewm.rating.dto.EventRatingCount;
import ewm.rating.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByEventIdAndUserId(Long eventId, Long userId);

    @Query("SELECT new ewm.rating.dto.EventRatingCount("
            + "r.event.id, "
            + "SUM(CASE WHEN r.type = ewm.rating.entity.RatingType.LIKE THEN 1L ELSE 0L END), "
            + "SUM(CASE WHEN r.type = ewm.rating.entity.RatingType.DISLIKE THEN 1L ELSE 0L END)) "
            + "FROM Rating r "
            + "WHERE r.event.id IN :eventIds "
            + "GROUP BY r.event.id")
    List<EventRatingCount> countByEventIds(@Param("eventIds") List<Long> eventIds);
}