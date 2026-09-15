package ewm.rating.controller;

import ewm.rating.dto.AuthorRatingDto;
import ewm.rating.service.RatingService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserRatingController {

    private final RatingService ratingService;

    @GetMapping("/{userId}/rating")
    public AuthorRatingDto getAuthorRating(@PathVariable Long userId) {
        return ratingService.getAuthorRating(userId);
    }

    @GetMapping("/top-authors")
    public List<AuthorRatingDto> getTopAuthors(@RequestParam(defaultValue = "0") @Min(0) int from,
                                               @RequestParam(defaultValue = "10") @Positive int size) {
        return ratingService.getTopAuthors(from, size);
    }
}