package com.edutec.DiscussionEvaluator.props;

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
        private TopicsConfigs.Topic xapi_statement_discussion;
        private TopicsConfigs.Topic discussion_analytics;
    }

    @Configuration
    @EnableConfigurationProperties
    @ConfigurationProperties("resources.stores")
    @Getter
    @Setter
    public static class StoreNameProvider {
        private String number_of_posts_per_user;
        private String reply_time_of_post_per_discussion;
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
