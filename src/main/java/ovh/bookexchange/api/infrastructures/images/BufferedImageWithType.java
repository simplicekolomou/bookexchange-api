package ovh.bookexchange.api.infrastructures.images;

import lombok.Getter;

import java.awt.image.BufferedImage;

@Getter
public class BufferedImageWithType {
    private final BufferedImage image;
    private final String type;

    public BufferedImageWithType(BufferedImage image, String type) {
        this.image = image;
        this.type = type;
    }
}
