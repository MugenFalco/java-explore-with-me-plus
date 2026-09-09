package ewm.request.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {

    @NotEmpty(message = "requestIds не должны быть пусты")
    private List<Long> requestIds;

    @NotNull(message = "status не может быть null")
    private RequestStatusUpdateAction status;
}