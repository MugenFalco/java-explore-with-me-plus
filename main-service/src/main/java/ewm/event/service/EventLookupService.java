package ewm.event.service;

import ewm.event.entity.Event;

public interface EventLookupService {

    Event getEntityById(Long eventId);

    Event getOwnedEventById(Long userId, Long eventId);
}