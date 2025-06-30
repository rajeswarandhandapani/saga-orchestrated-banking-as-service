package com.rajeswaran.common.model.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelNotificationCommand {
    private String referenceId; // The same referenceId used in SendNotificationCommand
}
