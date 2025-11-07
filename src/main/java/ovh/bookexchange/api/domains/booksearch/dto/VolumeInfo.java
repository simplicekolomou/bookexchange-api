package ovh.bookexchange.api.domains.booksearch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VolumeInfo(
        String title,
        List<String> authors,
        String publisher,
        String publishedDate,
        String description,
        List<IndustryIdentifier> industryIdentifiers,
        ReadingModes readingModes,
        Integer pageCount,
        String printType,
        List<String> categories,
        String maturityRating,
        Boolean allowAnonLogging,
        String contentVersion,
        String language,
        String previewLink,
        String infoLink,
        String canonicalVolumeLink
) {}
