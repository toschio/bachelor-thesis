package com.edutec.moodleXapiTransformer.props;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("resources")
@Getter
@Setter
public class TopicsConfigs {

    private Map<String, TopicsConfigs.Topic> topics;

    @Configuration
    @EnableConfigurationProperties
    @ConfigurationProperties("resources.topics")
    @Getter
    @Setter
    public static class TopicsProvider {
        private TopicsConfigs.Topic moodle_forum_posts_source;
        private TopicsConfigs.Topic moodle_forum_source;
        private TopicsConfigs.Topic moodle_user_source;

        private TopicsConfigs.Topic moodle_quiz;
        private TopicsConfigs.Topic moodle_quiz_attempts;
        private TopicsConfigs.Topic moodle_quiz_attempts_source_unpacked_keyed_by_user;
        private TopicsConfigs.Topic moodle_quiz_source_unpacked;

        private TopicsConfigs.Topic moodle_user_source_unpacked;

        private TopicsConfigs.Topic xapi_statement_discussion;
        private TopicsConfigs.Topic xapi_statement_assessment;

        private TopicsConfigs.Topic db_schema_history; // for connect debezium
    }

    @Configuration
    @EnableConfigurationProperties
    @ConfigurationProperties("resources.stores")
    @Getter
    @Setter
    public static class StoreNameProvider {
        private String mdl_user_source_unpacked_store;
        private String mdl_forum_source_unpacked_store;
    }

    @Data
    public static class Topic {

        private String topicname;
        private short replfactor;
        private Integer partitionfactor;

        public static Topic of(String topicname) {
            final Topic topic = new Topic();
            topic.setTopicname(topicname);
            return topic;
        }
    }
}
