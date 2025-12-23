package ovh.bookexchange.api.infrastructures.booksearch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ovh.bookexchange.api.domains.booksearch.BookSearchInterface;
import ovh.bookexchange.api.domains.booksearch.dto.Volume;
import ovh.bookexchange.api.domains.booksearch.dto.VolumeShort;
import ovh.bookexchange.api.domains.booksearch.dto.VolumesResponse;

import java.util.ArrayList;
import java.util.List;

@Service
public class BooksSearch implements BookSearchInterface {
    public static final String COVER_ENDPOINT = "https://play.google.com/books/content/images/frontcover/";
    BookClientInterface bookClientInterface;

    @Autowired
    public BooksSearch(BookClientInterface bookClientInterface) {
        this.bookClientInterface = bookClientInterface;
    }

    public List<VolumeShort> searchWorks(String title, String author, int startIndex, int maxResults) {
        VolumesResponse volumesResponse = bookClientInterface.searchWorks(title, author, startIndex, maxResults);
        List<VolumeShort> volumes = new ArrayList<>();
        if (volumesResponse == null || volumesResponse.items() == null || volumesResponse.items().isEmpty()) {
            return volumes;
        }
        for (Volume item : volumesResponse.items()) {
            VolumeShort volumeShort = new VolumeShort(
                    item.id(),
                    item.volumeInfo().title(),
                    item.volumeInfo().publishedDate(),
                    COVER_ENDPOINT + item.id(),
                    item.volumeInfo().industryIdentifiers(),
                    item.volumeInfo().authors(),
                    item.volumeInfo().description()
            );
            volumes.add(volumeShort);
        }
        return volumes;
    }
}
