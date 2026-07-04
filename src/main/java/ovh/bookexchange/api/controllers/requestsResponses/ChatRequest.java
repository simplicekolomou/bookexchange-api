package ovh.bookexchange.api.controllers.requestsResponses;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ovh.bookexchange.api.domains.entities.ChatType;

@Getter
@Setter
@RequiredArgsConstructor
public class ChatRequest {
    private Long targetUserId;
    private ChatType chatType;
}
