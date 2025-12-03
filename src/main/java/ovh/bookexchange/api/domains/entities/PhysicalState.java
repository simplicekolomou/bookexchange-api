package ovh.bookexchange.api.domains.entities;

/**
 * Enumération des états physiques des copies de livres.
 * Est lié à la même collection dans le front (bookType) donc /!\ aux modifications
 */
public enum PhysicalState {
    NEW,
    VERY_GOOD,
    GOOD,
    DECENT
}