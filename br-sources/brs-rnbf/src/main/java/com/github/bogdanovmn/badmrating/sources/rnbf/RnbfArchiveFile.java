package com.github.bogdanovmn.badmrating.sources.rnbf;

import com.github.bogdanovmn.badmrating.core.ArchiveFile;
import com.github.bogdanovmn.badmrating.core.PersonalRating;
import com.github.bogdanovmn.badmrating.core.PlayType;
import com.github.bogdanovmn.badmrating.core.excel.ExcelFile;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

import static com.github.bogdanovmn.badmrating.core.PlayType.UNKNOWN;

@Slf4j
public class RnbfArchiveFile extends ArchiveFile {

    private static final Set<String> SHEETS_TO_SKIP = Set.of("Соревнования");

    public RnbfArchiveFile(Path path) {
        super(path);
    }

    @Override
    public Set<PersonalRating> content() throws IOException {
        ExcelFile excel = new ExcelFile(Files.newInputStream(path, StandardOpenOption.READ));
        log.info(excel.sheets().toString());
        Set<PersonalRating> result = new HashSet<>();
        for (String sheetName : excel.sheets()) {
            PlayType type = PlayType.of(sheetName);
            if (UNKNOWN == type) {
                if (!SHEETS_TO_SKIP.contains(sheetName)) {
                    log.warn("Unknown play type: {}, skip it", sheetName);
                }
                continue;
            }
            ResultTable resultTable = new ResultTable(
                excel.sheetByName(sheetName).iterator(),
                type
            );
            PersonalRating personalRating;
            while ((personalRating = resultTable.nextRecord()) != null) {
                result.add(personalRating);
            }
        }
        return result;
    }
}
