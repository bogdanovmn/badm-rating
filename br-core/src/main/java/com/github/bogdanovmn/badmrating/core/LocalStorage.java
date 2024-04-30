package com.github.bogdanovmn.badmrating.core;


import com.github.bogdanovmn.common.files.Directory;
import com.github.bogdanovmn.httpclient.simple.SimpleHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class LocalStorage {
    private final String rootDir;
    private final RatingSource source;

    private Map<LocalDate, ArchiveFile> files;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String FILE_EXT = "xls";

    public void update() throws IOException {
        init();
        try (SimpleHttpClient httpClient = new SimpleHttpClient()) {
            for (ArchiveFileExternal externalArchive : source.archiveOverview()) {
                log.info("Checking for archive at {}", externalArchive.getDate().format(DATE_FORMAT));
                if (!files.containsKey(externalArchive.getDate())) {
                    log.info("Downloading {}", externalArchive.getUrl());
                    try (InputStream fileData = httpClient.downloadFile(externalArchive.getUrl())) {
                        Path file = saveFile(
                            externalArchive.getDate(),
                            IOUtils.toByteArray(fileData)
                        );
                        files.put(externalArchive.getDate(), source.archiveFile(file));
                    }
                }
                return;
            }
        }
    }

    public Optional<ArchiveFile> latest() {
        return files.entrySet().stream()
            .max(Map.Entry.comparingByKey())
            .map(Map.Entry::getValue);
    }

    private synchronized void init() throws IOException {
        if (null == files) {
            log.info("init storage: {}/{}", rootDir, source.id());
            Files.createDirectories(
                Paths.get(rootDir, source.id())
            );
            files = new Directory(
                Paths.get(rootDir, source.id()).toString()
            ).filesWithExtRecursively(FILE_EXT)
                .stream().map(source::archiveFile)
                .collect(
                    Collectors.toMap(ArchiveFile::date, Function.identity())
                );
            log.info("total {} files in the storage", files.size());
        }
    }

    private Path saveFile(LocalDate date, byte[] data) throws IOException {
        Path dir = Paths.get(rootDir, source.id(), String.valueOf(date.getYear()));
        Files.createDirectories(dir);
        Path filePath = dir.resolve(
            "%s.%s".formatted(date.format(DATE_FORMAT), FILE_EXT)
        );
        log.info("Saving to {}", filePath);
        return Files.write(filePath, data);
    }
}
