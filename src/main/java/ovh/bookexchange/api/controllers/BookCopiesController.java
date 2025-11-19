package ovh.bookexchange.api.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ovh.bookexchange.api.controllers.representations.BookRep;
import ovh.bookexchange.api.domains.entities.BookCopy;
import ovh.bookexchange.api.domains.entities.EndUser;
import ovh.bookexchange.api.infrastructures.BookCopyRepository;
import ovh.bookexchange.api.infrastructures.EndUserRepository;

import java.security.Principal;
import java.util.List;

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
        return bookCopies.stream().map(bookCopy -> mapper.map(bookCopy, BookRep.class)).toList();
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
