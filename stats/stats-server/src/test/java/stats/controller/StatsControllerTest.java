package stats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import stats.dto.EndpointHitDto;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestPropertySource(properties = "client.url=http://localhost:0")
class StatsControllerTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void saveHit_returnsCreated() throws Exception {
        EndpointHitDto hit = new EndpointHitDto("ewm-main-service", "/events/1",
                "192.163.0.1", LocalDateTime.now());

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hit)))
                .andExpect(status().isCreated());
    }

    @Test
    void saveHit_missingApp_returnsBadRequest() throws Exception {
        EndpointHitDto hit = new EndpointHitDto(null, "/events/1",
                "192.163.0.1", LocalDateTime.now());

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hit)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStats_returnsAggregatedHits() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        EndpointHitDto hit1 = new EndpointHitDto("ewm-main-service", "/events/5", "10.0.0.1", now);
        EndpointHitDto hit2 = new EndpointHitDto("ewm-main-service", "/events/5", "10.0.0.2", now);

        mockMvc.perform(post("/hit").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hit1)));
        mockMvc.perform(post("/hit").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hit2)));

        String start = now.minusHours(1).format(FORMATTER);
        String end = now.plusHours(1).format(FORMATTER);

        mockMvc.perform(get("/stats")
                        .param("start", start)
                        .param("end", end)
                        .param("uris", "/events/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app", is("ewm-main-service")))
                .andExpect(jsonPath("$[0].uri", is("/events/5")))
                .andExpect(jsonPath("$[0].hits", is(2)));
    }

    @Test
    void getStats_badDateFormat_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("start", "not-a-date")
                        .param("end", "2022-09-06 11:00:23"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStats_missingStart_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("end", "2022-09-06 11:00:23"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStats_startAfterEnd_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("start", "2022-09-06 11:00:23")
                        .param("end", "2022-09-05 11:00:23"))
                .andExpect(status().isBadRequest());
    }
}
