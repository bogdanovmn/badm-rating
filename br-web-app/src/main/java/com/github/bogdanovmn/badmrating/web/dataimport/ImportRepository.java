package com.github.bogdanovmn.badmrating.web.dataimport;

import com.github.bogdanovmn.badmrating.core.ArchiveFile;
import com.github.bogdanovmn.badmrating.web.common.domain.Source;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.github.bogdanovmn.badmrating.web.dataimport.ImportRepository.Status.FAILED;
import static com.github.bogdanovmn.badmrating.web.dataimport.ImportRepository.Status.IN_PROGRESS;
import static com.github.bogdanovmn.badmrating.web.dataimport.ImportRepository.Status.SUCCESS;

@Component
@RequiredArgsConstructor
class ImportRepository {
    private final JdbcTemplate jdbc;

    enum Status { SUCCESS, FAILED, IN_PROGRESS }

    LocalDate latestSuccessful(Source source) {
        return jdbc.queryForObject(
            "SELECT MAX(file_date) FROM import WHERE source_id = ?",
            LocalDate.class,
            source.getId()
        );
    }

    Long create(Source source, ArchiveFile archiveFile, String url) {
        return jdbc.queryForObject(
            "INSERT INTO import (source_id, url, file_date, started_at, status) VALUES (?, ?, ?, ?, ?) RETURNING id",
                Long.class,
                source.getId(),
                url,
                archiveFile.date(),
                LocalDateTime.now(),
                IN_PROGRESS.toString()
        );
    }

    void updateAsFailed(long importId, String message) {
        updateAsFinished(importId, FAILED, message);
    }

    void updateAsSuccessful(long importId) {
        updateAsFinished(importId, SUCCESS, null);
    }

    void updateAsFinished(long importId, Status status, String message) {
        jdbc.update("""
                UPDATE import SET
                    finished_at = ?,
                    status = ?,
                    details = ?
                WHERE id = ?
                """,
            LocalDateTime.now(),
            status.toString(),
            message,
            importId
        );
    }
}
