package ovh.bookexchange.api.domains.booksearch;

import ovh.bookexchange.api.domains.booksearch.dto.VolumeShort;

import java.util.List;

public interface BookSearchInterface {
    public List<VolumeShort> searchWorks(String title, String author, int startIndex, int maxResults);
}
