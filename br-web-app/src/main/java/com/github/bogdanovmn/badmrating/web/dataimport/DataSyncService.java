package com.github.bogdanovmn.badmrating.web.dataimport;

import com.github.bogdanovmn.badmrating.core.ArchiveFile;
import com.github.bogdanovmn.badmrating.core.PersonalRating;
import com.github.bogdanovmn.badmrating.core.Player;
import com.github.bogdanovmn.badmrating.web.common.domain.PlayerRepository;
import com.github.bogdanovmn.badmrating.web.common.domain.PlayerSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class DataSyncService {
    private final PlayerRepository playerRepository;

    @Transactional
    public void processFile(Long importId, ArchiveFile archiveFile) throws IOException {
        Map<Player, List<PersonalRating>> ratingByPlayer = new HashMap<>();
        for (PersonalRating rating : archiveFile.content()) {
            if (ratingByPlayer.containsKey(rating.getPlayer())) {
                ratingByPlayer.get(rating.getPlayer()).add(rating);
            } else {
                ratingByPlayer.put(rating.getPlayer(), new ArrayList<>() {{ add(rating); }});
            }
        }
        for (Player player : ratingByPlayer.keySet()) {
            UUID playerId = createOrUpdatePlayer(player);
            playerRepository.addRating(importId, playerId, archiveFile.date(), ratingByPlayer.get(player));
        }
    }

    private UUID createOrUpdatePlayer(Player player) {
        PlayerSearchResult persistedPlayer = playerRepository.findByNameAndYear(player.getName(), player.getYear());
        if (persistedPlayer == null) {
            persistedPlayer = playerRepository.create(player);
        } else if (!persistedPlayer.getDetails().equals(player)) {
            playerRepository.update(persistedPlayer.getId(), player);
        }
        return persistedPlayer.getId();
    }
}
