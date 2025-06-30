package com.rajeswaran.common.model.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OpenAccountCommand {
    private String userId;
    private String username;
    private String email;
    private String fullName;
    private Set<String> roles;
}
