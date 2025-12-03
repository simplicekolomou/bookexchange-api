package ovh.bookexchange.api.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ovh.bookexchange.api.controllers.representations.UserRep;
import ovh.bookexchange.api.domains.images.BadImageTypeException;
import ovh.bookexchange.api.domains.images.ImageStorable;
import ovh.bookexchange.api.domains.entities.EndUser;
import ovh.bookexchange.api.domains.images.NotAnImageException;
import ovh.bookexchange.api.infrastructures.repos.EndUserRepository;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.security.Principal;

@RestController
@RequestMapping("/users")
public class EndUserController {
    private final EndUserRepository endUserRepository;
    private final ModelMapper mapper;

    private final ImageStorable imgStore;

    public EndUserController(EndUserRepository endUserRepository, ModelMapper mapper, ImageStorable imgStore) {
        this.endUserRepository = endUserRepository;
        this.mapper = mapper;
        this.imgStore = imgStore;
    }
    @GetMapping("/me")
    @ResponseBody
    public UserRep getUserById(Principal principal) {
        String email = principal.getName();
        EndUser endUser = endUserRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found"));
        return mapper.map(endUser, UserRep.class);
    }


    /**
     * Update user information
     * @param userRep Une representation de l'utilisateur modififé.
     * !!! seul : firstName, lastName, adress, bio et visible sont modifiables
     * pas besoin d'inclure le reste des champs de UserRep.
     */
    @PutMapping("/me") //Note à moi-même : Ne plus faire des puts à la place de patch (mauvaise autodoc, implémentation bof).
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

    @PutMapping(value = "/me/profile-picture", consumes = {"image/jpeg", "image/png"})
    public void uploadProfileImage(@RequestBody byte[] imageBytes, Principal principal) {
        EndUser endUser = endUserRepository.findByEmail(principal.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found"));
        try {
            String format = imgStore.storeImage(imageBytes, endUser.getId());
            endUser.setProfilePicture(format);
            endUserRepository.save(endUser);
        } catch (BadImageTypeException | NotAnImageException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error saving profile picture");
        }
    }

    @DeleteMapping("/me/profile-picture")
    public void deleteProfileImage(Principal principal) {
        EndUser endUser = endUserRepository.findByEmail(principal.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found"));
        try {
            imgStore.delete(endUser.getId(), endUser.getProfilePicture());
        } catch (NoSuchFileException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No profile picture found");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting profile picture");
        }

        endUser.setProfilePicture(null);
        endUserRepository.save(endUser);
    }

    @GetMapping(value="/me/profile-picture", produces = "image/*")
    public ResponseEntity<byte[]> getProfileImage(Principal principal) {
        EndUser endUser = endUserRepository.findByEmail(principal.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found"));
        if (endUser.getProfilePicture() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No profile picture found");
        }
        try {
            byte[] data = imgStore.getImageData(endUser.getId(), endUser.getProfilePicture());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("image", endUser.getProfilePicture()));
            return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting profile picture");
        }
    }
}
