package com.github.bogdanovmn.badmrating.sources.rnbf;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ArchiveOverview {
    private final Set<ArchiveFileExternal> files;

    public ArchiveOverview(Set<ArchiveFileExternal> files) {
        this.files = files;
    }


    public Map<Integer, List<ArchiveFileExternal>> byYear() {
        return files.stream().collect(
            Collectors.groupingBy(f -> f.getDate().getYear())
        );
    }
}
