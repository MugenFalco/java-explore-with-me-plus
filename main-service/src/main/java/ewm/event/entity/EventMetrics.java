package ewm.event.entity;

public record EventMetrics(long confirmedRequests,
                           long views,
                           long likes,
                           long dislikes,
                           long rating) {



    public static final EventMetrics EMPTY = new EventMetrics(0, 0, 0 , 0, 0);
}
