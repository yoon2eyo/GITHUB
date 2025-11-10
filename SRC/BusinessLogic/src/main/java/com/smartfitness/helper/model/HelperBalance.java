package com.smartfitness.helper.model;

/**
 * HelperBalance: Balance information for a helper account.
 */
public class HelperBalance {
    private final Long helperId;
    private final double amount;

    public HelperBalance(Long helperId, double amount) {
        this.helperId = helperId;
        this.amount = amount;
    }

    public Long getHelperId() {
        return helperId;
    }

    public double getAmount() {
        return amount;
    }
}
