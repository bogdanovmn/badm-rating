package com.github.bogdanovmn.badmrating.core;

import java.io.IOException;
import java.util.Set;

public interface RatingSource {
    Set<ArchiveFileExternal> archiveOverview() throws IOException;

    String id();
}
