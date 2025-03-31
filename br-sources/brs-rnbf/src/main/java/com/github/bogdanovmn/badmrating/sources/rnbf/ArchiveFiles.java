package com.github.bogdanovmn.badmrating.sources.rnbf;

import com.github.bogdanovmn.badmrating.core.ArchiveFileExternal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
class ArchiveFiles {
    private static final String URL_PREFIX = "http://www.badm.ru";
    private static final String CURRENT_URL = URL_PREFIX + "/raiting.html";
    private static final String ARCHIVE_URL = URL_PREFIX + "/news/pressrelises/2376";

    Set<ArchiveFileExternal> all() throws IOException {
        log.info("Loading current archives");
        Set<ArchiveFileExternal> result = new ArchiveFilesHtmlDocument(CURRENT_URL, URL_PREFIX)
            .archiveFiles();
        log.info("Loading past years archives");
        result.addAll(
            new ArchiveFilesHtmlDocument(ARCHIVE_URL, URL_PREFIX)
                .archiveFiles()
        );
        log.info("Total archives loaded: {}", result.size());
        return result;
    }
}
