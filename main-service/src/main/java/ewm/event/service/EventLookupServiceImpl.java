package ewm.event.service;

import ewm.event.entity.Event;
import ewm.event.repository.EventRepository;
import ewm.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventLookupServiceImpl implements EventLookupService {

    private final EventRepository eventRepository;

    @Override
    public Event getEntityById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с идентификатором " + eventId + " не найдено."));
    }

    @Override
    public Event getOwnedEventById(Long userId, Long eventId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с идентификатором " + eventId + " не найдено."));
    }
}