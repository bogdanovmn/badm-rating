package com.github.bogdanovmn.badmrating.cli;

import com.github.bogdanovmn.badmrating.core.ArchiveFile;
import com.github.bogdanovmn.badmrating.core.LocalStorage;
import com.github.bogdanovmn.badmrating.core.PersonalRating;
import com.github.bogdanovmn.badmrating.core.Player;
import com.github.bogdanovmn.badmrating.core.RatingSource;
import com.github.bogdanovmn.badmrating.core.common.SimilarStrings;
import com.github.bogdanovmn.badmrating.sources.rnbf.RussianNationalBadmintonFederation;
import com.github.bogdanovmn.jaclin.CLI;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class App {
    private static final String OPT_STORAGE_DIR = "storage-dir";
    private static final String OPT_OVERVIEW = "overview";
    private static final String OPT_NAMES_CHECK = "names-check";
    private static final String OPT_APPLY = "apply";
    private static final String OPT_LATEST = "latest";
    private static final String OPT_SOURCE = "source";

    public static void main(String[] args) throws Exception {
        new CLI("import", "rating history import")
            .withRequiredOptions()
                .enumArg(OPT_SOURCE, "rating source", SourceType.class)
            .withOptions()
                .strArg(OPT_STORAGE_DIR, "local storage directory (for external result files caching)")
                .flag(OPT_OVERVIEW,      "show archive overview")
                .flag(OPT_NAMES_CHECK,   "show names check")
                .flag(OPT_APPLY,         "apply data")
                    .requires(OPT_STORAGE_DIR)
                .flag(OPT_LATEST,        "show latest results")
                    .requires(OPT_STORAGE_DIR)

            .withRestrictions()
                .atLeastOneShouldBeUsed(OPT_LATEST, OPT_OVERVIEW, OPT_APPLY, OPT_NAMES_CHECK)
                .mutualExclusions(OPT_LATEST, OPT_OVERVIEW, OPT_APPLY, OPT_NAMES_CHECK)

            .withEntryPoint(options -> {
                RatingSource source = ((SourceType) options.getEnum(OPT_SOURCE)).ratingSourceInstance();
                if (options.enabled(OPT_OVERVIEW)) {
                    source.archiveOverview().stream().collect(
                        Collectors.groupingBy(archive -> archive.getDate().getYear())
                    ).entrySet().stream()
                        .forEach(
                            entry -> System.out.printf("%s: %s files%n", entry.getKey(), entry.getValue().size())
                        );
                } else if (options.enabled(OPT_LATEST)) {
                    LocalStorage storage = new LocalStorage(options.get(OPT_STORAGE_DIR), source);
                    storage.update();
                    ArchiveFile archive = storage.latest().orElseThrow(() -> new NoSuchElementException("Can't find any archive"));
                    System.out.println("Latest archive: " + archive.date());
                    List<PersonalRating> ratings = archive.content();
                    System.out.println("Total parsed ratings: " + ratings.size());
                } else if (options.enabled(OPT_APPLY)) {
                    LocalStorage storage = new LocalStorage(options.get(OPT_STORAGE_DIR), source);
                    storage.update();
                    Map<Player, List<PersonalRating>> ratingByPlayer = new HashMap<>();
                    for (ArchiveFile archiveFile : storage.history()) {
                        log.info("Processing file: {}", archiveFile);
                        for (PersonalRating rating : archiveFile.content()) {
                            if (ratingByPlayer.containsKey(rating.getPlayer())) {
                                ratingByPlayer.get(rating.getPlayer()).add(rating);
                            } else {
                                ratingByPlayer.put(rating.getPlayer(), new ArrayList<>() {{ add(rating); }});
                            }
                        }
                    }
                    log.info("Total players: {}", ratingByPlayer.size());
                } else if (options.enabled(OPT_NAMES_CHECK)) {
                    LocalStorage storage = new LocalStorage(options.get(OPT_STORAGE_DIR), source);
                    storage.update();
                    Set<String> allNames = new HashSet<>();
                    for (ArchiveFile archiveFile : storage.history()) {
                        Set<String> names = archiveFile.content().stream()
                            .map(r -> r.getPlayer().getName())
                            .collect(Collectors.toSet());
                        log.info("Checked file: {}, names: {}", archiveFile, names.size());
                        allNames.addAll(names);
                    }
                    new SimilarStrings(allNames, 1).groups().forEach(
                        similar -> log.info("Similar names: {}", similar)
                    );
                }
            }).run(args);
    }
}
