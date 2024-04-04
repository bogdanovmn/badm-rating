package com.github.bogdanovmn.badmrating.sources.rnbf;

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