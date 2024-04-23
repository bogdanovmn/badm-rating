package com.github.bogdanovmn.badmrating.core;

import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Set;

@RequiredArgsConstructor
public class ArchiveFile {
    private final Path path;

    public Set<PersonalRating> content() {
        return null;
    }

    public LocalDate date() {
        return null;
    }
}
