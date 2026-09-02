package ewm.event;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class EventSpecification {

    private EventSpecification() {
    }

    public static Specification<Event> byAdminFilters(List<Long> userIds, List<EventState> states,
                                                        List<Long> categoryIds, LocalDateTime rangeStart,
                                                        LocalDateTime rangeEnd) {
        return (root, query, builder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (userIds != null && !userIds.isEmpty()) {
                predicates.add(root.get("initiator").get("id").in(userIds));
            }
            if (states != null && !states.isEmpty()) {
                predicates.add(root.get("state").in(states));
            }
            if (categoryIds != null && !categoryIds.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categoryIds));
            }
            if (rangeStart != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
            }
            if (rangeEnd != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }
            return builder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    public static Specification<Event> byPublicFilters(String text, List<Long> categoryIds, Boolean paid,
                                                        LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        return (root, query, builder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("state"), EventState.PUBLISHED));
            if (text != null && !text.isBlank()) {
                String pattern = "%" + text.toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("annotation")), pattern),
                        builder.like(builder.lower(root.get("description")), pattern)
                ));
            }
            if (categoryIds != null && !categoryIds.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categoryIds));
            }
            if (paid != null) {
                predicates.add(builder.equal(root.get("paid"), paid));
            }
            if (rangeStart != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
            }
            if (rangeEnd != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }
            return builder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
