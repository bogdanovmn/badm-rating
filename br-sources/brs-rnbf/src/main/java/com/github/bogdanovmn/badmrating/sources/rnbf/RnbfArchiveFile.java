package com.github.bogdanovmn.badmrating.sources.rnbf;

import com.github.bogdanovmn.badmrating.core.ArchiveFile;
import com.github.bogdanovmn.badmrating.core.PersonalRating;
import com.github.bogdanovmn.badmrating.core.PlayType;
import com.github.bogdanovmn.badmrating.core.Player;
import com.github.bogdanovmn.badmrating.core.excel.ExcelFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.OldExcelFormatException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.bogdanovmn.badmrating.core.PlayType.UNKNOWN;

@Slf4j
public class RnbfArchiveFile extends ArchiveFile {

    private static final Set<String> SHEETS_TO_SKIP = Set.of(
        "Соревнования",
        "Соревнование",
        "Соревнованя",
        "Пояснение",
        "Положение",
        "Поснения ",
        "Пояснениу",
        "Лист1",
        "Лист10",
        "Лист11",
        "Информация",
        "Начисление очков"
    );

    public RnbfArchiveFile(Path path) {
        super(path);
    }

    @Override
    public List<PersonalRating> content() {
        ExcelFile excel;
        try {
            excel = new ExcelFile(Files.newInputStream(path, StandardOpenOption.READ));
        } catch (IOException ex) {
            log.error("Open file error: {} ({})", ex.getMessage(), ex.getClass());
            return Collections.emptyList();
        } catch (OldExcelFormatException ex) {
            log.error("File format is not supported: {}", ex.getMessage());
            return Collections.emptyList();
        }
        log.debug(excel.sheets().toString());
        List<PersonalRating> result = new ArrayList<>();
        for (String sheetName : excel.sheets()) {
            PlayType type = PlayType.of(sheetName.trim());
            if (UNKNOWN == type) {
                if (!SHEETS_TO_SKIP.contains(sheetName)) {
                    log.warn("Unknown play type: {}, skip it", sheetName);
                }
                continue;
            }
            try {
                log.debug("Processing sheet '{}'", sheetName.trim());
                result.addAll(
                    new ResultTable(
                        excel.sheetByName(sheetName),
                        type
                    ).ratings()
                );
            } catch (Exception ex) {
                log.error("Parse file {} [sheet {}] error: {}", path.getFileName(), sheetName, ex.getMessage());
                throw ex;
            }
        }
        return new RatingList(result).corrected();
    }
}
