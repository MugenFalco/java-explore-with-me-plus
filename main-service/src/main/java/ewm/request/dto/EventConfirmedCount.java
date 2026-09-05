package ewm.request.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventConfirmedCount {
    private final Long eventId;
    private final Long confirmedRequests;
}