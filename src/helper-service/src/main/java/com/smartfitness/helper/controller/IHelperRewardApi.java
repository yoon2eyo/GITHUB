package com.smartfitness.helper.controller;

import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Interface Layer: Helper Reward API Interface
 * Reference: 04_HelperServiceComponent.puml (IHelperRewardApi)
 */
public interface IHelperRewardApi {
    ResponseEntity<Map<String, Object>> getRewardBalance(String helperId);
    ResponseEntity<Map<String, Object>> getRewardHistory(String helperId);
}

