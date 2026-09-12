package ewm.rating.controller;

import ewm.rating.entity.RatingType;
import ewm.rating.service.RatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/events/{eventId}")
@RequiredArgsConstructor
public class RatingPrivateController {

    private final RatingService ratingService;

    @PutMapping("/like")
    @ResponseStatus(HttpStatus.OK)
    public void like(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Пользователь {} ставит лайк событию {}", userId, eventId);
        ratingService.rate(userId, eventId, RatingType.LIKE);
    }

    @PutMapping("/dislike")
    @ResponseStatus(HttpStatus.OK)
    public void dislike(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Пользователь {} ставит дизлайк событию {}", userId, eventId);
        ratingService.rate(userId, eventId, RatingType.DISLIKE);
    }

    @DeleteMapping("/rating")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Пользователь {} удаляет оценку события {}", userId, eventId);
        ratingService.delete(userId, eventId);
    }
}