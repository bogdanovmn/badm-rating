package com.github.bogdanovmn.badmrating.sources.rnbf;

import com.github.bogdanovmn.badmrating.core.PersonalRating;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RnbfArchiveFileTest {
    private static final FileResource FILE_RESOURCE = new FileResource(RnbfArchiveFileTest.class);

    @Test
    void content() throws URISyntaxException {
        List<PersonalRating> content = new RnbfArchiveFile(FILE_RESOURCE.path("2016-01-21.xls")).content();
        assertEquals(1426, content.size());
        List<PersonalRating> rating = byName(content, "Болотова Екатерина");
        assertEquals(3, rating.size());
    }

    @Test
    void contentWithoutHeader() throws URISyntaxException {
        List<PersonalRating> content = new RnbfArchiveFile(FILE_RESOURCE.path("2010-07-02.xls")).content();
        assertEquals(679, content.size());
        List<PersonalRating> rating = byName(content, "Мальков Владимир");
        assertEquals(2, rating.size());
        assertEquals(9091, rating.get(0).getValue());
    }

    private List<PersonalRating> byName(List<PersonalRating> ratings, String playerName) {
        return ratings.stream()
            .filter(r -> r.getPlayer().getName().equals(playerName))
            .toList();
    }
}