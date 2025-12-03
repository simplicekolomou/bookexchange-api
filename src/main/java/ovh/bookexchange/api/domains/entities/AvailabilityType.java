package ovh.bookexchange.api.domains.entities;

/**
 * Enumération des types de disponibilités des copies de livres.
 * Est lié à la même collection dans le front (bookType) donc /!\ aux modifications
 */
public enum AvailabilityType {
    NONE,
    FOR_SALE,
    FOR_TRADE,
    FOR_GIFT
}
