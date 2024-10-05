package com.github.bogdanovmn.badmrating.sources.rnbf;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Set;

@RequiredArgsConstructor
abstract public class HtmlPage<T> {
    private final String html;

    public final Set<T> items() {
        return defineItems(Jsoup.parse(html));
    }

    abstract protected Set<T> defineItems(Document doc);
}
