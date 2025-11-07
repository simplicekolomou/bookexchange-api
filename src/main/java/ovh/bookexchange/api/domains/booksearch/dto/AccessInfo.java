
package ovh.bookexchange.api.domains.booksearch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AccessInfo(
        String country,
        String viewability,
        boolean embeddable,
        boolean publicDomain,
        String textToSpeechPermission,
        Epub epub,
        Pdf pdf,
        String webReaderLink,
        String accessViewStatus,
        boolean quoteSharingAllowed
) {}
