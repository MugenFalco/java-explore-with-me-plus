package ewm.event.repository;

import ewm.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    boolean existsByCategoryId(Long categoryId);

    Page<Event> findAllByInitiatorId(Long initiatorId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long id, Long initiatorId);

    @Query("SELECT e.id FROM Event e WHERE e.initiator.id = :initiatorId")
    List<Long> findIdsByInitiatorId(@Param("initiatorId") Long initiatorId);
}
