package com.smartfitness.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business Layer: Simple Keyword Tokenizer
 * Component: SimpleKeywordTokenizer
 * 
 * UC-09: Real-time Branch Search (Hot Path)
 * 
 * Simple, FAST keyword extraction (NO LLM)
 * - Split by whitespace
 * - Remove stopwords
 * - Ensure < 3초 response time
 * 
 * DD-06, DD-09: Hot Path must be FAST (NO external LLM call)
 * 
 * Reference: 03_BranchContentServiceComponent.puml
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SimpleKeywordTokenizer implements IQueryTokenizer {
    
    private final ISearchEngineClient searchEngineClient;
    
    private static final List<String> STOPWORDS = List.of("the", "a", "an", "in", "on", "at", "to", "for");
    
    @Override
    public List<String> tokenize(String query) {
        log.debug("Tokenizing query: '{}'", query);
        
        // 1. Simple whitespace split
        List<String> tokens = Arrays.stream(query.toLowerCase().split("\\s+"))
                .filter(token -> !STOPWORDS.contains(token))
                .filter(token -> token.length() > 1)
                .collect(Collectors.toList());
        
        log.debug("Tokens extracted: {}", tokens);
        
        // 2. Query SearchEngine (local, fast)
        // searchEngineClient.query(tokens);
        
        return tokens;
    }
}

