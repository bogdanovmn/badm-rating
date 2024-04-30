package com.github.bogdanovmn.badmrating.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public interface RatingSource {
    Set<ArchiveFileExternal> archiveOverview() throws IOException;

    String id();

    ArchiveFile archiveFile(Path path);
}
