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

    @GetMapping("/user/{userId}")
    public List<BookRep> getBookCopiesByUserId(
            @PathVariable Long userId,
            @ParameterObject Pageable pageable
    ) {
        List<BookCopy> bookCopies =
                bookCopyRepository.findByOwnerId(
                        userId, pageable
                );

        log.info("Getting available copies of userId {}", userId);
        log.info("Copies: {}", bookCopies);
        return bookCopies.stream()
                .map(bookCopy -> mapper.map(bookCopy, BookRep.class))
                .toList();
    }

    @GetMapping("/{copyId}")
    public BookRep getBookCopyByUserIdAndCopyId(
            @PathVariable Long copyId
    ) {
        log.info("Getting copy {}", copyId);
        BookCopy copy =
                bookCopyRepository.findById(copyId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return mapper.map(copy, BookRep.class);
    }

    @PostMapping(value = "/user/me")
    public void addBookCopy(@RequestBody @Valid BookRep bookRep, Principal principal) {
        BookCopy bookCopy = mapper.map(bookRep, BookCopy.class);
        EndUser owner = endUserRepository.findByEmail(principal.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found"));
        bookCopy.setOwner(owner);
        bookCopy.setWarehouseItems(List.of());
        bookCopyRepository.save(bookCopy);
    }

    @PutMapping("/user/me")
    public void updateBookCopy(@RequestBody @Valid BookRep bookRep, Principal principal) {
//        System.out.println("Incoming request: " + bookRep);

        EndUser owner = endUserRepository.findByEmail(principal.getName())
                .orElseThrow(() -> {
//                    System.out.println("User not found for principal: " + principal.getName());
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found");
                });

        BookCopy copyFromDb = bookCopyRepository.findByIdAndOwnerId(bookRep.getId(), owner.getId())
                .orElseThrow(() -> {
//                    System.out.println("Book copy not found: id=" + bookRep.getId() + ", owner=" + owner.getId());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Pre-existing book copy not found");
                });

//        System.out.println("Before mapping: " + copyFromDb);
        mapper.map(bookRep, copyFromDb);     // update the managed entity
//        System.out.println("After mapping:  " + copyFromDb);

        // Force-update the authors collection because it's a list and the changement tracking is weird.
        copyFromDb.getAuthors().clear();
        copyFromDb.getAuthors().addAll(bookRep.getAuthors());
//        System.out.println("After authors fix: " + copyFromDb);

        // make sure owner is not overridden by DTO
        copyFromDb.setOwner(owner);

        bookCopyRepository.save(copyFromDb);

//        System.out.println("Book copy updated successfully");
    }


}
