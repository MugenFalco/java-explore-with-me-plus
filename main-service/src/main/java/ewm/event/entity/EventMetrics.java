package ewm.event.entity;

public record EventMetrics(long confirmedRequests, long views) {

    public static final EventMetrics EMPTY = new EventMetrics(0, 0);
}
