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
                Arguments.of("/files/File/ranking/2017/rank13_07_2017.xls", LocalDate.of(2017, 7, 13)),
                Arguments.of("/files/File/ranking/2021/rank_2021_01_12.xls", LocalDate.of(2021, 1, 12)),
                Arguments.of("/files/File/ranking/2020/rank_2020_12_01(1).xls", LocalDate.of(2020, 12, 1)),
                Arguments.of("/files/File/news/2020/rank_13_10_2020.xls", LocalDate.of(2020, 10, 13)),
                Arguments.of("/files/File/ranking/2021/rank_21_06_08.xls", LocalDate.of(2021, 6, 8)),
                Arguments.of("/files/File/ranking/2015/rank09_04_15.xls", LocalDate.of(2015, 4, 9)),
                Arguments.of("https://nfbr.ru/files/File/ranking/2024/rank 20240109.xls", LocalDate.of(2024, 1, 9)),
                Arguments.of("https://nfbr.ru/files/File/ranking/2024/rank_20240109.xls", LocalDate.of(2024, 1, 9)),
                Arguments.of("https://nfbr.ru/files/File/ranking/2023/rank_20231010%20(2).xls", LocalDate.of(2023, 10, 10)),
                Arguments.of("https://nfbr.ru/files/File/ranking/2023/rank_20231128(1).xls", LocalDate.of(2023, 11, 28)),
                Arguments.of("https://nfbr.ru/files/File/ranking/2023/rank_123.xls", null)
            );
        }
    }
    @ParameterizedTest
    @ArgumentsSource(Args.class)
    public void of(String url, LocalDate expectedDate) {
        ArchiveFileExternal file = ArchiveFileExternal.of(url);
        if (expectedDate == null) {
            assertNull(file);
        } else {
            assertNotNull(file);
            assertEquals(file.getDate(), expectedDate);
        }
    }
}