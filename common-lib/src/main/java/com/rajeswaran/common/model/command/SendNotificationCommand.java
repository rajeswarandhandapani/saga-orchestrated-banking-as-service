package com.rajeswaran.common.model.command;

import com.rajeswaran.common.model.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationCommand {
    private UserDTO user;
    private String message;
    private String notificationType; // e.g., "ACCOUNT_OPENED"
    private String referenceId; // e.g., account number or user id
}
