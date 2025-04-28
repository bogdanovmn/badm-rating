package com.github.bogdanovmn.badmrating.sources.rnbf;

import com.github.bogdanovmn.badmrating.core.ArchiveFileExternal;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

class ArchiveFilesHtmlDocument extends HtmlDocument {
    private final String urlPrefix;

    public ArchiveFilesHtmlDocument(String url, String urlPrefix) throws IOException {
        super(url);
        this.urlPrefix = urlPrefix;
    }

    Set<ArchiveFileExternal> archiveFiles() {
        Set<ArchiveFileExternal> result = new HashSet<>();
        Elements blocks = htmlDocument.select("a[href*=xls]");
        for (Element block : blocks) {
            String href = block.attr("href");
            String text = block.text();
            ArchiveFileExternal file = text.toLowerCase().contains("ветеранский")
                ? null
                : ArchiveFileExternal.of(
                    href.startsWith("/")
                        ? urlPrefix + href
                        : href,
                    text
                );
            if (file != null) {
                result.add(file);
            }
        }
        return result;
    }
}
