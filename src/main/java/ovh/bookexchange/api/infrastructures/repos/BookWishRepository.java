package ovh.bookexchange.api.infrastructures.repos;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import ovh.bookexchange.api.domains.entities.BookWish;

import java.util.List;
import java.util.Optional;

public interface BookWishRepository extends ListPagingAndSortingRepository<BookWish, Long>, CrudRepository<BookWish, Long> {

    List<BookWish> findByOwnerEmail(String email, Pageable pageable);

    List<BookWish> findByOwnerId(Long userId, Pageable pageable);

    Optional<BookWish> findByIdAndOwnerId(long id, long id1);
}
