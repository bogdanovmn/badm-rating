package com.github.bogdanovmn.badmrating.sources.rnbf;

import com.github.bogdanovmn.badmrating.core.ArchiveFileExternal;
import com.github.bogdanovmn.httpclient.core.HttpClient;
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

    private final HttpClient httpClient;

    Set<ArchiveFileExternal> all() throws IOException {
        log.info("Loading current archives");
        Set<ArchiveFileExternal> result = new ArchiveFilesHtmlPage(
            httpClient.get(CURRENT_URL),
            URL_PREFIX
        ).items();
        log.info("Loading past years archives");
        result.addAll(
            new ArchiveFilesHtmlPage(
                httpClient.get(ARCHIVE_URL),
                URL_PREFIX
            ).items()
        );
        log.info("Total archives loaded: {}", result.size());
        return result;
    }
}
