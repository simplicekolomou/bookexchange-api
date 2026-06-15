package ovh.bookexchange.api.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ovh.bookexchange.api.controllers.representations.BookRep;
import ovh.bookexchange.api.controllers.representations.BookWishRep;
import ovh.bookexchange.api.domains.entities.BookWish;
import ovh.bookexchange.api.domains.entities.EndUser;
import ovh.bookexchange.api.infrastructures.repos.BookWishRepository;
import ovh.bookexchange.api.infrastructures.repos.EndUserRepository;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/book-wish")
public class BookWishController {
    private final BookWishRepository bookWishRepository;
    private final EndUserRepository endUserRepository;
    private final ModelMapper mapper;

    @Autowired
    public BookWishController(BookWishRepository bookWishRepository, EndUserRepository endUserRepository, ModelMapper mapper) {
        this.bookWishRepository = bookWishRepository;
        this.endUserRepository = endUserRepository;
        this.mapper = mapper;
    }

    @GetMapping(value = "/user/me")
    public List<BookWishRep> getBookCopiesInUserCollection(Principal principal, @ParameterObject Pageable pageable) {
        List<BookWish> bookWishCopies = bookWishRepository.findByOwnerEmail(principal.getName(), pageable);
        log.info("Getting my wishes for user {}: \n{}", principal.getName(), bookWishCopies);
        return bookWishCopies.stream().map(bookWish -> mapper.map(bookWish, BookWishRep.class)).toList();
    }

    @GetMapping("/user/{userId}")
    public List<BookWishRep> getBookCopiesByUserId(
            @PathVariable Long userId,
            @ParameterObject Pageable pageable
    ) {
        List<BookWish> bookWishCopies =
                bookWishRepository.findByOwnerId(
                        userId, pageable
                );

        log.info("Copies: {}", bookWishCopies);
        return bookWishCopies.stream()
                .map(bookCopy -> mapper.map(bookCopy, BookWishRep.class))
                .toList();
    }

    @GetMapping("/{copyId}")
    public BookWishRep getBookCopyByUserIdAndCopyId(
            @PathVariable Long copyId
    ) {
        log.info("Getting copy {}", copyId);
        BookWish copy =
            bookWishRepository.findById(copyId)
                .orElseThrow(() -> {
                    log.error("BookWish id not found {}", copyId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND);
                });
        return mapper.map(copy, BookWishRep.class);
    }

    @PostMapping(value = "/user/me")
    public void addBookCopy(@RequestBody @Valid BookWishRep bookRep, Principal principal) {
        BookWish bookWish = mapper.map(bookRep, BookWish.class);
        EndUser owner = endUserRepository.findByEmail(principal.getName())
                .orElseThrow(() -> {
                    log.error("EndUser id not found {}", principal.getName());
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found");
                });
        bookWish.setOwner(owner);
        bookWishRepository.save(bookWish);
        log.info("Adding book wish {}", bookWish);
    }

    @PutMapping("/user/me")
    public void updateBookCopy(@RequestBody @Valid BookRep bookRep, Principal principal) {
        EndUser owner = endUserRepository.findByEmail(principal.getName())
                .orElseThrow(() -> {
                    log.error("User not found for principal: {}", principal.getName());
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found");
                });

        BookWish wishFromDb = bookWishRepository.findByIdAndOwnerId(bookRep.getId(), owner.getId())
                .orElseThrow(() -> {
                    log.error("Book wish not found: id={}, owner={}", bookRep.getId(), owner.getId());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Pre-existing book copy not found");
                });

        mapper.map(bookRep, wishFromDb);     // update the managed entity

        // Force-update the authors collection because it's a list and the changement tracking is weird.
        wishFromDb.getAuthors().clear();
        wishFromDb.getAuthors().addAll(bookRep.getAuthors());

        // make sure owner is not overridden by DTO
        wishFromDb.setOwner(owner);

        bookWishRepository.save(wishFromDb);
        log.info("Updated book wish {}", wishFromDb);
    }

}
