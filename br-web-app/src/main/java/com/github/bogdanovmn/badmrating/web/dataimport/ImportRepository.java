package com.github.bogdanovmn.badmrating.web.dataimport;

import com.github.bogdanovmn.badmrating.core.ArchiveFile;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.github.bogdanovmn.badmrating.web.dataimport.ImportRepository.Status.IN_PROGRESS;

@Component
@RequiredArgsConstructor
class ImportRepository {
    private final JdbcTemplate jdbc;

    enum Status { SUCCESS, FAILED, IN_PROGRESS }

    LocalDate latestSuccessful(String source) {
        return jdbc.queryForObject(
            "SELECT MAX(file_date) FROM import WHERE source = ?",
            LocalDate.class,
            source
        );
    }

    Long create(String sourceId, ArchiveFile archiveFile, String url) {
        return jdbc.queryForObject(
            "INSERT INTO import (source, url, file_date, started_at, status) VALUES (?, ?, ?, ?, ?) RETURNING id",
                Long.class,
                sourceId,
                url,
                archiveFile.date(),
                LocalDateTime.now(),
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
            LocalDateTime.now(),
            status.toString(),
            importId
        );
    }
}
