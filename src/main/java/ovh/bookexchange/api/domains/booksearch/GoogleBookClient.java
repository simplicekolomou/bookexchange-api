package ovh.bookexchange.api.domains.booksearch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ovh.bookexchange.api.domains.booksearch.dto.VolumesResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.StringJoiner;

@Slf4j
@Component
public class GoogleBookClient implements BookClientInterface {
    private final RestClient client;
    private final String baseUrl;
    private final int retries;
    private final String apiKey;

    public GoogleBookClient(
            @Value("${app.books.gb.base-url}") String baseUrl,
            @Value("${app.books.gb.connect-timeout-ms}") int connectTimeoutMs,
            @Value("${app.books.gb.read-timeout-ms}") int readTimeoutMs,
            @Value("${app.books.gb.retries}") int retries,
            @Value("${app.books.gb.api-key}") String apiKey
    ) {
        this.baseUrl = baseUrl;
        this.retries = Math.max(0, retries);
        this.apiKey = apiKey;

        var rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(connectTimeoutMs);
        rf.setReadTimeout(readTimeoutMs);

        this.client = RestClient.builder()
                .requestFactory(rf)
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", "BookService/1.0 (Contact=theo.andernack@gmail.com")
                .build();
    }

    /**
     * Méthode pour faire une recherche sur les livres de Google Books
     *
     * <a href="https://developers.google.com/books/docs/v1/using?hl=fr">Doc Google Books API</a>
     * @param title         Le titre de l'œuvre
     * @param author        L'autheur de l'œuvre
     * @param startIndex    position dans la collection à partir de laquelle commencer. L'indice du premier élément est 0.
     * @param maxResults    nombre maximal de résultats à renvoyer. La valeur par défaut est 10 et la valeur maximale autorisée est 40.
     * @return
     */
    public VolumesResponse searchWorks(String title, String author, int startIndex, int maxResults) {
        String url = buildGoogleBooksUrl(baseUrl, apiKey, title, author, "fr", startIndex, maxResults, "relevance", "books");

        System.out.println(url);
        log.info("url: {}", url);

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
