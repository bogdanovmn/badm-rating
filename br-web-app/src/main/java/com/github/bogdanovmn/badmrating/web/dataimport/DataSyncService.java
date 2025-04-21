package com.github.bogdanovmn.badmrating.web.dataimport;

import com.github.bogdanovmn.badmrating.core.ArchiveFile;
import com.github.bogdanovmn.badmrating.core.PersonalRating;
import com.github.bogdanovmn.badmrating.core.Player;
import com.github.bogdanovmn.badmrating.web.common.domain.PlayerRepository;
import com.github.bogdanovmn.badmrating.web.common.domain.PlayerSearchResult;
import com.github.bogdanovmn.badmrating.web.statistic.PlayersStatisticRepository;
import com.github.bogdanovmn.common.log.Timer;
import com.github.bogdanovmn.humanreadablevalues.MillisecondsValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
class DataSyncService {
    private final PlayerRepository playerRepository;
    private final PlayersStatisticRepository playersStatisticRepository;

    @Transactional(rollbackFor = Exception.class)
    public void processFile(Long importId, ArchiveFile archiveFile) throws IOException {
        Timer timer = Timer.start();
        Map<Player, List<PersonalRating>> ratingByPlayer = new HashMap<>();
        for (PersonalRating rating : archiveFile.content()) {
            if (ratingByPlayer.containsKey(rating.getPlayer())) {
                ratingByPlayer.get(rating.getPlayer()).add(rating);
            } else {
                ratingByPlayer.put(rating.getPlayer(), new ArrayList<>() {{ add(rating); }});
            }
        }
        int playersCount = 0;
        int ratesCount = 0;
        Map<UUID, List<PersonalRating>> playersRatingToSave = new HashMap<>();
        for (Player player : ratingByPlayer.keySet()) {
            UUID playerId = createOrUpdatePlayer(importId, player);
            playersRatingToSave.put(playerId, ratingByPlayer.get(player));
            playersCount++;
            ratesCount += ratingByPlayer.get(player).size();
        }
        if (ratesCount > 0) {
            playerRepository.addRatingsBulk(importId, playersRatingToSave);
        }
        log.info("Imported {} players and {} rates in {}", playersCount, ratesCount, new MillisecondsValue(timer.durationInMills()).fullString());
    }

    @Transactional(rollbackFor = Exception.class)
    public void playersTopCalculate(long importId) {
        Timer timer = Timer.start();
        int rows = playersStatisticRepository.updatePlayersActualTop(importId);
        log.info("Updated players actual top in {} ({} rows)", new MillisecondsValue(timer.durationInMills()).fullString(), rows);

        timer = Timer.start();
        rows = playersStatisticRepository.updatePlayersGlobalTop(importId);
        log.info("Updated players global top in {} ({} rows)", new MillisecondsValue(timer.durationInMills()).fullString(), rows);
    }

    private UUID createOrUpdatePlayer(Long importId, Player player) {
        Optional<PlayerSearchResult> persistedPlayer = playerRepository.findByName(player);
        if (persistedPlayer.isEmpty()) {
            return playerRepository.create(importId, player).getId();
        } else {
            PlayerSearchResult persisted = persistedPlayer.get();
            if (shouldUpdatePlayer(player, persisted.getDetails())) {
                log.debug("Updating player {} -> {}", persisted, player);
                playerRepository.savePreviousDetails(persisted);
                playerRepository.update(importId, persisted.getId(), player);
            }
            return persisted.getId();
        }
    }

    private boolean shouldUpdatePlayer(Player player, Player persisted) {
        return player.getRegion() != null && !player.getRegion().equals(persisted.getRegion())
            || player.getYear() != null && !player.getYear().equals(persisted.getYear())
            || player.getRank() != null && !player.getRank().equals(persisted.getRank());
    }
}
