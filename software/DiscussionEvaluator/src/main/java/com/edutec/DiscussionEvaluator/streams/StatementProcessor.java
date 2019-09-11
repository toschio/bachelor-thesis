package com.edutec.DiscussionEvaluator.streams;

import com.edutec.DiscussionEvaluator.models.DiscussionPostStat;
import com.edutec.DiscussionEvaluator.models.NumberOfPostsPerDiscussion;
import com.edutec.DiscussionEvaluator.models.ReplyTimeOfPostsPerDiscussion;
import com.edutec.DiscussionEvaluator.models.VerbProviderAndFactory;
import com.edutec.DiscussionEvaluator.models.xapimodels.Statement;
import com.edutec.DiscussionEvaluator.models.xapimodels.StatementRef;
import com.edutec.DiscussionEvaluator.props.TopicsConfigs;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

/**
 * StatementProcessor evaluates instances of {@link com.edutec.DiscussionEvaluator.models.xapimodels.Statement} class
 * consumed from the topic provided in yml.
 */
@Configuration
@RequiredArgsConstructor
public class StatementProcessor {

    private final TopicsConfigs.TopicsProvider topicsProvider;
    private final TopicsConfigs.StoreNameProvider storeNameProvider;
    private final Log logger = LogFactory.getLog(StatementProcessor.class);

    @Bean
    public KStream<?, ?> counterPerUser(StreamsBuilder streamsBuilder) {
        // consumes the input stream
        KStream<String, Statement> statementKStream = streamsBuilder.stream(
                topicsProvider.getXapi_statement_discussion().getTopicname(),
                Consumed.with(Serdes.String(), new JsonSerde<>(Statement.class)));

        /**
         *  number of posts per user per diskussion: <actor id, number of posts>
         */
        KTable<String, NumberOfPostsPerDiscussion> numberOfPostsPerDiscussion =
                statementKStream
                        .selectKey(StandardStreamsOperations.KEY_BY_ACTOR)
                        .groupByKey(Serialized.with(Serdes.String(), new JsonSerde<>(Statement.class)))
                        .aggregate(
                                // initializer
                                NumberOfPostsPerDiscussion::new,
                                // adder
                                NumberOfPostsPerDiscussion::add,
                                Materialized.<String, NumberOfPostsPerDiscussion, KeyValueStore<Bytes, byte[]>>as(
                                        storeNameProvider.getNumber_of_posts_per_user())
                                        .withKeySerde(Serdes.String()).withValueSerde(
                                        new JsonSerde<>(NumberOfPostsPerDiscussion.class))
                        );


        /**
         * ReplyTimeOfPostPerDiscussion for each actor
         */
        KTable<String, ReplyTimeOfPostsPerDiscussion> replyTimeOfPostsPerDiscussionKTable =
                statementKStream
                        // pick only replies to other statements
                        .filter((key, value) -> {
                            if (value == null || value.getVerb() == null || value.getVerb().getId() == null) {
                                return false;
                            }
                            return value.getVerb().getId().equals(VerbProviderAndFactory.replied().getId());
                        })
                        // remove null values - i.e. tombstone records
                        .filter(StandardStreamsOperations.KEYANDVALUENOTNULL)
                        // select target object as key
                        .map((readOnlyKey, value) -> KeyValue.pair(((StatementRef) (value.getObject())).getId().toString(), value))
                        // join parent statement to get timedifference
                        .join(statementKStream
                                        .groupByKey(Serialized.with(Serdes.String(), new JsonSerde<>(Statement.class)))
                                        .reduce((value1, value2) -> value2),
                                (value1, value2) -> {
                                    // the discussion id in which the statements are happening
                                    String discussionId = value1.getContext().getContextActivities().getCategory().get(0).getId().toString();
                                    return new ReplyTimeOfPostsPerDiscussion.ReplyTimeOfPost(value1.getId().toString(),
                                            (value1.getTimestamp().getMillis() - value2.getTimestamp().getMillis()) / 1000,
                                            discussionId, value1.getActor().getName()
                                    );
                                }, Joined.with(Serdes.String(), new JsonSerde<>(Statement.class), new JsonSerde<>(Statement.class)))
                        // group by user
                        .groupBy((key, value) -> value.getActorId(), Serialized.with(Serdes.String(),
                                new JsonSerde<>(ReplyTimeOfPostsPerDiscussion.ReplyTimeOfPost.class)))
                        .aggregate(
                                ReplyTimeOfPostsPerDiscussion::new,
                                ReplyTimeOfPostsPerDiscussion::add,
                                Materialized.<String, ReplyTimeOfPostsPerDiscussion, KeyValueStore<Bytes, byte[]>>as(
                                        storeNameProvider.getReply_time_of_post_per_discussion())
                                        .withKeySerde(Serdes.String())
                                        .withValueSerde(new JsonSerde<>(ReplyTimeOfPostsPerDiscussion.class))
                        ).filter(StandardStreamsOperations.KEYANDVALUENOTNULL);

        /**
         * pack indicators together in a DiscussionPostStat object by joining
         */
        statementKStream
                .selectKey(StandardStreamsOperations.KEY_BY_ACTOR)
                .mapValues((readOnlyKey, value) -> new DiscussionPostStat())
                // the messages in statementKStream may arrive earlier than their counterpart entry in numberOfPostsPerDiscussion:
                // the numberOfPostsPerDiscussion therefore may lag behind
                .join(numberOfPostsPerDiscussion, DiscussionPostStat::apply, Joined.with(Serdes.String(),
                        new JsonSerde<>(DiscussionPostStat.class), new JsonSerde<>(NumberOfPostsPerDiscussion.class)))
                .leftJoin(replyTimeOfPostsPerDiscussionKTable, DiscussionPostStat::apply, Joined.with(Serdes.String(),
                        new JsonSerde<>(DiscussionPostStat.class), new JsonSerde<>(ReplyTimeOfPostsPerDiscussion.class)))
                .to(topicsProvider.getDiscussion_analytics().getTopicname(), Produced.with(Serdes.String(), new JsonSerde<>(DiscussionPostStat.class)));

        // alternatively :
//        numberOfPostsPerDiscussion.toStream()
//                .mapValues((readOnlyKey, value) -> {
//                    final DiscussionPostStat discussionPostStat = new DiscussionPostStat();
//                    discussionPostStat.apply(value);
//                    return discussionPostStat;
//                })
//                // the messages in statementKStream may arrive earlier than their counterpart entry in replyTimeOfPostsPerDiscussionKTable:
//                // the replyTimeOfPostsPerDiscussion therefore may lag behind
//                .leftJoin(replyTimeOfPostsPerDiscussionKTable, DiscussionPostStat::apply, Joined.with(Serdes.String(),
//                        new JsonSerde<>(DiscussionPostStat.class), new JsonSerde<>(ReplyTimeOfPostsPerDiscussion.class)))
//                .to(topicsProvider.getDiscussion_analytics().getTopicname(), Produced.with(Serdes.String(), new JsonSerde<>(DiscussionPostStat.class)));


        return statementKStream;
    }
}
