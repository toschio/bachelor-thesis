package com.edutec.moodleXapiTransformer.streams;

import com.edutec.moodleXapiTransformer.CdcUnpackService;
import com.edutec.moodleXapiTransformer.models.Moodle;
import com.edutec.moodleXapiTransformer.props.TopicsConfigs;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

/**
 * consumes a stream of cdc-wrapped {@link Moodle.MdlForum}, i.e. especially holding values of state
 * before- and -after DB actions executed
 * transforms this json into a proper java object {@link Moodle.MdlForum} and tabularizes the stream
 */
@Configuration
@RequiredArgsConstructor
public class MoodleForumCdcUnpackPipe {

    private final CdcUnpackService cdcUnpackService;
    private final TopicsConfigs.TopicsProvider topicsProvider;
    private final TopicsConfigs.StoreNameProvider storeNameProvider;

    @Bean(name = "keyedMoodleForum")
    public KTable<Long, Moodle.MdlForum> unpackMoodleForumCdcWrapper(StreamsBuilder streamsBuilder) {
        // input stream representing moodle forum in cdc wrapper
        KStream<?, JsonNode> moodleRawValueStream = streamsBuilder.stream(topicsProvider.getMoodle_forum_source().getTopicname());

        // unpack cdc wrapper
        return moodleRawValueStream
                // select and unpack key
                .selectKey((KeyValueMapper<Object, JsonNode, Long>) (key, value) -> cdcUnpackService.unpackKey(value).get())
                .mapValues(value -> {
                    //JSON to Java object - create an Moodle User Object
                    return cdcUnpackService.unpack(value, Moodle.MdlForum.class);
                })
                .filter(StandardStreamsOperations.VALUENOTNULL)
                // make table
                .groupByKey(Serialized.with(Serdes.Long(), new JsonSerde<>(Moodle.MdlForum.class)))
                .reduce((o, n) -> n,
                        Materialized.<Long, Moodle.MdlForum, KeyValueStore<Bytes, byte[]>>as(storeNameProvider.getMdl_forum_source_unpacked_store()).withKeySerde(Serdes.Long())
                                .withValueSerde(new JsonSerde<>(Moodle.MdlForum.class)));
    }
}
