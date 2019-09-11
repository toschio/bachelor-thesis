package com.edutec.AssessmentEvaluator.streams;

import com.edutec.AssessmentEvaluator.models.GesturePerUserAggregator;
import com.edutec.AssessmentEvaluator.models.GesturePerUserStats;
import com.edutec.AssessmentEvaluator.models.leapmotionmodels.LeapMotionFrame;
import com.edutec.AssessmentEvaluator.models.xapimodels.Statement;
import com.edutec.AssessmentEvaluator.props.TopicsConfigs;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.SessionStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

@Configuration
@RequiredArgsConstructor
public class AssessmentAnalytics {

    private final TopicsConfigs.TopicsProvider topicsProvider;
    private final TopicsConfigs.StoreNameProvider storeNameProvider;
    private final Log logger = LogFactory.getLog(AssessmentAnalytics.class);


    //
    @Bean
    public KStream<?, ?> leapFrame(StreamsBuilder streamsBuilder) {

        final KTable<String, Statement> userKeyedAssessmentKTable = streamsBuilder
                .stream(topicsProvider.getXapi_statement_assessment().getTopicname(), Consumed.with(Serdes.String(), new JsonSerde<>(Statement.class)))
                // distribute assessments by user
                .selectKey((key, value) -> value.getActor().getName())
                .groupByKey(Serialized.with(Serdes.String(), new JsonSerde<>(Statement.class)))
                .reduce((o, n) -> n);
//        , Materialized.<String, Statement, KeyValueStore<Bytes, byte[]>>as("theassessmenttable")
//                        .withKeySerde(Serdes.String())
//                        .withValueSerde(new JsonSerde<>(Statement.class)));

        // assume leap motion stream is keyed by user; String is userid
        final KStream<String, LeapMotionFrame> userKeyedLeapMotionKStream = streamsBuilder
                .stream(topicsProvider.getLeap_motion().getTopicname(),
                        Consumed.with(Serdes.String(), new JsonSerde<>(LeapMotionFrame.class)));


        userKeyedLeapMotionKStream
                // do aggregate per user over a session window - i.e. merge all that are not further from each other away than sessionwindow-time
                // in other words, aggregate over each continuum of messages
                .groupByKey(Serialized.with(Serdes.String(), new JsonSerde<>(LeapMotionFrame.class))) //  Serialized.with(Serdes.Long(), new JsonSerde<>( LeapMotionFrame.class)))
                .windowedBy(SessionWindows.with(42000))
                .aggregate(GesturePerUserAggregator::new, GesturePerUserAggregator.AGGREGATE, GesturePerUserAggregator.MERGE,
                        Materialized.<String, GesturePerUserAggregator, SessionStore<Bytes, byte[]>>as(storeNameProvider.getUser_keyed_session_windowed_leap_motion())
                                .withKeySerde(Serdes.String())
                                .withValueSerde(new JsonSerde<>(GesturePerUserAggregator.class)))
                .toStream((key, count) -> key.key())
                .filter((k, v) -> v != null)
                .join(userKeyedAssessmentKTable, GesturePerUserStats::with, Joined.with(Serdes.String(),
                        new JsonSerde<>(GesturePerUserAggregator.class), new JsonSerde<>(Statement.class)))
                .to(topicsProvider.getAssessment_analytics().getTopicname(), Produced.with(Serdes.String(),
                        new JsonSerde<>(GesturePerUserStats.class)));

        return userKeyedLeapMotionKStream;
    }
}
