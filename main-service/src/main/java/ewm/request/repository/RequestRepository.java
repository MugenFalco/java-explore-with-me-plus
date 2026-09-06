package ewm.request.repository;

import ewm.request.dto.EventConfirmedCount;
import ewm.request.entity.Request;
import ewm.request.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findAllByRequesterId(Long requesterId);

    List<Request> findAllByEventId(Long eventId);

    List<Request> findAllByEventIdAndStatus(Long eventId, RequestStatus status);

    Optional<Request> findByIdAndRequesterId(Long id, Long requesterId);

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    @Query("SELECT new ewm.request.dto.EventConfirmedCount(r.event.id, COUNT(r)) "
            + "FROM Request r "
            + "WHERE r.status = ewm.request.entity.RequestStatus.CONFIRMED "
            + "AND r.event.id IN :eventIds "
            + "GROUP BY r.event.id")
    List<EventConfirmedCount> countConfirmedByEventIds(@Param("eventIds") List<Long> eventIds);
}