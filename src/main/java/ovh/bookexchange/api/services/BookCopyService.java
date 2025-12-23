package ovh.bookexchange.api.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ovh.bookexchange.api.domains.entities.BookCopy;
import ovh.bookexchange.api.domains.entities.PhysicalState;
import ovh.bookexchange.api.infrastructures.repos.BookCopyRepository;


@Service
@RequiredArgsConstructor
public class BookCopyService {

    private final BookCopyRepository repository;

    /**
     * Cherche des copies de livres en fonction des critères fournis.
     * @param isbn          Le numéro ISBN du livre. Si null ou vide, ce critère est ignoré.
     * @param author        Le nom de l'auteur du livre. Si null ou vide, ce critère est ignoré.
     * @param title         Le titre du livre. Si null ou vide, ce critère est ignoré.
     * @param availability  La disponibilité du livre ("true" pour disponible).
     *                      Si null ou différent de "true", ce critère est ignoré.
     * @param bookState     L'état physique du livre ("verygood", "good", "decent", "new").
     *                      Si null ou vide, ce critère est ignoré.
     * @param pageable      Les informations de pagination.
     * @return Une page de copies de livres correspondant aux critères de recherche.
     */
    public Page<BookCopy> search(String isbn, String author, String title, String availability, String bookState, Pageable pageable) {
        Specification<BookCopy> spec = Specification.where(null);

        if (isbn != null && !isbn.isBlank()) {
            spec = spec.and(BookCopySpecifications.isbnContains(isbn));
        }

        if (title != null && !title.isBlank()) {
            spec = spec.and(BookCopySpecifications.titleContains(title));
        }

        if (author != null && !author.isBlank()) {
            spec = spec.and(BookCopySpecifications.authorContains(author));
        }

        if (availability != null && availability.equals("true")) {
            spec = spec.and(BookCopySpecifications.isAvailable());
        }

        if (bookState != null && !bookState.isBlank()) {
            String s = bookState.toLowerCase();
            PhysicalState state = switch (s) {
                case "verygood" -> PhysicalState.VERY_GOOD;
                case "good" -> PhysicalState.GOOD;
                case "decent" -> PhysicalState.DECENT;
                case "new" -> PhysicalState.NEW;
                default -> null;
            };
            spec = spec.and(BookCopySpecifications.hasBookState(state));
        }

        return repository.findAll(spec, pageable);
    }
}

