package ovh.bookexchange.api.domains.booksearch;

import graphql.Assert;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ovh.bookexchange.api.domains.booksearch.dto.VolumesResponse;
import ovh.bookexchange.api.infrastructures.GoogleBookClient;

@SpringBootTest
class GoogleBookClientTest {

    @Autowired
    GoogleBookClient googleBookClient;

    Logger log = LoggerFactory.getLogger(GoogleBookClientTest.class);

    @Test
    void given_aSimpleSearchByTitle_when_searching_then_resultsAreReceived() {
        VolumesResponse volumesResponse = googleBookClient.searchWorks(
                "Eragon",
                "Christopher Paolini",
                1,
                20
        );

        Assert.assertNotNull(volumesResponse);
        log.info("Volumes :\n{}", volumesResponse.toString());
    }
}