package com.github.bogdanovmn.badmrating.sources.rnbf.junior;

import com.github.bogdanovmn.badmrating.core.ArchiveFile;
import com.github.bogdanovmn.badmrating.core.ArchiveFileExternal;
import com.github.bogdanovmn.badmrating.core.RatingSource;
import com.github.bogdanovmn.badmrating.sources.rnbf.RnbfArchiveFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public class RussianNationalBadmintonFederationJunior implements RatingSource {

    private final JuniorArchiveFiles archiveFiles = new JuniorArchiveFiles();

    @Override
    public Set<ArchiveFileExternal> archiveOverview() throws IOException {
        return archiveFiles.all();
    }

    @Override
    public String id() {
        return "RNBFJunior";
    }

    @Override
    public ArchiveFile archiveFile(Path path) {
        return new RnbfArchiveFile(path);
    }
}
