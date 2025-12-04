package ovh.bookexchange.api.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ovh.bookexchange.api.controllers.representations.BookRep;
import ovh.bookexchange.api.domains.entities.AvailabilityType;
import ovh.bookexchange.api.domains.entities.BookCopy;
import ovh.bookexchange.api.domains.entities.EndUser;
import ovh.bookexchange.api.infrastructures.repos.BookCopyRepository;
import ovh.bookexchange.api.infrastructures.repos.EndUserRepository;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/book-copies")
public class BookCopiesController {
    private final BookCopyRepository bookCopyRepository;
    private final EndUserRepository endUserRepository;
    private final ModelMapper mapper;
    public BookCopiesController(BookCopyRepository bookCopyRepository, EndUserRepository endUserRepository, ModelMapper mapper) {
        this.bookCopyRepository = bookCopyRepository;
        this.endUserRepository = endUserRepository;
        this.mapper = mapper;
    }
    @GetMapping(value = "/user/me")
    public List<BookRep> getBookCopiesInUserCollection(Principal principal, @ParameterObject Pageable pageable) {
        List<BookCopy> bookCopies = bookCopyRepository.findByOwnerEmail(principal.getName(), pageable);
        log.info("Getting my copies for user {}: \n{}", principal.getName(), bookCopies);
        return bookCopies.stream().map(bookCopy -> mapper.map(bookCopy, BookRep.class)).toList();
    }

    @GetMapping(value = "/user/me/copy/{copyId}")
    public List<BookRep> getBookCopiesInUserCollection(
            Principal principal,
            @PathVariable long copyId,
            @ParameterObject Pageable pageable
    ) {
        List<BookCopy> bookCopies = bookCopyRepository.findByOwnerEmailAndIdIs(principal.getName(), copyId, pageable);
        log.info("Getting my copy {} for user {}", copyId, principal.getName());
        return bookCopies.stream().map(bookCopy -> mapper.map(bookCopy, BookRep.class)).toList();
    }

    @GetMapping("/user/{userId}")
    public List<BookRep> getBookCopiesByUserId(
            @PathVariable Long userId,
            @ParameterObject Pageable pageable
    ) {
        List<BookCopy> bookCopies =
                bookCopyRepository.findByOwnerIdAndAvailabilityTypeNot(
                        userId, AvailabilityType.NONE, pageable
                );

        log.info("Getting available copies of userId {}", userId);
        log.info("Copies: {}", bookCopies);
        return bookCopies.stream()
                .map(bookCopy -> mapper.map(bookCopy, BookRep.class))
                .toList();
    }


    @GetMapping("/user/{userId}/copy/{copyId}")
    public List<BookRep> getBookCopyByUserIdAndCopyId(
            @PathVariable Long userId,
            @PathVariable Long copyId,
            @ParameterObject Pageable pageable
    ) {
        List<BookCopy> bookCopies =
                bookCopyRepository.findByOwnerIdAndIdAndAvailabilityTypeNot(
                        userId, copyId, AvailabilityType.NONE, pageable
                );

        log.info("Getting copy {} of userId {}", copyId, userId);
        log.info("Copies {}", bookCopies);
        return bookCopies.stream()
                .map(bookCopy -> mapper.map(bookCopy, BookRep.class))
                .toList();
    }

    @PostMapping(value = "/user/me")
    public void addBookCopy(@RequestBody @Valid BookRep bookRep, Principal principal) {
        BookCopy bookCopy = mapper.map(bookRep, BookCopy.class);
        EndUser owner = endUserRepository.findByEmail(principal.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found"));
        bookCopy.setOwner(owner);
        bookCopy.setWarehouseItems(List.of());
        bookCopyRepository.save(bookCopy);
    }
}
