package ovh.bookexchange.api.infrastructures;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import ovh.bookexchange.api.domains.entities.BookCopy;

import java.util.List;

public interface BookCopyRepository  extends ListPagingAndSortingRepository<BookCopy, Long> {
    List<BookCopy> findByOwnerEmail(String email, Pageable pageable);
}
