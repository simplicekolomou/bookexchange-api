package ovh.bookexchange.api.domains.booksearch.dto;

import java.util.List;

public record VolumeShort (
        String id,
        String title,
        String publishedDate,
        String coverUrl,
        List<IndustryIdentifier> isbns,
        List<String> authors,
        String description
){}
