package com.github.bogdanovmn.badmrating.web.dataimport;

import com.github.bogdanovmn.badmrating.core.ArchiveFile;
import com.github.bogdanovmn.badmrating.core.LocalStorage;
import com.github.bogdanovmn.badmrating.web.common.domain.Source;
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
            Source source = Source.valueOf(storage.sourceId());
            log.info("Syncing {}...", storage.sourceId());
            storage.update();
            LocalDate latestSuccessful = importRepository.latestSuccessful(source);
            List<ArchiveFile> files = storage.historyFrom(latestSuccessful);
            int processed = 0;
            for (ArchiveFile archiveFile : files) {
                log.info("Processing file: {}", archiveFile);
                Long importId = importRepository.create(source, archiveFile, storage.fileExternalUrl(archiveFile));
                try {
                    if (dataSyncService.processFile(importId, archiveFile) > 0) {
                        log.debug("Players top calculation started...");
                        dataSyncService.playersTopCalculate(importId);
                        importRepository.updateAsSuccessful(importId);
                    }
                    processed++;
                } catch (Exception e) {
                    log.error("Error processing file: {}", archiveFile, e);
                    importRepository.updateAsFailed(importId, e.getClass().getName());
                }
//                if (processed > 2) break;
            }
            log.info(
                "{} Sync done in {}. Processed {} files. Errors: {}",
                    storage.sourceId(),
                    new MillisecondsValue(timer.durationInMills()).fullString(),
                    processed,
                    files.size() - processed
            );
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        sync();
    }
}
