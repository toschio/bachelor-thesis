package com.edutec.moodleXapiTransformer.streams;

import com.edutec.moodleXapiTransformer.CdcUnpackService;
import com.edutec.moodleXapiTransformer.models.Moodle;
import com.edutec.moodleXapiTransformer.models.TombStone;
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
 * consumes a stream of cdc-wrapped {@link Moodle.MdlUser}, i.e. especially holding values of state
 * before- and -after DB actions executed
 * transforms this json into a proper java object {@link Moodle.MdlUser} and tabularizes the stream
 */
@Configuration
@RequiredArgsConstructor
public class MoodleUserCdcUnpackPipe {

    // service to unpack JSON representing cdc values
    private final CdcUnpackService cdcUnpackService;
    private final TopicsConfigs.TopicsProvider topicsProvider;
    private final TopicsConfigs.StoreNameProvider storeNameProvider;

    @Bean(name = "keyedMoodleUser")
    public KTable<Long, Moodle.MdlUser> mdlUserToAgentStream(StreamsBuilder streamsBuilder) {
        // input stream representing moodle user in cdc wrapper
        KStream<?, JsonNode> moodleRawValueStream = streamsBuilder.stream(topicsProvider.getMoodle_user_source().getTopicname());

        // unpack cdc wrapper
        return moodleRawValueStream
                .selectKey((KeyValueMapper<Object, JsonNode, Long>) (key, value) -> cdcUnpackService.unpackKey(value).get())
                // create a proper POJO MdlUser from Json representing raw data from moodle DB
                .mapValues(value -> {
                    //JSON to Java object - create an Moodle User Object
                    return cdcUnpackService.unpack(value, Moodle.MdlUser.class);
                })
                .filter(StandardStreamsOperations.KEYNOTNULL)
                // publish for ksql
                .through(topicsProvider.getMoodle_user_source_unpacked().getTopicname(), Produced.with(Serdes.Long(),
                        new JsonSerde<>(Moodle.MdlUser.class)))
                .mapValues(value -> value == null ? TombStone.dig(new Moodle.MdlUser()) : value)
                // make table
                .groupByKey(Serialized.with(Serdes.Long(), new JsonSerde<>(Moodle.MdlUser.class)))
                .reduce((o, n) -> n,
                        Materialized.<Long, Moodle.MdlUser, KeyValueStore<Bytes, byte[]>>as(storeNameProvider.getMdl_user_source_unpacked_store()).withKeySerde(Serdes.Long())
                                .withValueSerde(new JsonSerde<>(Moodle.MdlUser.class)))
                .filterNot((key, value) -> value.isTombstone());
    }
}
