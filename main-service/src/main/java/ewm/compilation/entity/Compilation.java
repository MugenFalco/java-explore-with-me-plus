package ewm.compilation.entity;

import ewm.event.entity.Event;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "compilations")
@Getter
@Setter
@NoArgsConstructor
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @JoinTable(
            name = "compilation_events",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private Set<Event> events;

    @Column(nullable = false)
    private Boolean pinned;

    @Column(nullable = false, unique = true, length = 50)
    private String title;
}