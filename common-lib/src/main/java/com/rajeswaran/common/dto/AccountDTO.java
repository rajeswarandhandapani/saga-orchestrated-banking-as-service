package com.rajeswaran.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * Data Transfer Object for Account information shared across services.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO implements Serializable {
    private String accountId;
    private String accountNumber;
    private String accountType;
    private String userId;
    private double balance;
    private String status;
}
