package com.github.bogdanovmn.badmrating.sources.rnbf;

import com.github.bogdanovmn.badmrating.core.PersonalRating;
import com.github.bogdanovmn.badmrating.core.PlayType;
import com.github.bogdanovmn.badmrating.core.PlayerRank;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RnbfArchiveFileTest {
    private static final FileResource FILE_RESOURCE = new FileResource(RnbfArchiveFileTest.class);

    @Test
    void content() throws URISyntaxException {
        List<PersonalRating> content = new RnbfArchiveFile(FILE_RESOURCE.path("2016-01-21.xls")).content();
        assertEquals(1425, content.size());
        List<PersonalRating> rating = byName(content, "Болотова Екатерина");
        assertEquals(3, rating.size());
        assertEquals(1992, rating.get(0).getPlayer().getYear());

        rating = byName(content, "Морозова Ольга");
        assertEquals(1, rating.size());
        assertEquals(1995, rating.get(0).getPlayer().getYear());

        rating = byName(content, "Червякова Анастасия");
        assertEquals(3, rating.size());
        assertEquals(1992, rating.get(0).getPlayer().getYear());
        assertEquals(2003, rating.get(1).getPlayer().getYear());
        assertEquals(1992, rating.get(2).getPlayer().getYear());

    }

    @Test
    void content1() throws URISyntaxException {
        List<PersonalRating> content = new RnbfArchiveFile(FILE_RESOURCE.path("2021-03-02.xls")).content();
        assertEquals(5720, content.size());
        List<PersonalRating> rating = byName(content, "Степаков Глеб");
        assertEquals(3, rating.size());
        assertEquals(2005, rating.get(0).getPlayer().getYear());
        assertEquals(2005, rating.get(1).getPlayer().getYear());
        assertEquals(2005, rating.get(2).getPlayer().getYear());
    }

    @Test
    void contentJunior() throws URISyntaxException {
        List<PersonalRating> content = new RnbfArchiveFile(FILE_RESOURCE.path("2024-06-04.xls")).content();
        assertEquals(7952, content.size());
        List<PersonalRating> rating = byName(content, "Малый Семен");
        assertEquals(3, rating.size());
        assertEquals(2007, rating.get(0).getPlayer().getYear());
    }

    @Test
    void contentJuniorElder() throws URISyntaxException {
        List<PersonalRating> content = new RnbfArchiveFile(FILE_RESOURCE.path("2009-01-02.xls")).content();
        assertEquals(1813, content.size());
        List<PersonalRating> rating = byName(content, "Сорокин Александр");
        assertEquals(3, rating.size());
        assertEquals(1993, rating.get(2).getPlayer().getYear());
    }

    @Test
    void contentYearParseCase1() throws URISyntaxException {
        List<PersonalRating> content = new RnbfArchiveFile(FILE_RESOURCE.path("2022-05-17.xls")).content();
        assertEquals(1286, content.size());
        List<PersonalRating> rating = byName(content, "Капкаун Елисей");
        assertEquals(1, rating.size());
        assertEquals(2007, rating.get(0).getPlayer().getYear());
        assertEquals("МСО", rating.get(0).getPlayer().getRegion());
        assertEquals(PlayerRank.R2, rating.get(0).getPlayer().getRank());
        assertEquals(PlayType.MD, rating.get(0).getType());
        assertEquals(565, rating.get(0).getValue());
    }

    @Test
    void contentYearParseCase2() throws URISyntaxException {
        List<PersonalRating> content = new RnbfArchiveFile(FILE_RESOURCE.path("2022-05-24.xls")).content();
        assertEquals(1309, content.size());
        List<PersonalRating> rating = byName(content, "Капкаун Елисей");
        assertEquals(1, rating.size());
        assertEquals(2007, rating.get(0).getPlayer().getYear());
        assertEquals("МСО", rating.get(0).getPlayer().getRegion());
        assertEquals(PlayerRank.R2, rating.get(0).getPlayer().getRank());
        assertEquals(PlayType.MD, rating.get(0).getType());
        assertEquals(565, rating.get(0).getValue());
    }

    @Test
    void contentSheetPlayersCount() throws URISyntaxException {
        List<PersonalRating> content = new RnbfArchiveFile(FILE_RESOURCE.path("2019-02-26.xls")).content();
        assertEquals(1405, content.size());
        Map<PlayType, List<PersonalRating>> data = content.stream().collect(Collectors.groupingBy(
            PersonalRating::getType,
            Collectors.collectingAndThen(
                Collectors.toList(),
                list -> list.stream()
                    .sorted(Comparator.comparingInt(PersonalRating::getValue).reversed())
                    .collect(Collectors.toList())
            )
        ));
        assertEquals(223, data.get(PlayType.WS).size());
        assertEquals(265, data.get(PlayType.WD).size());
        assertEquals(224, data.get(PlayType.MS).size());
        assertEquals(274, data.get(PlayType.MD).size());
        assertEquals(220 + 199, data.get(PlayType.XD).size());
    }

    @Test
    void playerYearAndRegionCorrection() throws URISyntaxException {
        List<PersonalRating> content = new RnbfArchiveFile(FILE_RESOURCE.path("2019-02-26.xls")).content();
        assertEquals(1405, content.size());
        List<PersonalRating> rating = byName(content, "Соколова Ольга");

        assertEquals(3, rating.size());
        assertNull(rating.get(0).getPlayer().getYear());
        assertEquals("МСГ", rating.get(0).getPlayer().getRegion());
        assertEquals(PlayerRank.KMS, rating.get(0).getPlayer().getRank());
        assertEquals(5815, rating.get(0).getValue());

        assertEquals(2002, rating.get(1).getPlayer().getYear());
        assertEquals("МСГ", rating.get(1).getPlayer().getRegion());
        assertEquals(PlayerRank.KMS, rating.get(1).getPlayer().getRank());
        assertEquals(7370, rating.get(1).getValue());

        assertNull(rating.get(2).getPlayer().getYear());
        assertEquals("СПГ", rating.get(2).getPlayer().getRegion());
        assertEquals(PlayerRank.KMS, rating.get(2).getPlayer().getRank());
        assertEquals(3415, rating.get(2).getValue());
    }

    @Test
    void contentWithoutHeader() throws URISyntaxException {
        List<PersonalRating> content = new RnbfArchiveFile(FILE_RESOURCE.path("2010-07-02.xls")).content();
        assertEquals(681, content.size());
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