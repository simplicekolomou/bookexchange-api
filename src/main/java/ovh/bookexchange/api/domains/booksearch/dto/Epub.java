package ovh.bookexchange.api.domains.booksearch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Epub(
        @JsonProperty("isAvailable") boolean isAvailable
) {}
