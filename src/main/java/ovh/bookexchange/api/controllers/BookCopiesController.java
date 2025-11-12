package ovh.bookexchange.api.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ovh.bookexchange.api.controllers.representations.BookRep;
import ovh.bookexchange.api.domains.entities.BookCopy;
import ovh.bookexchange.api.infrastructures.BookCopyRepository;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/book-copies")
public class BookCopiesController {
    public BookCopiesController(BookCopyRepository bookCopyRepository) {
        this.bookCopyRepository = bookCopyRepository;
    }
    private final BookCopyRepository bookCopyRepository;
    @GetMapping(value = "/user/me")
    public List<BookRep> getBookCopiesInUserCollection(Principal principal, Pageable pageable) {
        if (pageable.getPageSize() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page size must be less than 100");
            //TODO il doit y avoir un meilleur moyen (@PageableDefault existe pourqoi pas @PageableLimits ?)
        }
        ModelMapper mapper = new ModelMapper();
        List<BookCopy> bookCopies = bookCopyRepository.findByOwnerEmail(principal.getName(), pageable);
        return bookCopies.stream().map(bookCopy -> mapper.map(bookCopy, BookRep.class)).toList();
    }
}
