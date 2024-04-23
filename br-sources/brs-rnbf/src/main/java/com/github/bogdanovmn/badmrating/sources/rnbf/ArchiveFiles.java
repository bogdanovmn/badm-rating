package com.github.bogdanovmn.badmrating.sources.rnbf;

import com.github.bogdanovmn.badmrating.core.ArchiveFileExternal;
import com.github.bogdanovmn.httpclient.core.ExternalHttpService;
import com.github.bogdanovmn.httpclient.core.HttpClient;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Slf4j
class ArchiveFiles extends ExternalHttpService<Set<ArchiveFileExternal>> {
    private static final String PREFIX_URL = "http://www.badm.ru";
    private static final String CURRENT_URL = PREFIX_URL + "/raiting.html";
    private static final String ARCHIVE_URL = PREFIX_URL + "/news/pressrelises/2376";

    ArchiveFiles(HttpClient httpClient, String urlPrefix) {
        super(httpClient, urlPrefix);
    }

    ArchiveFiles(HttpClient httpClient) {
        this(httpClient, PREFIX_URL);
    }

    Set<ArchiveFileExternal> all() throws IOException {
        log.info("Loading current archives");
        Set<ArchiveFileExternal> result = parsedServiceResponse(
            httpClient.get(CURRENT_URL)
        );
        log.info("Loading past years archives");
        result.addAll(
            parsedServiceResponse(
                httpClient.get(ARCHIVE_URL)
            )
        );
        log.info("Total archives loaded: {}", result.size());
        return result;
    }

    @Override
    protected Set<ArchiveFileExternal> parsedServiceResponse(String html) {
        Set<ArchiveFileExternal> result = new HashSet<>();
        Document doc = Jsoup.parse(html);

        Elements blocks = doc.select("a[href*=xls]");
        for (Element block : blocks) {
            ArchiveFileExternal file = ArchiveFileExternal.of(block.attr("href"));
            if (file != null) {
                result.add(file);
            }
        }
        return result;
    }
}
