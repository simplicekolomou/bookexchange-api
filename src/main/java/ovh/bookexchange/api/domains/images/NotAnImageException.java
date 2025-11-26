package ovh.bookexchange.api.domains.images;

public class NotAnImageException extends Exception{
    public NotAnImageException() {
        super();
    }
    public NotAnImageException(String message) {
        super(message);
    }
}
