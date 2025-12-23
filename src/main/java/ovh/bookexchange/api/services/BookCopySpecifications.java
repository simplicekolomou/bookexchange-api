package ovh.bookexchange.api.services;

import org.springframework.data.jpa.domain.Specification;
import ovh.bookexchange.api.domains.entities.AvailabilityType;
import ovh.bookexchange.api.domains.entities.BookCopy;
import ovh.bookexchange.api.domains.entities.PhysicalState;

public class BookCopySpecifications {

    public static Specification<BookCopy> isbnContains(String isbn) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("isbn")), "%" + isbn.toLowerCase() + "%");
    }

    public static Specification<BookCopy> titleContains(String title) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<BookCopy> authorContains(String author) {
        return (root, query, cb) ->
                cb.isMember(author.toLowerCase(), root.get("authors"));
    }

    public static Specification<BookCopy> isAvailable() {
        return (root, query, cb) ->
                cb.notEqual(root.get("availabilityType"), AvailabilityType.NONE);
    }

    public static Specification<BookCopy> hasBookState(PhysicalState bookState) {
        return (root, query, cb) ->
                cb.equal(root.get("physicalState"), bookState);
    }
}

