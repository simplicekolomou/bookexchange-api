package ovh.bookexchange.api.infrastructures.images;

import org.junit.jupiter.api.Test;
import ovh.bookexchange.api.domains.images.ImageStorable;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImageStoreTest {
    @Test
    void save() {
        assertDoesNotThrow(() -> {
            ImageStorable store = new ImageStore(Paths.get("src/test/resources/profile_pictures/"), List.of("jpeg", "png"));
            store.storeImage(Files.readAllBytes(Paths.get("src/test/resources/mona420.png")), 1);
            store.delete(1, "png");
        });
    }

}