package ovh.bookexchange.api.infrastructures.booksearch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ovh.bookexchange.api.domains.booksearch.dto.VolumesResponse;

import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

@Slf4j
@Component
public class GoogleBookClient implements BookClientInterface {
    private final RestClient client;
    private final String baseUrl;
    private final int retries;
    private final String apiKey;

    public GoogleBookClient(Environment environment) {
        this.baseUrl = environment.getProperty("APP_BOOKS_GB_BASE_URL");
        int connectTimeoutMs = environment.getProperty("APP_BOOKS_GB_CONNECT_TIMEOUT_MS", Integer.class, 5000);
        int readTimeoutMs = environment.getProperty("APP_BOOKS_GB_READ_TIMEOUT_MS", Integer.class, 10000);
        this.retries = Math.max(0, environment.getProperty("APP_BOOKS_GB_RETRIES", Integer.class, 3));
        this.apiKey = environment.getProperty("APP_BOOKS_GB_API_KEY");

        var rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(connectTimeoutMs);
        rf.setReadTimeout(readTimeoutMs);

        this.client = RestClient.builder()
                .requestFactory(rf)
                .baseUrl(this.baseUrl)
                .defaultHeader("User-Agent", "BookService/1.0 (Contact=theo.andernack@gmail.com")
                .build();
    }

    /**
     * Méthode pour faire une recherche sur les livres de Google Books
     * <a href="https://developers.google.com/books/docs/v1/using?hl=fr">Doc Google Books API</a>
     * @param title         Le titre de l'œuvre
     * @param author        L'autheur de l'œuvre
     * @param startIndex    position dans la collection à partir de laquelle commencer. L'indice du premier élément est 0.
     * @param maxResults    nombre maximal de résultats à renvoyer. La valeur par défaut est 10 et la valeur maximale autorisée est 40.
     * @return Un objet VolumesResponse contenant les résultats de la recherche
     */
    public VolumesResponse searchWorks(String title, String author, int startIndex, int maxResults) {
        String url = buildGoogleBooksUrl(baseUrl, apiKey, title, author, "fr", startIndex, maxResults, "relevance", "books");

        System.out.println(url);
        log.info("url: {}", url);

        VolumesResponse response = makeApiCalls(url);
        removeNonIsbnIdentifiers(response);
        return response;

    }

    private void removeNonIsbnIdentifiers(VolumesResponse uncheckedIsbnResponse) {
        uncheckedIsbnResponse.items().forEach(v ->
            v.volumeInfo().industryIdentifiers().removeIf(i -> !i.type().startsWith("ISBN_"))
        );
    }

    private VolumesResponse makeApiCalls(String url) {
        int attempt = 0;
        RuntimeException last = null;
        while (attempt <= retries) {
            try {
                log.info("Attempt {}/{}", attempt + 1, retries + 1);
                return client.get()
                        .uri(url)
                        .retrieve()
                        .body(VolumesResponse.class);
            } catch (RuntimeException ex) {
                last = ex;
                attempt++;
                if (attempt > retries) break;
                try { Thread.sleep(100 + (long)(Math.random() * 200)); } catch (InterruptedException ignored) {}
            }
        }
        throw last != null ? last : new RuntimeException("Google Books search failed");
    }

    private String buildGoogleBooksUrl(
            String baseUrl,
            String apiKey,
            String title,
            String author,
            String langRestrict,   // e.g. "en", "fr" (optional)
            Integer startIndex,    // 0..n
            Integer maxResults,    // 1..40
            String orderBy,        // "relevance" or "newest" (optional)
            String printType       // "all", "books", "magazines" (optional)
    ) {
        // Build q=... using Google Books operators
        StringJoiner q = new StringJoiner(" ");
        if (title  != null && !title.isBlank())  q.add("intitle:"  + title.trim());
        if (author != null && !author.isBlank()) q.add("inauthor:" + author.trim());
        if (q.length() == 0) q.add("*"); // fallback: match all (or throw)

        int si = Math.max(0, startIndex == null ? 0 : startIndex);
        int mr = Math.max(1, Math.min(40, maxResults == null ? 10 : maxResults)); // API caps at 40

        UriComponentsBuilder b = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .path("/volumes")
                .queryParam("q", q.toString())
                .queryParam("startIndex", si)
                .queryParam("maxResults", mr);

        if (langRestrict != null && !langRestrict.isBlank()) b.queryParam("langRestrict", langRestrict);
        if (orderBy != null && !orderBy.isBlank())           b.queryParam("orderBy", orderBy);      // relevance|newest
        if (printType != null && !printType.isBlank())       b.queryParam("printType", printType);  // all|books|magazines
        if (apiKey != null && !apiKey.isBlank())             b.queryParam("key", apiKey);

        // (Optional) shrink payload with partial response:
        // b.queryParam("fields", "kind,totalItems,items(kind,id,volumeInfo/title,volumeInfo/authors,volumeInfo/publishedDate)");

        return b.encode(StandardCharsets.UTF_8).toUriString();
    }
}
