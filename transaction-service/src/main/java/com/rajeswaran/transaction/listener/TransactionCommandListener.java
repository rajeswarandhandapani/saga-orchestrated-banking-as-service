package com.rajeswaran.transaction.listener;

import com.rajeswaran.common.entity.Payment;
import com.rajeswaran.common.entity.Transaction;
import com.rajeswaran.common.saga.payment.commands.RecordTransactionCommand;
import com.rajeswaran.common.saga.payment.events.TransactionFailedEvent;
import com.rajeswaran.common.saga.payment.events.TransactionRecordedEvent;
import com.rajeswaran.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionCommandListener {

    private final StreamBridge streamBridge;
    private final TransactionRepository transactionRepository;

    @Bean
    public Consumer<Message<RecordTransactionCommand>> recordTransactionCommand() {
        return message -> {
            RecordTransactionCommand cmd = message.getPayload();
            Payment payment = cmd.getPayment();
            log.info("[Transaction] Received RecordTransactionCommand for saga {} and payment: {}", cmd.getSagaId(), payment);
            try {
                // Map RecordTransactionCommand to Transaction entity and persist
                Transaction transaction = new Transaction();
                transaction.setAccountNumber(payment.getSourceAccountNumber());
                transaction.setAmount(payment.getAmount());
                transaction.setType("PAYMENT");
                transaction.setDescription(payment.getDescription());
                transaction.setStatus("COMPLETED");
                transaction.setReference("Payment ID: " + payment.getId());
                transaction.setTimestamp(LocalDateTime.now());
                transaction.setUsername(payment.getCreatedBy());
                transaction.setBalance(payment.getSourceAccountBalance());
                // Optionally set username and balance if available in cmd
                transactionRepository.save(transaction);
                log.info("[Transaction] Persisted transaction: {}", transaction);

                // Also persist destination transaction
                Transaction destTransaction = new Transaction();
                destTransaction.setAccountNumber(payment.getDestinationAccountNumber());
                destTransaction.setAmount(payment.getAmount());
                destTransaction.setType("PAYMENT_RECEIVED");
                destTransaction.setDescription(payment.getDescription());
                destTransaction.setStatus("COMPLETED");
                destTransaction.setReference("Payment ID: " + payment.getId());
                destTransaction.setTimestamp(LocalDateTime.now());
                destTransaction.setUsername(payment.getDestinationAccountUserName());
                destTransaction.setBalance(payment.getDestinationAccountBalance());
                // Optionally set username and balance if available in cmd
                transactionRepository.save(destTransaction);
                log.info("[Transaction] Persisted destination transaction: {}", destTransaction);

                // On success, emit TransactionRecordedEvent
                TransactionRecordedEvent event = TransactionRecordedEvent.create(
                        cmd.getSagaId(),
                        payment
                );
                streamBridge.send("transactionRecordedEvent-out-0", event);
                log.info("[Transaction] Published TransactionRecordedEvent for saga {} and payment: {}", cmd.getSagaId(), payment.getId());
            } catch (Exception e) {
                log.error("[Transaction] Failed to record transaction for saga {} and payment {}: {}", cmd.getSagaId(), payment.getId(), e.getMessage(), e);
                // On failure, emit TransactionFailedEvent
                TransactionFailedEvent event = TransactionFailedEvent.create(
                        cmd.getSagaId(),
                        payment,
                        "Failed to record transaction: " + e.getMessage()
                );
                streamBridge.send("transactionFailedEvent-out-0", event);
            }
        };
    }
}
