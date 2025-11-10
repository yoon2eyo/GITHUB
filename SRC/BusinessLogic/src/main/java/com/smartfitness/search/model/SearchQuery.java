package com.smartfitness.search.model;

/**
 * SearchQuery: Semantic query payload from clients.
 */
public class SearchQuery {
    private final String text;

    public SearchQuery(String text) {
        this.text = text;
    }

    public String getText() { return text; }
}

