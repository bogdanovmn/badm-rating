package com.github.bogdanovmn.badmrating.sources.rnbf;

import com.github.bogdanovmn.httpclient.core.ExternalHttpService;
import com.github.bogdanovmn.httpclient.core.HttpClient;
import com.github.bogdanovmn.httpclient.core.ResponseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

class ArchiveFiles extends ExternalHttpService<Set<ArchiveFileExternal>> {
    private static final String PREFIX_URL = "http://www.badm.ru";
    private static final String CURRENT_URL = PREFIX_URL + "/raiting.html";
    private static final String ARCHIVE_URL = PREFIX_URL + "/news/pressrelises/2376";

    public ArchiveFiles(HttpClient httpClient, String urlPrefix) {
        super(httpClient, urlPrefix);
    }

    public ArchiveFiles(HttpClient httpClient) {
        this(httpClient, PREFIX_URL);
    }

    public Set<ArchiveFileExternal> all() throws IOException, ResponseException {
        return parsedServiceResponse(
            httpClient.get(CURRENT_URL)
        );
    }

    @Override
    protected Set<ArchiveFileExternal> parsedServiceResponse(String html) throws ResponseException {
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
