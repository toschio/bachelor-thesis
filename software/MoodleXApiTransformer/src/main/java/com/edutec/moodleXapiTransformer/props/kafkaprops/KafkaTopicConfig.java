package com.edutec.moodleXapiTransformer.props.kafkaprops;

import com.edutec.moodleXapiTransformer.props.TopicsConfigs;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Kafka Admin Bean, creates topics as they are listed in the application.yml under resources.topics
 */
@Configuration
public class KafkaTopicConfig {


    private final List<String> host;
    private final TopicsConfigs topicsConfigs;
    private final String clientId;
    private final Log logger = LogFactory.getLog(getClass());

    public KafkaTopicConfig(@Value("${resources.kafka.broker}") final ArrayList<String> kafkaBroker,
                            @Value("${resources.kafka.clientid}") final String clientId,
                            TopicsConfigs topicsConfigs) {
        this.host = kafkaBroker;
        this.topicsConfigs = topicsConfigs;
        this.clientId = clientId;
    }

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, host);
        final KafkaAdmin kafkaAdmin = new KafkaAdmin(configs);
        try (final AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfig())) {
            createTopics(adminClient);
        }
        return kafkaAdmin;
    }

    /**
     * create all topics that are mentioned in yaml
     *
     * @param adminClient
     */
    private void createTopics(AdminClient adminClient) {
        CreateTopicsResult createTopics = adminClient.createTopics(topicsConfigs.getTopics().values().stream()
                .map(t -> new NewTopic(t.getTopicname(), t.getPartitionfactor(), t.getReplfactor()))
                .collect(Collectors.toList()));
        createTopics.values().forEach((s, voidKafkaFuture) -> {
            logger.info("Creating topic " + s);
            try {
                voidKafkaFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                if (e.getCause() instanceof TopicExistsException) {
                    logger.info("... topic already exists. Ignore Exception");
                    return;
                }
                logger.error(e);
            }
        });
    }

}
