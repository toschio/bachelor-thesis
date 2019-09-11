package com.edutec.serving.streams;


import com.edutec.serving.models.DiscussionPostStatData;
import com.edutec.serving.models.dtos.DiscussionPostStatDataDto;
import com.edutec.serving.props.Resources;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Configuration
@RequiredArgsConstructor
public class DiscussionEvaluationSink {


    private final SimpMessagingTemplate template;
    private final Resources.TopicsProvider topicsProvider;
    private final Resources.StoreNameProvider storeNameProvider;
    private final Resources resources;

    private Log logger = LogFactory.getLog(DiscussionEvaluationSink.class);

    @Bean
    public KTable<String, DiscussionPostStatData> discussionEvaluatorSink(StreamsBuilder builder) {
        return builder
                .stream(topicsProvider.getDiscussion_analytics().getTopicname(),
                        Consumed.with(Serdes.String(), new JsonSerde<>(DiscussionPostStatData.class)))
                // publish to websocket
                .peek((key, value) -> {
                    try {
                        DiscussionPostStatDataDto dto = DiscussionPostStatDataDto.of(key, value);
                        template.convertAndSend(resources.getStat_websocket(), new ObjectMapper().writeValueAsString(dto));
                    } catch (MessagingException | JsonProcessingException me) {
                        logger.error(me);
                    }
                })
                .groupByKey()
                // store
                .reduce((o, n) -> n, Materialized.<String, DiscussionPostStatData, KeyValueStore<Bytes, byte[]>>as(
                        storeNameProvider.getDiscussion_analytics_store())
                        .withKeySerde(Serdes.String())
                        .withValueSerde(new JsonSerde<>(DiscussionPostStatData.class)));


    }
}
