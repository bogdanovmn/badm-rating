package com.github.bogdanovmn.badmrating.core.common;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimilarStrings {
    private final LevenshteinDistance levenshteinDistance;
    private final Collection<String> strings;
    private final int editDistance;

    public SimilarStrings(Collection<String> strings, int editDistance) {
        this.strings = strings;
        this.editDistance = editDistance;
        levenshteinDistance = new LevenshteinDistance(editDistance);
    }

    public String findClosest(String value) {
        return strings.stream()
            .map(str -> new AbstractMap.SimpleEntry<>(str, levenshteinDistance.apply(str, value)))
            .filter(entry -> entry.getValue() >= 0) // Игнорируем -1
            .min(Comparator.comparingInt(Map.Entry::getValue))
            .map(Map.Entry::getKey)
            .orElse(value);
    }

    public List<Set<String>> groups() {
        List<Set<String>> result = new ArrayList<>();
        List<String> names = new ArrayList<>(strings == null ? List.of() : strings);

        while (!names.isEmpty()) {
            String baseName = names.remove(0);
            Set<String> similar = new HashSet<>();
            Iterator<String> iterator = names.iterator();
            while (iterator.hasNext()) {
                String otherName = iterator.next();
                int distance = levenshteinDistance.apply(baseName, otherName);
                if (distance >= 0 && distance <= editDistance) {
                    similar.add(otherName);
                    iterator.remove();
                }
            }
            if (!similar.isEmpty()) {
                similar.add(baseName);
                result.add(similar);
            }
        }
        return result;
    }
}
