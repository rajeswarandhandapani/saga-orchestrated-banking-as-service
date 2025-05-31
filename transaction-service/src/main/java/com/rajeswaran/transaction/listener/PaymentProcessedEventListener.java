package com.rajeswaran.transaction.listener;

import com.rajeswaran.common.events.PaymentProcessedEvent;
import com.rajeswaran.common.events.TransactionRecordedEvent;
import com.rajeswaran.common.util.SagaEventBuilderUtil;
import com.rajeswaran.transaction.entity.Transaction;
import com.rajeswaran.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.function.Consumer;

@Component
public class PaymentProcessedEventListener {
    private static final Logger log = LoggerFactory.getLogger(PaymentProcessedEventListener.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private StreamBridge streamBridge;

    @Bean
    public Consumer<PaymentProcessedEvent> paymentProcessedEvent() {
        return event -> {
            log.info("Received PaymentProcessedEvent for paymentId={}, amount={}", event.getPaymentId(), event.getAmount());

            // Record DEBIT transaction for source account - associated with the sender (current user)
            Transaction debitTransaction = new Transaction();
            debitTransaction.setAccountNumber(event.getSourceAccountNumber());
            debitTransaction.setAmount(event.getAmount());
            debitTransaction.setType("DEBIT");
            debitTransaction.setDescription("Payment processed for paymentId: " + event.getPaymentId());
            debitTransaction.setStatus("COMPLETED");
            debitTransaction.setReference(event.getPaymentId());
            debitTransaction.setTimestamp(LocalDateTime.now());
            debitTransaction.setUsername(event.getUsername()); // The initiator of the payment
            Transaction savedDebitTransaction = transactionRepository.save(debitTransaction);

            // Record CREDIT transaction for destination account - associated with the recipient
            Transaction creditTransaction = new Transaction();
            creditTransaction.setAccountNumber(event.getDestinationAccountNumber());
            creditTransaction.setUsername(event.getRecipientUsername());
            creditTransaction.setAmount(event.getAmount());
            creditTransaction.setType("CREDIT");
            creditTransaction.setDescription("Payment received for paymentId: " + event.getPaymentId());
            creditTransaction.setStatus("COMPLETED");
            creditTransaction.setReference(event.getPaymentId());
            creditTransaction.setTimestamp(LocalDateTime.now());

            Transaction savedCreditTransaction = transactionRepository.save(creditTransaction);

            // Publish TransactionRecordedEvent for audit
            TransactionRecordedEvent transactionRecordedEvent = TransactionRecordedEvent.builder()
                    .paymentId(event.getPaymentId())
                    .sourceAccountNumber(event.getSourceAccountNumber())
                    .destinationAccountNumber(event.getDestinationAccountNumber())
                    .amount(event.getAmount())
                    .debitTransactionId(String.valueOf(savedDebitTransaction.getId()))
                    .creditTransactionId(String.valueOf(savedCreditTransaction.getId()))
                    .username(event.getUsername())
                    .timestamp(SagaEventBuilderUtil.now())
                    .details("Transactions recorded for paymentId: " + event.getPaymentId())
                    .correlationId(event.getCorrelationId())
                    .serviceName(com.rajeswaran.common.AppConstants.ServiceName.AUDIT_SERVICE)
                    .eventType(com.rajeswaran.common.AppConstants.SagaEventType.TRANSACTION_RECORDED)
                    .build();
            
            streamBridge.send("auditEvent-out-0", transactionRecordedEvent);
            log.info("Published TransactionRecordedEvent for paymentId={}", event.getPaymentId());
        };
    }
}

