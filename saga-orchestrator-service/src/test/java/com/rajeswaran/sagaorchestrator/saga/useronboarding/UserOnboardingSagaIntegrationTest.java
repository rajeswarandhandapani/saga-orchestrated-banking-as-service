package com.rajeswaran.sagaorchestrator.saga.useronboarding;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@EmbeddedKafka(partitions = 1, topics = {
        "createUserCommand-out-0",
        "accountOpenCommand-out-0",
        "sendNotificationCommand-out-0",
        "deleteUserCommand-out-0"
})
@ActiveProfiles("test")
class UserOnboardingSagaIntegrationTest {
    @Autowired
    private UserOnboardingSaga userOnboardingSaga;

    @Autowired
    private org.springframework.kafka.core.ConsumerFactory<String, Object> consumerFactory;

    @Test
    void whenStartSagaFlow_thenCreateUserCommandIsProduced() {
        // Arrange: create a User object
        var user = com.rajeswaran.common.entity.User.builder()
                .userId(123L)
                .username("testuser")
                .fullName("Test User")
                .email("testuser@example.com")
                .build();
        Long sagaId = 1L;

        // Act: start the saga
        userOnboardingSaga.startSagaFlow(sagaId, user);

        // Assert: consume the CreateUserCommand from Kafka
        var consumer = consumerFactory.createConsumer();
        org.springframework.kafka.test.utils.KafkaTestUtils.getRecords(
                consumer, java.time.Duration.ofSeconds(5)); // trigger assignment
        consumer.subscribe(java.util.List.of("createUserCommand-out-0"));
        var records = org.springframework.kafka.test.utils.KafkaTestUtils.getRecords(
                consumer, java.time.Duration.ofSeconds(5));
        boolean found = false;
        for (var record : records) {
            if (record.value() != null && record.topic().equals("createUserCommand-out-0")) {
                found = true;
                break;
            }
        }
        consumer.close();
        org.junit.jupiter.api.Assertions.assertTrue(found, "CreateUserCommand should be produced to Kafka");
    }
}
