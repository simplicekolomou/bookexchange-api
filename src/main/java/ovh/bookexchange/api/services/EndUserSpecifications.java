package ovh.bookexchange.api.services;

import org.springframework.data.jpa.domain.Specification;
import ovh.bookexchange.api.domains.entities.EndUser;

public class EndUserSpecifications {

    public static Specification<EndUser> firstNameContains(String firstName) {
        return (root, query, cb) ->
                cb.like(
                        cb.lower(root.get("firstName")),
                        "%" + firstName.toLowerCase() + "%"
                );
    }

    public static Specification<EndUser> lastNameContains(String lastName) {
        return (root, query, cb) ->
                cb.like(
                        cb.lower(root.get("lastName")),
                        "%" + lastName.toLowerCase() + "%"
                );
    }

    /**
     * Recherche libre : match sur firstName OU lastName
     */
    public static Specification<EndUser> nameContains(String q) {
        return (root, query, cb) -> {
            String like = "%" + q.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), like),
                    cb.like(cb.lower(root.get("lastName")), like)
            );
        };
    }
}

