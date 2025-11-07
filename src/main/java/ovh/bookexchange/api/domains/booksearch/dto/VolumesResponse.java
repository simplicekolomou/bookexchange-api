package ovh.bookexchange.api.domains.booksearch.dto;

/* ===== JSON mapping for Google Books /volumes ===== */

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VolumesResponse(
        String kind,
        int totalItems,
        List<Volume> items
) {}

