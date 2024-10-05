package com.github.bogdanovmn.badmrating.sources.rnbf;

import com.github.bogdanovmn.badmrating.core.ArchiveFile;
import com.github.bogdanovmn.badmrating.core.ArchiveFileExternal;
import com.github.bogdanovmn.badmrating.core.RatingSource;
import com.github.bogdanovmn.httpclient.simple.SimpleHttpClient;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

@RequiredArgsConstructor
public class RussianNationalBadmintonFederation implements RatingSource {

    private final ArchiveFiles archiveFiles = new ArchiveFiles(new SimpleHttpClient());

    @Override
    public Set<ArchiveFileExternal> archiveOverview() throws IOException {
        return archiveFiles.all();
    }

    @Override
    public String id() {
        return "RNBF";
    }

    @Override
    public ArchiveFile archiveFile(Path path) {
        return new RnbfArchiveFile(path);
    }
}
