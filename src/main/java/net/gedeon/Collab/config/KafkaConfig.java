package net.gedeon.Collab.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

/**
 * Configuration Kafka pour la collaboration temps réel
 *
 * Définit les topics et les configurations nécessaires pour
 * la publication d'événements de curseurs et d'opérations
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // Topics Kafka
    public static final String CURSOR_UPDATES_TOPIC = "cursor-updates";
    public static final String DOCUMENT_OPERATIONS_TOPIC = "document-operations";

    /**
     * Configuration du producer Kafka
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * KafkaTemplate pour envoyer des messages
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Topic pour les mises à jour de curseurs
     */
    @Bean
    public NewTopic cursorUpdatesTopic() {
        return TopicBuilder
                .name(CURSOR_UPDATES_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Topic pour les opérations sur les documents
     */
    @Bean
    public NewTopic documentOperationsTopic() {
        return TopicBuilder
                .name(DOCUMENT_OPERATIONS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
