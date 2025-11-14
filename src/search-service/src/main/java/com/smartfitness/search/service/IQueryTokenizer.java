package com.smartfitness.search.service;

import java.util.List;

/**
 * Business Layer: Query Tokenizer Interface
 * Reference: 03_BranchContentServiceComponent.puml (IQueryTokenizer)
 */
public interface IQueryTokenizer {
    List<String> tokenize(String query);
}

