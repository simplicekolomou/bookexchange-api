package ovh.bookexchange.api.infrastructures.booksearch;

import ovh.bookexchange.api.domains.booksearch.dto.VolumesResponse;

public interface BookClientInterface {
    VolumesResponse searchWorks(String title, String author, int startIndex, int maxResults);
}
