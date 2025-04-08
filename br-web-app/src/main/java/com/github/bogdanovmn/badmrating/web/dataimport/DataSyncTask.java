package com.github.bogdanovmn.badmrating.web.dataimport;

import com.github.bogdanovmn.badmrating.core.ArchiveFile;
import com.github.bogdanovmn.badmrating.core.LocalStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static com.github.bogdanovmn.badmrating.web.dataimport.ImportRepository.Status.FAILED;
import static com.github.bogdanovmn.badmrating.web.dataimport.ImportRepository.Status.SUCCESS;

@Slf4j
@Component
@RequiredArgsConstructor
class DataSyncTask implements ApplicationRunner {
    private final List<LocalStorage> localStorage;
    private final ImportRepository importRepository;
    private final DataSyncService dataSyncService;

    @Scheduled(cron = "0 0 0 * * *")
    void sync() throws IOException {
        for (LocalStorage storage : localStorage) {
            log.info("Syncing {}...", storage.sourceId());
            storage.update();
            LocalDate latestSuccessful = importRepository.latestSuccessful(storage.sourceId());
            List<ArchiveFile> files = storage.historyFrom(latestSuccessful);
            int precessed = 0;
            for (ArchiveFile archiveFile : files) {
                log.info("Processing file: {}", archiveFile);
                Long importId = importRepository.create(storage.sourceId(), archiveFile, storage.fileExternalUrl(archiveFile));
                try {
                    dataSyncService.processFile(importId, archiveFile);
                    importRepository.updateAsFinished(importId, SUCCESS);
                    precessed++;
                } catch (Exception e) {
                    log.error("Error processing file: {}", archiveFile, e);
                    importRepository.updateAsFinished(importId, FAILED);
                }
            }
            log.info("{} Sync done. Processed {} files. Errors: {}", storage.sourceId(), precessed, files.size() - precessed);
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        sync();
    }
}
