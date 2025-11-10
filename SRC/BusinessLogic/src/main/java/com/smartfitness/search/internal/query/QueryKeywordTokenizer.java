package com.smartfitness.search.internal.query;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Lightweight tokenizer for user queries so the hot path can avoid remote LLM calls.
 */
public class QueryKeywordTokenizer {
    private static final Pattern NON_LETTER = Pattern.compile("[^\\p{IsAlphabetic}\\p{IsDigit}]+");
    private static final Set<String> STOP_WORDS = Set.of(
        "the", "is", "are", "and", "or", "of", "in", "to", "a", "an", "for", "with",
        "please", "near", "close", "place", "area"
    );

    public List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String normalized = Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFKC);
        String[] rawTokens = NON_LETTER.split(normalized);

        Set<String> deduped = Arrays.stream(rawTokens)
            .map(String::trim)
            .filter(token -> token.length() >= 2)
            .filter(token -> !STOP_WORDS.contains(token))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        return List.copyOf(deduped);
    }
}
