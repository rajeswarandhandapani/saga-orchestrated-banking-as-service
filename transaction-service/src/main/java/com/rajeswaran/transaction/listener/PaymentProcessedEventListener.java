package com.rajeswaran.transaction.listener;

import com.rajeswaran.common.events.PaymentProcessedEvent;
import com.rajeswaran.transaction.entity.Transaction;
import com.rajeswaran.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.function.Consumer;

@Component
public class PaymentProcessedEventListener {
    private static final Logger log = LoggerFactory.getLogger(PaymentProcessedEventListener.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Bean
    public Consumer<PaymentProcessedEvent> paymentProcessedEvent() {
        return event -> {
            log.info("Received PaymentProcessedEvent for paymentId={}, amount={}", event.getPaymentId(), event.getAmount());

            // Record DEBIT transaction for source account
            Transaction debitTransaction = new Transaction();
            debitTransaction.setAccountNumber(event.getSourceAccountNumber());
            debitTransaction.setAmount(event.getAmount());
            debitTransaction.setType("DEBIT");
            debitTransaction.setDescription("Payment processed for paymentId: " + event.getPaymentId());
            debitTransaction.setStatus("COMPLETED");
            debitTransaction.setReference(event.getPaymentId());
            debitTransaction.setTimestamp(LocalDateTime.now());
            transactionRepository.save(debitTransaction);

            // Record CREDIT transaction for destination account
            Transaction creditTransaction = new Transaction();
            creditTransaction.setAccountNumber(event.getDestinationAccountNumber());
            creditTransaction.setAmount(event.getAmount());
            creditTransaction.setType("CREDIT");
            creditTransaction.setDescription("Payment received for paymentId: " + event.getPaymentId());
            creditTransaction.setStatus("COMPLETED");
            creditTransaction.setReference(event.getPaymentId());
            creditTransaction.setTimestamp(LocalDateTime.now());
            transactionRepository.save(creditTransaction);
        };
    }
}

