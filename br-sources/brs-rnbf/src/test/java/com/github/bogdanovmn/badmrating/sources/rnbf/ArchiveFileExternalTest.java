package com.github.bogdanovmn.badmrating.sources.rnbf;

import com.github.bogdanovmn.badmrating.core.ArchiveFileExternal;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ArchiveFileExternalTest {


    static class Args implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                Arguments.of("","/files/File/ranking/ranking06122012(1).xls", LocalDate.of(2012, 12, 6)),
                Arguments.of("","/files/File/ranking/spisok06122012(1).xls", LocalDate.of(2012, 12, 6)),
                Arguments.of("","/files/File/ranking/2010_7_2.xls", LocalDate.of(2010, 7, 2)),
                Arguments.of("","/files/File/ranking/2009_12_31.xls", LocalDate.of(2009, 12, 31)),
                Arguments.of("","/files/File/ranking/2011/ranking_15_02.xls", LocalDate.of(2011, 2, 15)),
                Arguments.of("","/files/File/ranking/2011/rank_15_02.xls", LocalDate.of(2011, 2, 15)),
                Arguments.of("","/files/File/ranking/2012/rank_27_04(2).xls", LocalDate.of(2012, 4, 27)),
                Arguments.of("","/files/File/ranking/2012/ranking_27_04(2).xls", LocalDate.of(2012, 4, 27)),
                Arguments.of("","/files/File/ranking/2012/rank_gp_27_04(2).xls", LocalDate.of(2012, 4, 27)),
                Arguments.of("","/files/File/ranking/2012/ranking_29_02_12.xls", LocalDate.of(2012, 2, 29)),
                Arguments.of("","/files/File/ranking/2014/ss_06022014.xls", LocalDate.of(2014, 2, 6)),
                Arguments.of("","/files/File/ranking/2014/gp_06022014.xls", LocalDate.of(2014, 2, 6)),
                Arguments.of("","/files/File/ranking/2014/ss_22_03_14.xls", LocalDate.of(2014, 3, 22)),
                Arguments.of("","/files/File/ranking/2013/ss_01.04.2013.xls", LocalDate.of(2013, 4, 1)),
                Arguments.of("","/files/File/ranking/2013/ss_01.04.2013(1).xls", LocalDate.of(2013, 4, 1)),
                Arguments.of("","/files/File/ranking/2013/ss01.04.2013(1).xls", LocalDate.of(2013, 4, 1)),
                Arguments.of("","/files/File/ranking/2013/gp01.04.2013.xls", LocalDate.of(2013, 4, 1)),
                Arguments.of("","/files/File/ranking/2017/rank13_07_2017.xls", LocalDate.of(2017, 7, 13)),
                Arguments.of("","/files/File/ranking/2021/rank_2021_01_12.xls", LocalDate.of(2021, 1, 12)),
                Arguments.of("","/files/File/ranking/2017/rank_01_01_2018.xls", LocalDate.of(2018, 1, 1)),
                Arguments.of("","/files/File/ranking/2020/rank_2020_12_01(1).xls", LocalDate.of(2020, 12, 1)),
                Arguments.of("","/files/File/news/2020/rank_13_10_2020.xls", LocalDate.of(2020, 10, 13)),
                Arguments.of("","/files/File/ranking/2021/rank_21_06_08.xls", LocalDate.of(2021, 6, 8)),
                Arguments.of("","/files/File/ranking/2015/rank09_04_15.xls", LocalDate.of(2015, 4, 9)),
                Arguments.of("","https://nfbr.ru/files/File/ranking/2024/rank 20240109.xls", LocalDate.of(2024, 1, 9)),
                Arguments.of("","https://nfbr.ru/files/File/ranking/2024/rank_20240109.xls", LocalDate.of(2024, 1, 9)),
                Arguments.of("","https://nfbr.ru/files/File/ranking/2023/rank_20231010%20(2).xls", LocalDate.of(2023, 10, 10)),
                Arguments.of("","https://nfbr.ru/files/File/ranking/2023/rank_20231128(1).xls", LocalDate.of(2023, 11, 28)),
                Arguments.of("","https://nfbr.ru/files/File/ranking/2023/rank_123.xls", null),
                Arguments.of("Рейтинг на 23 марта 2009 г.","https://www.badm.ru/files/File/ranking/DGP2008_09_reit_04.xls", LocalDate.of(2009, 3, 23)),
                Arguments.of("Рейтинг на 06.12.2018","https://www.badm.ru/files/File/ranking/DGPreit.xls", LocalDate.of(2018, 12, 6)),
                Arguments.of("Рейтинг на 16.12.2011","https://www.badm.ru/files/File/ranking/DGPreit.xls", LocalDate.of(2011, 12, 16)),
                Arguments.of("17 декабря","https://www.badm.ru/files/File/ranking/2021/DGPreit.xls", LocalDate.of(2021, 12, 17))
            );
        }
    }
    @ParameterizedTest
    @ArgumentsSource(Args.class)
    public void of(String text, String url, LocalDate expectedDate) {
        ArchiveFileExternal file = ArchiveFileExternal.of(url, text);
        if (expectedDate == null) {
            assertNull(file);
        } else {
            assertNotNull(file);
            assertEquals(expectedDate, file.getDate());
        }
    }
}