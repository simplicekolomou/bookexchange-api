package ovh.bookexchange.api.controllers.representations.messages;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
public class MessageRep {
    private long id;

    @NotBlank
    private String content;

    private Timestamp sendTime;

    private long groupChatId;

    private long senderId;
    private List<Long> read;
}
