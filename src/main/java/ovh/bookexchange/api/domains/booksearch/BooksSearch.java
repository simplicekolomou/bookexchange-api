package ovh.bookexchange.api.domains.booksearch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ovh.bookexchange.api.domains.booksearch.dto.VolumesResponse;

@Service
public class BooksSearch {
    BookClientInterface bookClientInterface;

    @Autowired
    public BooksSearch(BookClientInterface bookClientInterface) {
        this.bookClientInterface = bookClientInterface;
    }

    public VolumesResponse searchWorks(String title, String author, int startIndex, int maxResults) {
        return bookClientInterface.searchWorks(title, author, startIndex, maxResults);
    }
}
