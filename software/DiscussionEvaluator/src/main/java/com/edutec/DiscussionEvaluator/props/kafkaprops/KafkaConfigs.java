package com.edutec.DiscussionEvaluator.props.kafkaprops;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kafka Consumer Configs, especially naming group id
 */
@Configuration
@EnableKafka
public class KafkaConfigs {


    private final List<String> host;
    private final String consumerGroupId;
    private Log logger = LogFactory.getLog(KafkaConfigs.class);

    public KafkaConfigs(@Value("${resources.kafka.broker}") final ArrayList<String> kafka,
                        @Value("${resources.kafka.consumergroupid}") final String consumerGroupId) {
        this.host = kafka;
        this.consumerGroupId = consumerGroupId;
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        logger.info("Kafka is on " + this.host);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.host);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        return props;
    }

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.host);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        return props;
    }

}
