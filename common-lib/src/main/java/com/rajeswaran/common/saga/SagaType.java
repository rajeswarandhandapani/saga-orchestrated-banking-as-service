package com.rajeswaran.common.saga;

/**
 * Enumeration of different types of sagas in the banking system.
 * Each saga type represents a distinct business workflow.
 */
public enum SagaType {
    /**
     * User onboarding workflow - creates user, opens account, sends welcome notification.
     */
    USER_ONBOARDING("user-onboarding"),
    
    /**
     * Money transfer workflow - validates accounts, transfers funds, records transaction.
     */
    MONEY_TRANSFER("money-transfer"),
    
    /**
     * Account closure workflow - validates balance, closes account, sends confirmation.
     */
    ACCOUNT_CLOSURE("account-closure"),
    
    /**
     * Payment processing workflow - validates payment, processes transaction, updates balances.
     */
    PAYMENT_PROCESSING("payment-processing"),
    
    /**
     * User profile update workflow - validates changes, updates profile, notifies user.
     */
    PROFILE_UPDATE("profile-update");
    
    private final String identifier;
    
    SagaType(String identifier) {
        this.identifier = identifier;
    }
    
    /**
     * Gets the string identifier for this saga type.
     * @return saga type identifier
     */
    public String getIdentifier() {
        return identifier;
    }
    
    /**
     * Finds a SagaType by its identifier.
     * @param identifier the identifier to search for
     * @return matching SagaType
     * @throws IllegalArgumentException if no matching type is found
     */
    public static SagaType fromIdentifier(String identifier) {
        for (SagaType type : values()) {
            if (type.identifier.equals(identifier)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown saga type identifier: " + identifier);
    }
    
    @Override
    public String toString() {
        return identifier;
    }
}
