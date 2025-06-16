package com.rajeswaran.notification.listener;

import com.rajeswaran.common.events.AccountBalanceUpdatedEvent;
import com.rajeswaran.common.events.AccountOpenedEvent;
import com.rajeswaran.common.events.PaymentFailedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@Component
public class notificationEventListener {    
    
    @Bean
    public Consumer<AccountOpenedEvent> notificationEvent() {
        return event -> {
            if (event.getEventType() == com.rajeswaran.common.AppConstants.SagaEventType.ACCOUNT_OPENED) {
                log.info("Welcome {}, your new {} account (Account Number: {}) has been created with an initial balance of ${}.", event.getFullName(), event.getAccountType(), event.getAccountNumber(), event.getBalance());
            }
        };
    }

    @Bean
    public Consumer<AccountBalanceUpdatedEvent> accountBalanceUpdatedNotification() {
        return event -> {
            if (event.getEventType() == com.rajeswaran.common.AppConstants.SagaEventType.ACCOUNT_BALANCE_UPDATED) {
                log.info("Payment Notification: Dear {}, your payment of ${} has been successfully processed. Payment ID: {}", 
                        event.getUsername(), event.getAmount(), event.getPaymentId());
                log.info("Transfer Details: ${} transferred from account {} to account {}", 
                        event.getAmount(), event.getSourceAccountNumber(), event.getDestinationAccountNumber());
            }
        };
    }

    @Bean
    public Consumer<PaymentFailedEvent> paymentFailedNotification() {
        return event -> {
            if (event.getEventType() == com.rajeswaran.common.AppConstants.SagaEventType.PAYMENT_FAILED) {
                log.info("Payment Failed Notification: Dear {}, your payment (ID: {}) could not be processed. Reason: {}", 
                        event.getUsername(), event.getPaymentId(), event.getDetails());
            }
        };
    }
}

