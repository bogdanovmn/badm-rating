package com.github.bogdanovmn.badmrating.sources.rnbf;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

abstract public class HtmlDocument {
    protected final Document htmlDocument;

    protected HtmlDocument(String url) throws IOException {
        this.htmlDocument = Jsoup.connect(url).get();
    }
}
