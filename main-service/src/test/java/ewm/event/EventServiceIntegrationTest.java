package ewm.event;

import ewm.category.Category;
import ewm.category.CategoryRepository;
import ewm.event.dto.EventAdminStateAction;
import ewm.event.dto.EventFullDto;
import ewm.event.dto.EventShortDto;
import ewm.event.dto.LocationDto;
import ewm.event.dto.NewEventDto;
import ewm.event.dto.PublicEventSort;
import ewm.event.dto.UpdateEventAdminRequest;
import ewm.exception.ValidationException;
import ewm.user.User;
import ewm.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class EventServiceIntegrationTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Long userId;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setName("Инициатор");
        user.setEmail("initiator@example.com");
        userId = userRepository.save(user).getId();

        Category category = new Category();
        category.setName("Концерты");
        categoryId = categoryRepository.save(category).getId();
    }

    @Test
    void shouldCreatePendingEventAndPublishIt() {
        EventFullDto created = eventService.create(userId, newEvent(LocalDateTime.now().plusHours(3)));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getState()).isEqualTo(EventState.PENDING);
        assertThat(created.getCreatedOn()).isNotNull();

        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setStateAction(EventAdminStateAction.PUBLISH_EVENT);
        EventFullDto published = eventService.updateByAdmin(created.getId(), request);

        assertThat(published.getState()).isEqualTo(EventState.PUBLISHED);
        assertThat(published.getPublishedOn()).isNotNull();
        assertThat(eventService.getPublicEvent(created.getId()).getId()).isEqualTo(created.getId());
    }

    @Test
    void shouldRejectEventCreationTooCloseToCurrentTime() {
        assertThatThrownBy(() -> eventService.create(userId, newEvent(LocalDateTime.now().plusMinutes(30))))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldSearchPublicEventsAndRespectOffset() {
        EventFullDto first = eventService.create(userId, newEvent(LocalDateTime.now().plusHours(3)));
        NewEventDto secondEvent = newEvent(LocalDateTime.now().plusHours(4));
        secondEvent.setTitle("Второй будущий концерт");
        EventFullDto second = eventService.create(userId, secondEvent);

        publish(first.getId());
        publish(second.getId());

        List<EventShortDto> events = eventService.getPublicEvents(
                "концерт", List.of(categoryId), false, null, null, false,
                PublicEventSort.EVENT_DATE, 1, 1
        );

        assertThat(events).singleElement().extracting(EventShortDto::getId).isEqualTo(second.getId());
    }

    private void publish(Long eventId) {
        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setStateAction(EventAdminStateAction.PUBLISH_EVENT);
        eventService.updateByAdmin(eventId, request);
    }

    private NewEventDto newEvent(LocalDateTime eventDate) {
        return new NewEventDto(
                "Подробная аннотация будущего концерта",
                categoryId,
                "Подробное описание будущего концерта для всех посетителей",
                eventDate,
                new LocationDto(55.75F, 37.61F),
                false,
                10,
                true,
                "Будущий концерт"
        );
    }
}
