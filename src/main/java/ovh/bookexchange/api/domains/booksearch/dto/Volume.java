package ovh.bookexchange.api.domains.booksearch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Volume(
        String kind,
        String id,
        String etag,
        String selfLink,
        VolumeInfo volumeInfo,
        SaleInfo saleInfo,
        AccessInfo accessInfo,
        SearchInfo searchInfo
) {}
