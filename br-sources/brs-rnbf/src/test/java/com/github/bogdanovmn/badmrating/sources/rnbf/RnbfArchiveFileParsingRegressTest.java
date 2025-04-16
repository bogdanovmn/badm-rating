package com.github.bogdanovmn.badmrating.sources.rnbf;

import com.github.bogdanovmn.badmrating.core.PersonalRating;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RnbfArchiveFileParsingRegressTest {
    private static final FileResource FILE_RESOURCE = new FileResource(RnbfArchiveFileParsingRegressTest.class);


    @Test
    void ratingDuplicate_hiddenSheetCase() throws URISyntaxException {
        List<PersonalRating> content = new RnbfArchiveFile(FILE_RESOURCE.path("2015-11-08.xls")).content();
        assertEquals(5143, content.size());
        List<PersonalRating> rating = byName(content, "Пузырев Александр");
        assertEquals(3, rating.size());
        assertEquals(2005, rating.get(0).getPlayer().getYear());
        assertEquals(2005, rating.get(1).getPlayer().getYear());
        assertEquals(2005, rating.get(2).getPlayer().getYear());
    }

    private List<PersonalRating> byName(List<PersonalRating> ratings, String playerName) {
        return ratings.stream()
            .filter(r -> r.getPlayer().getName().equals(playerName))
            .toList();
    }
}