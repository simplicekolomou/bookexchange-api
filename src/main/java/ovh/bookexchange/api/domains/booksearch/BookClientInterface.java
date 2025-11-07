package ovh.bookexchange.api.domains.booksearch;

import ovh.bookexchange.api.domains.booksearch.dto.VolumesResponse;

public interface BookClientInterface {
    VolumesResponse searchWorks(String title, String author, int startIndex, int maxResults);
}
