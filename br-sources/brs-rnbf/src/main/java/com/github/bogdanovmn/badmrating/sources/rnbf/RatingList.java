package com.github.bogdanovmn.badmrating.sources.rnbf;

import com.github.bogdanovmn.badmrating.core.PersonalRating;
import com.github.bogdanovmn.badmrating.core.PlayType;
import com.github.bogdanovmn.badmrating.core.Player;
import com.github.bogdanovmn.badmrating.core.PlayerRank;
import com.github.bogdanovmn.badmrating.core.common.SimilarStrings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class RatingList {
    private final List<PersonalRating> ratings;

    public List<PersonalRating> corrected() {
        // Собираем рейтинги по именам
        Map<String, List<PersonalRating>> ratingsByName = ratings.stream()
            .collect(Collectors.groupingBy(
                rating -> rating.getPlayer().getName(),
                Collectors.toList()
            ));

        // Исправляем опечатки в именах
        for (Set<String> similar : new SimilarStrings(ratingsByName.keySet(), 1).groups()) {
            fixTypo(similar, ratingsByName);
        }
        return ratingsByName.values().stream()
            .flatMap(List::stream)
            .toList();

        // Нормализуем игроков по году, региону и рангу
//        Map<String, Player> normalizedPlayers = ratingsByName.entrySet().stream()
//            .collect(Collectors.toMap(
//                Map.Entry::getKey,
//                entry -> normalizePlayer(entry.getValue())
//            ));
//
//        return ratingsByName.entrySet().stream()
//            .flatMap(e -> {
//                String originalName = e.getKey();
//                List<PersonalRating> originalRatings = e.getValue();
//
//                return originalRatings.stream()
//                    .map(rating -> PersonalRating.builder()
//                        .player(normalizedPlayers.get(originalName))
//                        .type(rating.getType())
//                        .value(rating.getValue())
//                        .build()
//                    );
//            })
//            .collect(Collectors.toList());
    }

    private void fixTypo(Set<String> similar, Map<String, List<PersonalRating>> ratingsByName) {
        log.info("Try to fix typo: {}", similar);

        Set<String> filteredSimilar = similar.stream()
            .filter(name -> ratingsByName.get(name).stream().map(PersonalRating::getType).distinct().count() < 3)
            .collect(Collectors.toSet());
        if (filteredSimilar.size() < 2) {
            log.info(
                "Skip too small typo group: {}. Filtered: {}",
                    filteredSimilar,
                    similar.stream().filter(
                        name -> ratingsByName.get(name).stream().map(PersonalRating::getType).distinct().count() >= 3
                    ).map(name -> String.format("%s %s", name, ratingsByName.get(name).stream().map(PersonalRating::getType).collect(Collectors.toSet())))
                        .collect(Collectors.joining(", "))
            );
            return;
        }

        Iterator<String> iterator = filteredSimilar.iterator();
        String baseName = iterator.next();
        List<PersonalRating> baseRatings = ratingsByName.get(baseName);
        Set<PlayType> basePlayType = baseRatings.stream().map(PersonalRating::getType).collect(Collectors.toSet());

        while (iterator.hasNext()) {
            String typo = iterator.next();
            List<PersonalRating> typoRatings = ratingsByName.get(typo);
            Set<PlayType> typoPlayType = typoRatings.stream().map(PersonalRating::getType).collect(Collectors.toSet());

            if (basePlayType.stream().anyMatch(typoPlayType::contains)) {
                log.info(
                    "Can't fix typo (have common play type): {} ({}) -> {} ({})",
                        typo, typoPlayType, baseName, basePlayType
                );
            } else {
                Player basePlayer = baseRatings.get(0).getPlayer();//normalizePlayer(baseRatings);
                Player typoPlayer = typoRatings.get(0).getPlayer();//normalizePlayer(typoRatings);
                if (playersLooksEqual(typoPlayer, basePlayer)) {
                    log.info("Looks like the same person: {} & {}. Will fix typo: {} -> {}", typoPlayer, basePlayer, typo, baseName);
                    List<PersonalRating> fixedRatings = changedRatingsWithTypo(typoRatings, basePlayer);
                    baseRatings.addAll(fixedRatings);
                    basePlayType.addAll(typoPlayType);
                    ratingsByName.remove(typo);
                } else {
                    log.info("Can't fix typo (possibly different persons): {} & {}", typoPlayer, basePlayer);
                }
            }
        }
    }

    private boolean playersLooksEqual(Player player1, Player player2) {
        return Objects.equals(player1.getYear(), player2.getYear())
            && Objects.equals(player1.getRegion(), player2.getRegion())
            && Objects.equals(player1.getRank(), player2.getRank());
    }

    private List<PersonalRating> changedRatingsWithTypo(List<PersonalRating> typoRatings, Player basePlayer) {
        return typoRatings.stream()
            .map(rating -> PersonalRating.builder()
                .player(basePlayer)
                .type(rating.getType())
                .value(rating.getValue())
                .build()
            ).toList();
    }

    private Player normalizePlayer(List<PersonalRating> ratingList) {
        Map<PlayType, Long> typesCount = ratingList.stream()
            .map(PersonalRating::getType)
            .collect(Collectors.groupingBy(
                t -> t,
                Collectors.counting()
            ));
        if (typesCount.values().stream().anyMatch(count -> count > 1)) {
            log.warn(
                "Can't normalize player '{}': they have the same play types {}",
                ratingList.get(0).getPlayer().getName(),
                typesCount
            );
            return null;
        }

        Player firstPlayer = ratingList.get(0).getPlayer();

        // Нормализация года: выбираем первый непустой
        Integer year = ratingList.stream()
            .map(PersonalRating::getPlayer)
            .map(Player::getYear)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);

        // Нормализация региона: выбираем первый непустой
        String region = ratingList.stream()
            .map(PersonalRating::getPlayer)
            .map(Player::getRegion)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);

        // Нормализация ранга: выбираем последний непустой, кроме NO_RANK
        PlayerRank rank = ratingList.stream()
            .map(PersonalRating::getPlayer)
            .map(Player::getRank)
            .filter(Objects::nonNull)
            .filter(r -> r != PlayerRank.NO_RANK)
            .reduce((first, second) -> second)
            .orElse(PlayerRank.NO_RANK);

        return Player.builder()
            .name(firstPlayer.getName())
            .year(year)
            .region(region)
            .rank(rank)
        .build();
    }
}
