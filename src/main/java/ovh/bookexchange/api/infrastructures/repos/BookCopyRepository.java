package ovh.bookexchange.api.infrastructures.repos;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import ovh.bookexchange.api.domains.entities.BookCopy;

import java.util.List;
import java.util.Optional;

public interface BookCopyRepository  extends ListPagingAndSortingRepository<BookCopy, Long>, JpaSpecificationExecutor<BookCopy>, CrudRepository<BookCopy, Long> {
    List<BookCopy> findByOwnerEmail(String email, Pageable pageable);

    Optional<BookCopy> findByIdAndOwnerId(long id, long ownerId);

    List<BookCopy> findByOwnerId(Long userId, Pageable pageable);
}
