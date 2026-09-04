package ewm.event.entity;

import ewm.event.dto.AdminEventSearchParams;
import ewm.event.dto.PublicEventSearchParams;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public final class EventSpecification {

    private EventSpecification() {
    }

    public static Specification<Event> byAdminFilters(AdminEventSearchParams searchParams) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (searchParams.getUsers() != null && !searchParams.getUsers().isEmpty()) {
                predicates.add(root.get("initiator").get("id").in(searchParams.getUsers()));
            }
            if (searchParams.getStates() != null && !searchParams.getStates().isEmpty()) {
                predicates.add(root.get("state").in(searchParams.getStates()));
            }
            if (searchParams.getCategories() != null && !searchParams.getCategories().isEmpty()) {
                predicates.add(root.get("category").get("id").in(searchParams.getCategories()));
            }
            if (searchParams.getRangeStart() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("eventDate"), searchParams.getRangeStart()));
            }
            if (searchParams.getRangeEnd() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("eventDate"), searchParams.getRangeEnd()));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Event> byPublicFilters(PublicEventSearchParams searchParams) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("state"), EventState.PUBLISHED));
            if (searchParams.getText() != null && !searchParams.getText().isBlank()) {
                String pattern = "%" + searchParams.getText().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("annotation")), pattern),
                        builder.like(builder.lower(root.get("description")), pattern)
                ));
            }
            if (searchParams.getCategories() != null && !searchParams.getCategories().isEmpty()) {
                predicates.add(root.get("category").get("id").in(searchParams.getCategories()));
            }
            if (searchParams.getPaid() != null) {
                predicates.add(builder.equal(root.get("paid"), searchParams.getPaid()));
            }
            if (searchParams.getRangeStart() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("eventDate"), searchParams.getRangeStart()));
            }
            if (searchParams.getRangeEnd() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("eventDate"), searchParams.getRangeEnd()));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
