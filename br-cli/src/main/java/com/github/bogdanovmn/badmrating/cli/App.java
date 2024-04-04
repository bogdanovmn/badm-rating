package com.github.bogdanovmn.badmrating.cli;

import com.github.bogdanovmn.badmrating.sources.rnbf.RussianNationalBadmintonFederation;
import com.github.bogdanovmn.jaclin.CLI;

public class App {
    public static void main(String[] args) throws Exception {
        new CLI("import", "rating history import")
            .withEntryPoint(options -> {
                new RussianNationalBadmintonFederation()
                    .archiveOverview()
                    .byYear()
                        .entrySet().stream()
                            .forEach(entry -> System.out.printf("%s: %s files%n", entry.getKey(), entry.getValue().size()));
            })
            .run(args);
    }
}
