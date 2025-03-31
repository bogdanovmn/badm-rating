package com.github.bogdanovmn.badmrating.sources.rnbf.junior;

import com.github.bogdanovmn.badmrating.core.ArchiveFileExternal;
import com.github.bogdanovmn.badmrating.sources.rnbf.HtmlDocument;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class JuniorArchiveFilesHtmlDocument extends HtmlDocument {
    private final String urlPrefix;

    private static final Pattern DATE_LINK_TITLE_PATTERN = Pattern.compile(
        "(\\d\\d?\\s+(января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря))"
            + "|(вгуст\\s+\\d{4})"
            + "|на\\s+(\\d{1,2}\\.\\d{1,2}\\.\\d{4})"
    );

    public JuniorArchiveFilesHtmlDocument(String url, String urlPrefix) throws IOException {
        super(url);
        this.urlPrefix = urlPrefix;
    }

    Set<ArchiveFileExternal> archiveFiles() {
        return linkElements().stream()
            .map(link -> {
                String href = link.attr("href");
                String text = link.text();
                return ArchiveFileExternal.of(
                    href.startsWith("/") ? urlPrefix + href : href,
                    text
                );
            }).filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Elements linkElements() {
        return Optional.ofNullable(
            htmlDocument.selectFirst("p:contains(ЕДИНЫЙ)")
        ).map(Element::nextElementSibling)
            .filter(element -> element.tagName().equals("table"))
            .map(table -> table.select("a[href$=.xls]"))
            .orElseGet(() -> {
                Elements links = htmlDocument.select("a[href$=.xls]");
                links.removeIf(link -> !DATE_LINK_TITLE_PATTERN.matcher(link.text()).find());
                return links;
            });
    }

    Set<String> historicalArchivePages() {
        return Optional.of(
            htmlDocument.select("a[href*=news/pressrelises]")
        ).map(links -> links.stream()
            .map(link -> link.attr("href"))
            .collect(Collectors.toSet())
        ).orElse(Collections.emptySet());
    }
}
