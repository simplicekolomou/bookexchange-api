package ovh.bookexchange.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ovh.bookexchange.api.domains.booksearch.BooksSearch;
import ovh.bookexchange.api.domains.booksearch.dto.VolumesResponse;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {


    private final BooksSearch booksSearch;

    @Autowired
    public BookController(BooksSearch booksSearch) {
        this.booksSearch = booksSearch;
    }

    @GetMapping("/search")
    public VolumesResponse search(
            @RequestParam(required = false, defaultValue = "") String author,
            @RequestParam(required = false, defaultValue = "") String title
    ) {
        return booksSearch.searchWorks(title, author, 1, 20);
    }
}
