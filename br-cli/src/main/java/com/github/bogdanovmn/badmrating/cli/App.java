package com.github.bogdanovmn.badmrating.cli;

import com.github.bogdanovmn.badmrating.core.ArchiveFile;
import com.github.bogdanovmn.badmrating.core.LocalStorage;
import com.github.bogdanovmn.badmrating.core.PersonalRating;
import com.github.bogdanovmn.badmrating.core.RatingSource;
import com.github.bogdanovmn.badmrating.sources.rnbf.RussianNationalBadmintonFederation;
import com.github.bogdanovmn.jaclin.CLI;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

public class App {
    private static final String OPT_STORAGE_DIR = "storage-dir";
    private static final String OPT_OVERVIEW = "overview";
    private static final String OPT_LATEST = "latest";

    public static void main(String[] args) throws Exception {
        new CLI("import", "rating history import")
            .withOptions()
                .flag(OPT_OVERVIEW,      "show archive overview")
                .flag(OPT_LATEST,        "show latest results")
                    .requires(OPT_STORAGE_DIR)
                .strArg(OPT_STORAGE_DIR, "local storage directory (for external result files caching)")

            .withRestrictions()
                .atLeastOneShouldBeUsed(OPT_LATEST, OPT_OVERVIEW)
                .mutualExclusions(OPT_LATEST, OPT_OVERVIEW)

            .withEntryPoint(options -> {
                RatingSource source = new RussianNationalBadmintonFederation();
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
                    Set<PersonalRating> ratings = archive.content();
                    System.out.println("Total parsed ratings: " + ratings.size());
                }
            })
            .run(args);
    }
}
