package com.rajeswaran.transaction.listener;

import com.rajeswaran.common.saga.payment.commands.RecordTransactionCommand;
import com.rajeswaran.common.saga.payment.events.TransactionRecordedEvent;
import com.rajeswaran.common.saga.payment.events.TransactionFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionCommandListener {

    private final StreamBridge streamBridge;

    @Bean
    public Consumer<Message<RecordTransactionCommand>> recordTransactionCommand() {
        return message -> {
            RecordTransactionCommand cmd = message.getPayload();
            log.info("[Transaction] Received RecordTransactionCommand for saga {} and payment: {}", cmd.getSagaId(), cmd.getPaymentId());
            try {
                // Simulate transaction recording (replace with real persistence logic)
                log.info("[Transaction] Recording transaction: {} -> {} amount: {}", cmd.getSourceAccountNumber(), cmd.getDestinationAccountNumber(), cmd.getAmount());
                // On success, emit TransactionRecordedEvent
                TransactionRecordedEvent event = TransactionRecordedEvent.create(
                        cmd.getSagaId(),
                        cmd.getCorrelationId(),
                        cmd.getPaymentId(),
                        cmd.getSourceAccountNumber(),
                        cmd.getDestinationAccountNumber(),
                        cmd.getAmount(),
                        cmd.getDescription(),
                        "PAYMENT"
                );
                streamBridge.send("transactionRecordedEvent-out-0", event);
                log.info("[Transaction] Published TransactionRecordedEvent for saga {} and payment: {}", cmd.getSagaId(), cmd.getPaymentId());
            } catch (Exception e) {
                log.error("[Transaction] Failed to record transaction for saga {} and payment {}: {}", cmd.getSagaId(), cmd.getPaymentId(), e.getMessage(), e);
                // On failure, emit TransactionFailedEvent
                TransactionFailedEvent event = TransactionFailedEvent.create(
                        cmd.getSagaId(),
                        cmd.getCorrelationId(),
                        cmd.getPaymentId(),
                        cmd.getSourceAccountNumber(),
                        cmd.getDestinationAccountNumber(),
                        cmd.getAmount(),
                        "Failed to record transaction: " + e.getMessage(),
                        "PAYMENT"
                );
                streamBridge.send("transactionFailedEvent-out-0", event);
            }
        };
    }
}
