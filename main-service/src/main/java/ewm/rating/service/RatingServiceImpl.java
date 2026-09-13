package ewm.rating.service;

import ewm.event.entity.Event;
import ewm.event.entity.EventState;
import ewm.event.service.EventLookupService;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import ewm.rating.dto.EventRatingCount;
import ewm.rating.entity.Rating;
import ewm.rating.entity.RatingType;
import ewm.rating.repository.RatingRepository;
import ewm.request.service.RequestService;
import ewm.user.User;
import ewm.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final EventLookupService eventLookupService;
    private final RequestService requestService;
    private final UserService userService;

    @Override
    @Transactional
    public void rate(Long userId, Long eventId, RatingType type) {
        User user = userService.getEntityById(userId);
        Event event = eventLookupService.getEntityById(eventId);

        validateCanRate(event, userId);

        ratingRepository.findByEventIdAndUserId(eventId, userId)
                .ifPresentOrElse(
                        rating -> rating.setType(type),
                        () -> {
                            Rating rating = new Rating();
                            rating.setEvent(event);
                            rating.setUser(user);
                            rating.setType(type);
                            rating.setCreated(LocalDateTime.now());
                            ratingRepository.save(rating);
                        });

        log.info("Пользователь {} оценил событие {} как {}", userId, eventId, type);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long eventId) {
        Rating rating = ratingRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Оценка пользователя " + userId + " для события " + eventId + " не найдена."));

        ratingRepository.delete(rating);
        log.info("Пользователь {} удалил оценку события {}", userId, eventId);
    }

    @Override
    public Map<Long, EventRatingCount> getRatingsForEvents(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }
        return ratingRepository.countByEventIds(eventIds).stream()
                .collect(Collectors.toMap(EventRatingCount::getEventId, Function.identity()));
    }

    private void validateCanRate(Event event, Long userId) {
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Нельзя оценивать собственное событие.");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Оценить можно только опубликованное событие.");
        }
        if (event.getEventDate().isAfter(LocalDateTime.now())) {
            throw new ConflictException("Оценить событие можно только после его окончания.");
        }
        if (!requestService.hasConfirmedRequest(event.getId(), userId)) {
            throw new ConflictException("Оценить событие может только участник с подтверждённой заявкой.");
        }
    }
}