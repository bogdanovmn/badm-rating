package com.github.bogdanovmn.badmrating.sources.rnbf;

import com.github.bogdanovmn.badmrating.core.ArchiveFileExternal;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;

@Slf4j
class ArchiveFilesHtmlPage extends HtmlPage<ArchiveFileExternal> {
    private final String urlPrefix;

    public ArchiveFilesHtmlPage(String html, String urlPrefix) {
        super(html);
        this.urlPrefix = urlPrefix;
    }

    @Override
    protected Set<ArchiveFileExternal> defineItems(Document doc) {
        Set<ArchiveFileExternal> result = new HashSet<>();
        Elements blocks = doc.select("a[href*=xls]");
        for (Element block : blocks) {
            String href = block.attr("href");
            ArchiveFileExternal file = ArchiveFileExternal.of(
                href.startsWith("/")
                    ? urlPrefix + href
                    : href
            );
            if (file != null) {
                result.add(file);
            }
        }
        return result;
    }
}
