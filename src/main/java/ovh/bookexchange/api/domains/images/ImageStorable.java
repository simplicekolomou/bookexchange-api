package ovh.bookexchange.api.domains.images;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

public interface ImageStorable {
    public String save(byte[] image, long imageId) throws NotAnImageException, BadImageTypeException, IOException;

    public void delete(long imageId, String profilePicture) throws IOException;

    public byte[] getImageData(long imageId, String imageType) throws IOException;
}
