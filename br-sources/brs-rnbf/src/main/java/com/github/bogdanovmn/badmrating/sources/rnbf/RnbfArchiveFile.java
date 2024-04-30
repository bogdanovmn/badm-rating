package com.github.bogdanovmn.badmrating.sources.rnbf;

import com.github.bogdanovmn.badmrating.core.ArchiveFile;
import com.github.bogdanovmn.badmrating.core.PersonalRating;
import com.github.bogdanovmn.badmrating.core.excel.ExcelFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.Set;

@Slf4j
public class RnbfArchiveFile extends ArchiveFile {

    public RnbfArchiveFile(Path path) {
        super(path);
    }

    @Override
    public Set<PersonalRating> content() throws IOException {
        ExcelFile excel = new ExcelFile(Files.newInputStream(path, StandardOpenOption.READ));
        log.info(excel.sheets().toString());
        return null;
    }
}
