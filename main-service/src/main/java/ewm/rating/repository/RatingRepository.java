package ewm.rating.repository;

import ewm.rating.dto.AuthorRatingCount;
import ewm.rating.dto.EventRatingCount;
import ewm.rating.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
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

    @Query("SELECT new ewm.rating.dto.EventRatingCount(r.event.id, "
            + "SUM(CASE WHEN r.type = ewm.rating.entity.RatingType.LIKE THEN 1L ELSE 0L END), "
            + "SUM(CASE WHEN r.type = ewm.rating.entity.RatingType.DISLIKE THEN 1L ELSE 0L END)) "
            + "FROM Rating r "
            + "GROUP BY r.event.id "
            + "ORDER BY (SUM(CASE WHEN r.type = ewm.rating.entity.RatingType.LIKE THEN 1L ELSE 0L END) "
            + "- SUM(CASE WHEN r.type = ewm.rating.entity.RatingType.DISLIKE THEN 1L ELSE 0L END)) DESC")
    List<EventRatingCount> findTopEvents(Pageable pageable);

    @Query("SELECT new ewm.rating.dto.AuthorRatingCount(e.initiator.id, "
            + "SUM(CASE WHEN r.type = ewm.rating.entity.RatingType.LIKE THEN 1L ELSE 0L END), "
            + "SUM(CASE WHEN r.type = ewm.rating.entity.RatingType.DISLIKE THEN 1L ELSE 0L END), "
            + "COUNT(DISTINCT e.id)) "
            + "FROM Event e LEFT JOIN Rating r ON r.event = e "
            + "WHERE e.state = ewm.event.entity.EventState.PUBLISHED "
            + "AND e.initiator.id = :userId "
            + "GROUP BY e.initiator.id")
    Optional<AuthorRatingCount> findAuthorRatingCount(@Param("userId") Long userId);

    @Query("SELECT new ewm.rating.dto.AuthorRatingCount(e.initiator.id, "
            + "SUM(CASE WHEN r.type = ewm.rating.entity.RatingType.LIKE THEN 1L ELSE 0L END), "
            + "SUM(CASE WHEN r.type = ewm.rating.entity.RatingType.DISLIKE THEN 1L ELSE 0L END), "
            + "COUNT(DISTINCT e.id)) "
            + "FROM Event e LEFT JOIN Rating r ON r.event = e "
            + "WHERE e.state = ewm.event.entity.EventState.PUBLISHED "
            + "GROUP BY e.initiator.id "
            + "ORDER BY (SUM(CASE WHEN r.type = ewm.rating.entity.RatingType.LIKE THEN 1L ELSE 0L END) "
            + "- SUM(CASE WHEN r.type = ewm.rating.entity.RatingType.DISLIKE THEN 1L ELSE 0L END)) * 1.0 "
            + "/ COUNT(DISTINCT e.id) DESC")
    List<AuthorRatingCount> findTopAuthors(Pageable pageable);
}