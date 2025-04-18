package com.github.bogdanovmn.badmrating.sources.rnbf;

import com.github.bogdanovmn.badmrating.core.PersonalRating;
import com.github.bogdanovmn.badmrating.core.Player;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void ratingDuplicate_TheSameNameOnTheSamePlayTypeCase1() throws URISyntaxException {
        List<PersonalRating> content = new RnbfArchiveFile(FILE_RESOURCE.path("2023-05-23.xls")).content();
        assertEquals(1254, content.size());
        List<PersonalRating> rating = byName(content, "Борисов Никита");
        assertEquals(5, rating.size());
        Map<Player, List<PersonalRating>> playerRating = rating.stream().collect(groupingBy(PersonalRating::getPlayer));
        assertEquals(2, playerRating.keySet().size());
        assertTrue(
            playerRating.containsKey(
                Player.builder().name("Борисов Никита").year(2002).region("МСО").build()
            )
        );
        assertTrue(
            playerRating.containsKey(
                Player.builder().name("Борисов Никита").year(2006).region("МГС").build()
            )
        );
    }

//    @Test
//    void ratingDuplicate_TheSameNameOnTheSamePlayTypeCase2() throws URISyntaxException {
//        List<PersonalRating> content = new RnbfArchiveFile(FILE_RESOURCE.path("2009-01-22.xls")).content();
//        assertEquals(1813, content.size());
//        List<PersonalRating> rating = byName(content, "Морозова Ольга");
//        assertEquals(5, rating.size());
//        assertEquals(2005, rating.get(0).getPlayer().getYear());
//        assertEquals(2005, rating.get(1).getPlayer().getYear());
//        assertEquals(2005, rating.get(2).getPlayer().getYear());
//    }

    private List<PersonalRating> byName(List<PersonalRating> ratings, String playerName) {
        return ratings.stream()
            .filter(r -> r.getPlayer().getName().equals(playerName))
            .toList();
    }
}