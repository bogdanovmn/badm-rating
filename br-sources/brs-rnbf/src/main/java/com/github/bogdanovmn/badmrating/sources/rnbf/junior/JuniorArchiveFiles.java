package com.github.bogdanovmn.badmrating.sources.rnbf.junior;

import com.github.bogdanovmn.badmrating.core.ArchiveFileExternal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
class JuniorArchiveFiles {
    private static final String URL_PREFIX = "http://www.badm.ru";
    private static final String CURRENT_URL = URL_PREFIX + "/index.php?page_id=229";

    Set<ArchiveFileExternal> all() throws IOException {
        log.info("Loading current archives");
        JuniorArchiveFilesHtmlDocument mainPage = new JuniorArchiveFilesHtmlDocument(CURRENT_URL, URL_PREFIX);
        Set<ArchiveFileExternal> result = mainPage.archiveFiles();
        Set<String> historicalPages = mainPage.historicalArchivePages();
        log.info("Loading past years archives ({} pages)", historicalPages.size());
        for (String pageUrl : historicalPages) {
            log.info("Processing {}", pageUrl);
            Set<ArchiveFileExternal> files = new JuniorArchiveFilesHtmlDocument(pageUrl, URL_PREFIX).archiveFiles();
            result.addAll(files);
            log.info("Found {} files", files.size());
        }
        log.info("Total archives loaded: {}", result.size());
        return result;
    }
}
