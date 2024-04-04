package com.github.bogdanovmn.badmrating.sources.rnbf;

import com.github.bogdanovmn.httpclient.core.ResponseException;
import com.github.bogdanovmn.httpclient.simple.SimpleHttpClient;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Set;

@RequiredArgsConstructor
public class RussianNationalBadmintonFederation {
    private static final String PREFIX_URL = "http://www.badm.ru";

    private final ArchiveFiles archiveFiles = new ArchiveFiles(new SimpleHttpClient());

    public ArchiveOverview archiveOverview() throws ResponseException, IOException {
        return new ArchiveOverview(
            archiveFiles.all()
        );
    }
}
