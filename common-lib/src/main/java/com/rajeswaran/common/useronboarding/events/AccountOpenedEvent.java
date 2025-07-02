package com.rajeswaran.common.useronboarding.events;

import com.rajeswaran.common.entity.Account;
import com.rajeswaran.common.entity.User;
import com.rajeswaran.common.event.BaseEvent;
import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * Event indicating an account was successfully opened.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AccountOpenedEvent extends BaseEvent {

    Account account;

    User user;

    public static AccountOpenedEvent create(SagaId sagaId, String correlationId, Account account, User user) {
        return AccountOpenedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .sagaId(sagaId)
            .correlationId(correlationId)
            .timestamp(Instant.now())
            .success(true)
            .errorMessage(null)
            .account(account)
            .user(user)
            .build();
    }
}
