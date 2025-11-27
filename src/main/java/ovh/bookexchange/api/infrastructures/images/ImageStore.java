package ovh.bookexchange.api.infrastructures.images;

import lombok.extern.slf4j.Slf4j;
import ovh.bookexchange.api.domains.images.BadImageTypeException;
import ovh.bookexchange.api.domains.images.ImageStorable;
import ovh.bookexchange.api.domains.images.NotAnImageException;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class ImageStore implements ImageStorable {
    private final Path imageFolder;
    private final List<String> imageTypes;

    public ImageStore(Path imageFolder, List<String> imageTypes) {
        if (imageTypes == null || imageTypes.isEmpty()) {
            throw new IllegalArgumentException("Image types must be provided");
        }
        this.imageTypes = imageTypes;
        if (!Files.exists(imageFolder)) {
            throw new IllegalArgumentException("Image folder does not exist");
        }
        if (!Files.isDirectory(imageFolder)) {
            throw new IllegalArgumentException("Image folder must be a directory");
        }
        this.imageFolder = imageFolder;
    }

    @Override
    public String storeImage(byte[] input, long imageId) throws NotAnImageException, BadImageTypeException, IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(input);
        BufferedImageWithType image = checkImage(is);
        Files.write(resolveImage(imageId, image.getType()), input);
        return image.getType();
    }

    private BufferedImageWithType checkImage(ByteArrayInputStream input) throws NotAnImageException, BadImageTypeException {
        try {
            ImageInputStream imgInput = ImageIO.createImageInputStream(input);
            var readers = ImageIO.getImageReaders(imgInput);
            if (!readers.hasNext()) {
                throw new NotAnImageException();
            }
            var reader = readers.next();
            reader.setInput(imgInput);
            String formatName = reader.getFormatName().toLowerCase();
            if (!imageTypes.contains(formatName)) {
                ImageStore.log.error("Bad image type: {}", formatName);
                throw new BadImageTypeException();
            }
            BufferedImage image = reader.read(0);
            return new BufferedImageWithType(image, formatName);
        } catch (IOException e) {
            throw new NotAnImageException();
        }
    }

    @Override
    public void delete(long imageId, String imageType) throws IOException {
        Files.delete(resolveImage(imageId, imageType));
    }

    @Override
    public byte[] getImageData(long imageId, String imageType) throws IOException {
        return Files.readAllBytes(resolveImage(imageId, imageType));
    }

    private Path resolveImage(long imageId, String imageType) {
        return imageFolder.resolve(imageId + "." + imageType);
    }
}
