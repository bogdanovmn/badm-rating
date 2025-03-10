package com.github.bogdanovmn.badmrating.web.dataimport;

import com.github.bogdanovmn.badmrating.core.ArchiveFile;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static com.github.bogdanovmn.badmrating.web.dataimport.ImportRepository.Status.IN_PROGRESS;

@Component
@RequiredArgsConstructor
class ImportRepository {
    private final JdbcTemplate jdbc;

    enum Status { SUCCESS, FAILED, IN_PROGRESS }

    LocalDate latestSuccessful() {
        return jdbc.queryForObject("SELECT MAX(file_date) FROM import", LocalDate.class);
    }

    Long create(String sourceId, ArchiveFile archiveFile) {
        return jdbc.queryForObject(
            "INSERT INTO import (source, file_date, started_at, status) VALUES (?, ?, ?, ?) RETURNING id",
                Long.class,
                sourceId,
                archiveFile.date(),
                LocalDate.now(),
                IN_PROGRESS.toString()
        );
    }

    void updateAsFinished(long importId, Status status) {
        jdbc.update("""
                UPDATE import SET
                    finished_at = ?,
                    status = ?
                WHERE id = ?
                """,
            LocalDate.now(),
            status.toString(),
            importId
        );
    }
}
