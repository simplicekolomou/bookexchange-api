package ovh.bookexchange.api.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ovh.bookexchange.api.controllers.representations.UserRep;
import ovh.bookexchange.api.domains.entities.Adress;
import ovh.bookexchange.api.domains.entities.EndUser;
import ovh.bookexchange.api.infrastructures.EndUserRepository;

import java.security.Principal;

@RestController
@RequestMapping("/users")
public class EndUserController {
    private final EndUserRepository endUserRepository;
    private final ModelMapper mapper;

    public EndUserController(EndUserRepository endUserRepository, ModelMapper mapper) {
        this.endUserRepository = endUserRepository;
        this.mapper = mapper;
    }
    @GetMapping("/me")
    @ResponseBody
    public UserRep getUserById(Principal principal) {
        String email = principal.getName();
        EndUser endUser = endUserRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found"));
        return mapper.map(endUser, UserRep.class);
    }

    @PutMapping("/me")
    public void updateUser(@Valid @RequestBody UserRep userRep, Principal principal) {
        String email = principal.getName();
        EndUser endUser = endUserRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found"));
        endUser.setFirstName(userRep.getFirstName());
        endUser.setLastName(userRep.getLastName());
        endUser.setAdress(userRep.getAdress());
        endUser.setVisible(userRep.isVisible());
        endUser.setBio(userRep.getBio());
        endUserRepository.save(endUser);
    }
}
