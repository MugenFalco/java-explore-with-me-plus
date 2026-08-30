package stats;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import stats.client.StatsClient;
import stats.dto.EndpointHitDto;
import stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = "client.url=http://localhost:0")
class StatsClientIntegrationTest {

    @Autowired
    private Environment environment;

    @Test
    void clientShouldSendHitAndRetrieveStats() {
        int port = environment.getProperty("local.server.port", Integer.class);
        StatsClient client = new StatsClient("http://localhost:" + port);

        LocalDateTime now = LocalDateTime.now();
        client.hit(new EndpointHitDto("ewm-main-service", "/events/42", "127.0.0.1", now));

        List<ViewStatsDto> stats = client.getStats(now.minusMinutes(1), now.plusMinutes(1),
                List.of("/events/42"), false);

        assertThat(stats).hasSize(1);
        assertThat(stats.getFirst().getHits()).isEqualTo(1);
    }
}