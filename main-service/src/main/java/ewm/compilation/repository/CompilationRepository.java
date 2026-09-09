package ewm.compilation.repository;

import ewm.compilation.entity.Compilation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @Query("SELECT c FROM Compilation c " +
            "WHERE (:pinned IS NULL OR c.pinned = :pinned)")
    List<Compilation> findAll(@Param("pinned") Boolean pinned, Pageable pageable);
}
