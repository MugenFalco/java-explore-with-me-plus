package ewm.event.repository;

import ewm.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    boolean existsByCategoryId(Long categoryId);

    Page<Event> findAllByInitiatorId(Long initiatorId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long id, Long initiatorId);

    @Query("SELECT e.id FROM Event e WHERE e.initiator.id = :initiatorId")
    List<Long> findIdsByInitiatorId(@Param("initiatorId") Long initiatorId);

    @Query("""
            SELECT e
            FROM Event e
            LEFT JOIN Rating r ON r.event.id = e.id
            WHERE e.state = ewm.event.entity.EventState.PUBLISHED
                AND (
                     :text IS NULL OR :text = ''
                     OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%'))
                     OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))                        
                )
                AND (:paid IS NULL OR e.paid = :paid)
                AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart) 
                AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)
                AND (:categories IS NULL OR e.category.id IN :categories)     
                AND (
                                :onlyAvailable = false
                                OR e.participantLimit = 0
                                OR (
                                    SELECT COUNT(req)
                                    FROM Request req
                                    WHERE req.event.id = e.id
                                        AND req.status = ewm.request.entity.RequestStatus.CONFIRMED
                                ) < e.participantLimit
                )                  
            GROUP BY e.id
            ORDER BY (
                      SUM(CASE WHEN r.type = ewm.rating.entity.RatingType.LIKE THEN 1L ELSE 0L END) -
                      SUM(CASE WHEN r.type = ewm.rating.entity.RatingType.DISLIKE THEN 1L ELSE 0L END)            
                      ) DESC                                                                             
            """)
    List<Event> findPublicEventsSortedByRating(
            @Param("text") String text,
            @Param("categories") List<Long> categories,
            @Param("paid") Boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("onlyAvailable") boolean onlyAvailable,
            Pageable pageable
            );
}
