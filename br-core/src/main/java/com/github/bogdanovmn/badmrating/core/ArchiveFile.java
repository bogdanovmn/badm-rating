package com.github.bogdanovmn.badmrating.core;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Set;

@RequiredArgsConstructor
public abstract class ArchiveFile {
    protected final Path path;

    public abstract Set<PersonalRating> content() throws IOException;

    public LocalDate date() {
        return LocalDate.parse(
            path.getFileName().toString().substring(0, 10)
        );
    }
}
