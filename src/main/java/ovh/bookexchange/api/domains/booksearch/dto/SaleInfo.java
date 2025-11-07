package ovh.bookexchange.api.domains.booksearch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SaleInfo(
        String country,
        String saleability,
        @JsonProperty("isEbook") boolean isEbook
) {}
