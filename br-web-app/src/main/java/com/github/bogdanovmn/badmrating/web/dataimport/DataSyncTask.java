package com.github.bogdanovmn.badmrating.web.dataimport;

import com.github.bogdanovmn.badmrating.core.ArchiveFile;
import com.github.bogdanovmn.badmrating.core.LocalStorage;
import com.github.bogdanovmn.common.log.Timer;
import com.github.bogdanovmn.humanreadablevalues.MillisecondsValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

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
            Timer timer = Timer.start();
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
                    log.debug("Players top calculation started...");
                    dataSyncService.playersTopCalculate(importId);
                    importRepository.updateAsSuccessful(importId);
                    precessed++;
                } catch (Exception e) {
                    log.error("Error processing file: {}", archiveFile, e);
                    importRepository.updateAsFailed(importId, e.getClass().getName());
                }
            }
            log.info(
                "{} Sync done in {}. Processed {} files. Errors: {}",
                    storage.sourceId(),
                    new MillisecondsValue(timer.durationInMills()).fullString(),
                    precessed,
                    files.size() - precessed
            );
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        sync();
    }
}
