package com.edutec.moodleXapiTransformer.streams;

import com.edutec.moodleXapiTransformer.CdcUnpackService;
import com.edutec.moodleXapiTransformer.models.Moodle;
import com.edutec.moodleXapiTransformer.props.TopicsConfigs;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Properties;

@EnableKafkaStreams
public class MoodleUserCdcUnpackPipeTests {
    private static final String INPUT_TOPIC = "__.goethe-universitaet-frankfurt.db.moodle.mdl_user";
    private static final String OUTPUT_TOPIC = "__.goethe-universitaet-frankfurt.db.moodle.mdl_user.unpacked";
    private static final String STORENAME = "__.goethe-universitaet-frankfurt.db.moodle.mdl_user";
    @Mock
    TopicsConfigs.TopicsProvider topicsProvider;
    @Mock
    TopicsConfigs.StoreNameProvider storeNameProvider;
    private TopologyTestDriver testDriver;
    private ConsumerRecordFactory<?, CdcWrapper> userRecordFactory;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        final TopicsConfigs.Topic inputTopic = new TopicsConfigs.Topic();
        inputTopic.setTopicname(INPUT_TOPIC);
        Mockito.when(topicsProvider.getMoodle_user_source()).thenReturn(inputTopic);
        final TopicsConfigs.Topic throughTopic = new TopicsConfigs.Topic();
        throughTopic.setTopicname(OUTPUT_TOPIC);
        Mockito.when(topicsProvider.getMoodle_user_source_unpacked()).thenReturn(throughTopic);
        Mockito.when(storeNameProvider.getMdl_user_source_unpacked_store()).thenReturn(STORENAME);

        StreamsBuilder builder = new StreamsBuilder();
        new MoodleUserCdcUnpackPipe(new CdcUnpackService(), topicsProvider, storeNameProvider).mdlUserToAgentStream(builder);

        Topology topology = builder.build();

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, new JsonSerde<>(CdcWrapper.class).getClass());
        config.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, JsonNode.class);

        userRecordFactory = new ConsumerRecordFactory<String, CdcWrapper>(INPUT_TOPIC, Serdes.String().serializer(), new JsonSerde<CdcWrapper>().serializer());

        testDriver = new TopologyTestDriver(topology, config);


    }


    @After()
    public void close() throws Exception {
        testDriver.close();
    }

    /**
     * test unwrapping and keying of cdc wrapper json of mdl_user, expecting one Moodle.MdlUser in output topic and store
     *
     * @throws Exception
     */
    @Test
    public void shouldPublishOneUser() throws Exception {
        final CdcWrapper<Moodle.MdlUser> cdcWrapper = CdcWrapper.AFTER_MDL_USER();
        testDriver.pipeInput(userRecordFactory.create(INPUT_TOPIC, cdcWrapper));
        final ProducerRecord<Long, Moodle.MdlUser> record = testDriver.readOutput(OUTPUT_TOPIC, Serdes.Long().deserializer(), new JsonSerde<>(Moodle.MdlUser.class).deserializer());
        final ProducerRecord<Long, Moodle.MdlUser> record2 = testDriver.readOutput(OUTPUT_TOPIC, Serdes.Long().deserializer(), new JsonSerde<>(Moodle.MdlUser.class).deserializer());
        Assert.assertNull(record2);
        Assert.assertNotNull(record);
        Assert.assertEquals(cdcWrapper.getAfter().getId(), record.key());
        Assert.assertEquals(cdcWrapper.getAfter().getFirstname(), record.value().getFirstname());
        Assert.assertEquals(cdcWrapper.getAfter().getId(), record.value().getId());

        final Moodle.MdlUser fromStore = (Moodle.MdlUser) testDriver.getKeyValueStore(STORENAME).get(cdcWrapper.getAfter().getId());
        Assert.assertEquals(record.value(), fromStore);
    }

    /**
     * test deletion of user in moodle; expecting tombstone enty
     *
     * @throws Exception
     */
    @Test
    public void shouldDeleteOneUser() throws Exception {
        final CdcWrapper<Moodle.MdlUser> cdcWrapper = CdcWrapper.AFTER_MDL_USER();
        final CdcWrapper<Moodle.MdlUser> deleteWrapper = CdcWrapper.BEFORE_MDL_USER();
        testDriver.pipeInput(userRecordFactory.create(INPUT_TOPIC, cdcWrapper));
        testDriver.pipeInput(userRecordFactory.create(INPUT_TOPIC, deleteWrapper));

        final ProducerRecord<Long, Moodle.MdlUser> firstRecord = testDriver.readOutput(OUTPUT_TOPIC, Serdes.Long().deserializer(), new JsonSerde<>(Moodle.MdlUser.class).deserializer());
        final ProducerRecord<Long, Moodle.MdlUser> deletionRecord = testDriver.readOutput(OUTPUT_TOPIC, Serdes.Long().deserializer(), new JsonSerde<>(Moodle.MdlUser.class).deserializer());
        final ProducerRecord<Long, Moodle.MdlUser> record3 = testDriver.readOutput(OUTPUT_TOPIC, Serdes.Long().deserializer(), new JsonSerde<>(Moodle.MdlUser.class).deserializer());
        Assert.assertNull(record3);
        Assert.assertNotNull(deletionRecord);
        Assert.assertEquals(cdcWrapper.getAfter().getId(), deletionRecord.key());
        Assert.assertNull(deletionRecord.value());
        testDriver.getKeyValueStore(STORENAME).flush();

        final Moodle.MdlUser fromStore = (Moodle.MdlUser) testDriver.getKeyValueStore(STORENAME).get(deleteWrapper.getBefore().getId());
        Assert.assertTrue(fromStore.isTombstone());
    }

}
