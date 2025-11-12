package ovh.bookexchange.api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ovh.bookexchange.api.controllers.representations.UserRepresentation;
import ovh.bookexchange.api.domains.entities.EndUser;
import ovh.bookexchange.api.infrastructures.EndUserRepository;

import java.security.Principal;

@RestController
@RequestMapping("/users")
public class EndUserController {
    private final EndUserRepository endUserRepository;

    public EndUserController(EndUserRepository endUserRepository) {
        this.endUserRepository = endUserRepository;
    }
    @GetMapping("/me")
    @ResponseBody
    public UserRepresentation getUserById(Principal principal) {
        String email = principal.getName();
        EndUser endUser = endUserRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found"));
        return new UserRepresentation(endUser.getFirstName(), endUser.getLastName(), endUser.getEmail(), endUser.isAdmin(), endUser.getProfilePicture(), endUser.getBio(), endUser.getAdress());
    }
}
