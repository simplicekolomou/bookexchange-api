package ovh.bookexchange.api.domains.booksearch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchInfo(
        String textSnippet
) {}
