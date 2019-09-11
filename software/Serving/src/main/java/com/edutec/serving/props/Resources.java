package com.edutec.serving.props;

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
public class Resources {
    private String stat_websocket;
    private String assessment_stat_websocket;
    private Map<String, Topic> topics;

    @Configuration
    @EnableConfigurationProperties
    @ConfigurationProperties("resources.topics")
    @Getter
    @Setter
    public static class TopicsProvider {
        private Topic discussion_analytics;
        private Topic assessment_analytics;
    }

    @Configuration
    @EnableConfigurationProperties
    @ConfigurationProperties("resources.stores")
    @Getter
    @Setter
    public static class StoreNameProvider {

        private String discussion_analytics_store;
        private String assessment_analytics_store;
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
