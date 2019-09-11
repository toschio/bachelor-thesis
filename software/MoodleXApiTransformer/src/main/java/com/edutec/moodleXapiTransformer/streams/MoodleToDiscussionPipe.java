package com.edutec.moodleXapiTransformer.streams;

import com.edutec.moodleXapiTransformer.CdcUnpackService;
import com.edutec.moodleXapiTransformer.XApiFactory;
import com.edutec.moodleXapiTransformer.XApiTopicNameExtractor;
import com.edutec.moodleXapiTransformer.models.Moodle;
import com.edutec.moodleXapiTransformer.models.StatementDataWrapper;
import com.edutec.moodleXapiTransformer.props.TopicsConfigs;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

/**
 * consumes userdata, forumdata and forumpostdata from moodle and transforms them into an xapi statement that
 * is published in the topic that is extracted from the statements'
 * verbs by {@link com.edutec.moodleXapiTransformer.XApiTopicNameExtractor}
 */
@Configuration
@RequiredArgsConstructor
public class MoodleToDiscussionPipe {

    private final Log logger = LogFactory.getLog(getClass());
    // service to unpack JSON representing cdc values
    private final CdcUnpackService unpackService;
    // service to construct a statement
    private final XApiFactory xApiFactory;
    // service that provides topic names read from thy yml
    private final TopicsConfigs.TopicsProvider topicNameProvider;
    // extracts topics to publish to according to xapi statement's verbs
    private final XApiTopicNameExtractor xApiTopicNameExtractor;

    /**
     * transforms input streams into an xapi statement
     *
     * @param streamsBuilder   constructs kafka topolgy
     * @param keyedMoodleUser  a bean that represents a KTable of MdlUser
     * @param keyedMoodleForum a bean that represents a KTable of MdlForum
     * @return not of interest
     */
    @Bean
    public KStream<?, ?> constructDisussionStatement(StreamsBuilder streamsBuilder,
                                   KTable<Long, Moodle.MdlUser> keyedMoodleUser,
                                   KTable<Long, Moodle.MdlForum> keyedMoodleForum) {      // Moodle User stream via bean && Moodle Forum stream via bean
        //
        // input streams
        //
        // Moodle Forum Post Cdc wrapper stream
        KStream<?, JsonNode> moodleForumPostAsJsonStream = streamsBuilder.stream(
                topicNameProvider.getMoodle_forum_posts_source().getTopicname());

        // data operation
        // concerning Moodle Forum Posts and publishing of Statements
        moodleForumPostAsJsonStream
                //unpack mdlForumPost and build StatementDataWrapper, which is a wrapper for objects to be joined
                .mapValues(value -> {
                        //JSON to Java object - create an Moodle Forum Post Object
                        logger.info("unpacking forum pst");
                        return unpackService.unpack(value, Moodle.MdlForumPost.class); }
                )
                .mapValues(mdlForumPost -> {
                    logger.info("building wrapper");
                    return StatementDataWrapper.builder().mdlForumPost(mdlForumPost).build();
                })
                //
                // join with Moodle User and Moodle Forum streams to fill missing data in wrapper
                //
                // since we expect the user to be more statical than the forum post, we make a leftjoin - essentially a table lookup
                // join with Moodle User
                .selectKey((k, v) -> v.getMdlForumPost().getUserid())
                // this join means, that results are only computed if KStream records are processed - only those new records will
                // be updated with new user records
                .join(keyedMoodleUser, StatementDataWrapper::setMdlUser,
                        Joined.with(Serdes.Long(), new JsonSerde<>(StatementDataWrapper.class), new JsonSerde<>(Moodle.MdlUser.class)))
                // join with Moodle Forum
                .selectKey((k, v) -> v.getMdlForumPost().getDiscussion())
                .join(keyedMoodleForum, StatementDataWrapper::setMdlForum,
                        Joined.with(Serdes.Long(), new JsonSerde<>(StatementDataWrapper.class), new JsonSerde<>(Moodle.MdlForum.class)))
                //
                // build the XApiStatement
                //
                .map((k, v) -> xApiFactory.constructStatement(v))
                // publish the statement
                .filter(StandardStreamsOperations.KEYANDVALUENOTNULL)
                .to(xApiTopicNameExtractor, xApiTopicNameExtractor.getStringStatementSerdes());

        return moodleForumPostAsJsonStream;
    }
}

