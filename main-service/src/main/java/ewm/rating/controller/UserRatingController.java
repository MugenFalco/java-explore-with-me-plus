package ewm.rating.controller;

import ewm.event.service.EventService;
import ewm.rating.dto.AuthorRatingDto;
import ewm.rating.dto.EventRatingCount;
import ewm.rating.mapper.RatingMapper;
import ewm.rating.service.RatingService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserRatingController {

    private final EventService eventService;
    private final RatingService ratingService;

    @GetMapping("/{userId}/rating")
    public AuthorRatingDto getAuthorRating(@PathVariable Long userId) {
        List<Long> eventIds = eventService.getEventIdsByInitiator(userId);
        Map<Long, EventRatingCount> ratings = ratingService.getRatingsForEvents(eventIds);

        long totalRating = ratings.values().stream()
                .mapToLong(r -> r.getLikes() - r.getDislikes())
                .sum();
        long ratedEventsCount = ratings.values().stream()
                .filter(r -> r.getLikes() + r.getDislikes() > 0)
                .count();

        return RatingMapper.toAuthorRatingDto(userId, totalRating, ratedEventsCount);
    }

    @GetMapping("/top-authors")
    public List<AuthorRatingDto> getTopAuthors(@RequestParam(defaultValue = "10") @Positive int size) {
        return ratingService.getTopAuthors(size).stream()
                .map(RatingMapper::toAuthorRatingDto)
                .toList();
    }
}