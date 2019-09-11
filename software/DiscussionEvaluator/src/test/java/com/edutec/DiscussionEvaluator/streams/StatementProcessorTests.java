package com.edutec.DiscussionEvaluator.streams;

import com.edutec.DiscussionEvaluator.helper.ForumPostVerbs;
import com.edutec.DiscussionEvaluator.models.DiscussionPostStat;
import com.edutec.DiscussionEvaluator.models.NumberOfPostsPerDiscussion;
import com.edutec.DiscussionEvaluator.models.xapimodels.*;
import com.edutec.DiscussionEvaluator.props.TopicsConfigs;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@EnableKafkaStreams
public class StatementProcessorTests {
    private static final String INPUT_TOPIC = "edutec.micro.discussion";

    private static final String OUTPUT_TOPIC = "discussion-analytics";

    @Mock
    private TopicsConfigs.TopicsProvider topicsProvider;
    @Mock
    private TopicsConfigs.StoreNameProvider storeNameProvider;

    private TopologyTestDriver testDriver;
    private ConsumerRecordFactory<String, Statement> statementRecordFactory;

    public static void assertEqualityOfMap(Map<String, Long> actual, Map<String, Long> expectedMap) {
        Assert.assertTrue(actual.size() == expectedMap.size());
        actual.forEach((k, v) -> {
            Assert.assertTrue(expectedMap.containsKey(k));
            Assert.assertTrue(expectedMap.get(k).equals(v));
        });
    }

    public static void assertEqualityOfLongSummaryStatistic(LongSummaryStatistics expected, LongSummaryStatistics actual) {
        Assert.assertEquals(expected.getCount(), actual.getCount());
        Assert.assertEquals(expected.getMax(), actual.getMax());
        Assert.assertEquals(expected.getMin(), actual.getMin());
        Assert.assertEquals(expected.getSum(), actual.getSum());
        Assert.assertEquals(expected.getAverage(), actual.getAverage(), 0);
    }

    @Before()
    public void setup() throws Exception {
        initMocks();
        // Create topology
        StreamsBuilder builder = new StreamsBuilder();
        new StatementProcessor(topicsProvider, storeNameProvider).counterPerUser(builder);
        Topology topology = builder.build();

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, new JsonSerde<>(NumberOfPostsPerDiscussion.class).getClass());
        config.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);
        config.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 2);


        statementRecordFactory = new ConsumerRecordFactory<>(INPUT_TOPIC,
                Serdes.String().serializer(), new JsonSerde<>(Statement.class).serializer());
        // Run it on the test driver
        testDriver = new TopologyTestDriver(topology, config);
    }

    private void initMocks() {
        MockitoAnnotations.initMocks(this);
        final TopicsConfigs.Topic discussion = TopicsConfigs.Topic.of(INPUT_TOPIC);
        final TopicsConfigs.Topic analytics = TopicsConfigs.Topic.of(OUTPUT_TOPIC);
        Mockito.when(topicsProvider.getDiscussion_analytics()).thenReturn(analytics);
        Mockito.when(topicsProvider.getXapi_statement_discussion()).thenReturn(discussion);
        Mockito.when(storeNameProvider.getNumber_of_posts_per_user()).thenReturn("n");
        Mockito.when(storeNameProvider.getReply_time_of_post_per_discussion()).thenReturn("r");
    }

    @After()
    public void close() throws Exception {
        testDriver.close();
    }

    /**
     * should calculate the stats for one statement / post
     *
     * @throws Exception
     */
    @Test
    public void shouldCount1() throws Exception {
        // Feed input data
        String activityId = "activityId1";
        String actorName = "actorName1";
        Statement validStatement = getValidStatement(actorName, activityId);

        // expected value
        String expectedKey = actorName;
        LongSummaryStatistics averageForumPostsPerDiscusison = new LongSummaryStatistics();
        averageForumPostsPerDiscusison.accept(1L);
        DiscussionPostStat stat = new DiscussionPostStat();
        stat.setForumPosts(1L);
        stat.setAverageForumPostsPerDiscussion(averageForumPostsPerDiscusison);
        // stream input
        testDriver.pipeInput(statementRecordFactory.create(INPUT_TOPIC, validStatement.getId().toString(), validStatement));
        // run test
        ProducerRecord<String, DiscussionPostStat> statRecord = testDriver.readOutput(OUTPUT_TOPIC, Serdes.String().deserializer(), new JsonSerde<>(DiscussionPostStat.class).deserializer());

        // assertions
        assertThatOneStatementArrivedForActor(statRecord, actorName, activityId);
    }

    /**
     * should calculate the stats for two statements / posts by same actor in same discussion / forum
     *
     * @throws Exception
     */
    @Test
    public void shouldCount2() throws Exception {
        // Feed input data
        String actorName = "actorName1";
        String activityId = "activity1";
        Statement validStatement = getValidStatement(actorName, activityId);
        Statement validStatement2 = getValidStatement(actorName, activityId);

        testDriver.pipeInput(statementRecordFactory.create(INPUT_TOPIC, validStatement.getId().toString(), validStatement));
        testDriver.pipeInput(statementRecordFactory.create(INPUT_TOPIC, validStatement2.getId().toString(), validStatement2));
        // run test
        ProducerRecord<String, DiscussionPostStat> statRecord = testDriver.readOutput(OUTPUT_TOPIC, Serdes.String().deserializer(), new JsonSerde<>(DiscussionPostStat.class).deserializer());
        ProducerRecord<String, DiscussionPostStat> finalRecord = testDriver.readOutput(OUTPUT_TOPIC, Serdes.String().deserializer(), new JsonSerde<>(DiscussionPostStat.class).deserializer());

        // expected value second record
        DiscussionPostStat stat2 = new DiscussionPostStat();
        stat2.setForumPosts(2L);
        LongSummaryStatistics averageForumPostsPerDiscusison2 = new LongSummaryStatistics();
        averageForumPostsPerDiscusison2.accept(2L);
        stat2.setAverageForumPostsPerDiscussion(averageForumPostsPerDiscusison2);

        // assertions
        assertEqualityOfLongSummaryStatistic(new LongSummaryStatistics(1, 2, 2, 2), finalRecord.value().getAverageForumPostsPerDiscussion());
        Map<String, Long> expectedMap2 = Map.of(activityId, 2L);
        assertEqualityOfMap(expectedMap2, finalRecord.value().getForumPostsPerDiscussion());

        Assert.assertEquals((Long) 2L, (Long) finalRecord.value().getForumPosts());
        Assert.assertEquals(actorName, statRecord.key());

        KeyValueStore<String, NumberOfPostsPerDiscussion> store = testDriver.getKeyValueStore(storeNameProvider.getNumber_of_posts_per_user());
        Assert.assertTrue(
                store.get(actorName)
                        .getNumberOfPostsPerDiscussion()
                        .get(activityId).equals(2L));
    }

    /**
     * should calculate the stats for two statements / posts by different actors in same discussion / forum
     *
     * @throws Exception
     */
    @Test
    public void shouldCount1EachBy1Actor() throws Exception {
        // Feed input data
        String actorName = "actorName1";
        String actorName2 = "actorName2";
        String activityId = "activity1";
        Statement validStatement = getValidStatement(actorName, activityId);
        Statement validStatement2 = getValidStatement(actorName2, activityId);

        testDriver.pipeInput(List.of(
                statementRecordFactory.create(INPUT_TOPIC, validStatement.getId().toString(), validStatement),
                statementRecordFactory.create(INPUT_TOPIC, validStatement2.getId().toString(), validStatement2)
        ));
        // run test
        ProducerRecord<String, DiscussionPostStat> statRecord = testDriver.readOutput(OUTPUT_TOPIC, Serdes.String().deserializer(), new JsonSerde<>(DiscussionPostStat.class).deserializer());
        ProducerRecord<String, DiscussionPostStat> statRecord2 = testDriver.readOutput(OUTPUT_TOPIC, Serdes.String().deserializer(), new JsonSerde<>(DiscussionPostStat.class).deserializer());

        // expected value final record
        assertThatOneStatementArrivedForActor(statRecord, actorName, activityId);
        assertThatOneStatementArrivedForActor(statRecord2, actorName2, activityId);
    }

    /**
     * should calculate the stats for three statements / posts, 2 by by different actors in same discussion / forum and 1 in another discussion
     *
     * @throws Exception
     */
    @Test
    public void shouldCount3ByTwoActors() throws Exception {
        // Feed input data
        String actorName = "actorName1";
        String actorName2 = "actorName2";
        String activityId = "activity1";
        String activityId2 = "activity2";

        Statement validStatement = getValidStatement(actorName, activityId);
        Statement validStatement2 = getValidStatement(actorName2, activityId);
        Statement validStatement3 = getValidStatement(actorName2, activityId2);

        testDriver.pipeInput(List.of(
                statementRecordFactory.create(INPUT_TOPIC, validStatement.getId().toString(), validStatement),
                statementRecordFactory.create(INPUT_TOPIC, validStatement2.getId().toString(), validStatement2),
                statementRecordFactory.create(INPUT_TOPIC, validStatement3.getId().toString(), validStatement3)
        ));
        // run test
        ProducerRecord<String, DiscussionPostStat> statRecord = testDriver.readOutput(OUTPUT_TOPIC, Serdes.String().deserializer(), new JsonSerde<>(DiscussionPostStat.class).deserializer());
        ProducerRecord<String, DiscussionPostStat> statRecord2 = testDriver.readOutput(OUTPUT_TOPIC, Serdes.String().deserializer(), new JsonSerde<>(DiscussionPostStat.class).deserializer());
        ProducerRecord<String, DiscussionPostStat> finalRecord = testDriver.readOutput(OUTPUT_TOPIC, Serdes.String().deserializer(), new JsonSerde<>(DiscussionPostStat.class).deserializer());

        // expected value final record
        assertThatOneStatementArrivedForActor(statRecord, actorName, activityId);

        DiscussionPostStat finalValueActor2 = finalRecord.value();
        Assert.assertTrue(finalValueActor2.getForumPosts().equals(2L));

        Map<String, Long> expectedMap = Map.of(activityId, 1L, activityId2, 1L);
        assertEqualityOfMap(finalValueActor2.getForumPostsPerDiscussion(), expectedMap);

        assertEqualityOfLongSummaryStatistic(new LongSummaryStatistics(2, 1, 1, 2), finalValueActor2.getAverageForumPostsPerDiscussion());
    }

    /**
     * should calculate the time of reply for two statements
     *
     * @throws Exception
     */
    @Test
    public void shouldCalculateTimeOfReply() throws Exception {
        // Feed input data

        String actorName = "actorName1";
        String actorName2 = "actorName2";
        String activityId = "activity1";

        Statement validStatement = getValidStatement(actorName, activityId);
        validStatement.setTimestamp(DateTime.now());
        Statement validStatement2 = getValidStatement(actorName2, activityId);
        validStatement2.setVerb(ForumPostVerbs.replied());
        validStatement2.setTimestamp(validStatement.getTimestamp().plusSeconds(42));
        validStatement2.setObject(new StatementRef(validStatement.getId()));

        testDriver.pipeInput(statementRecordFactory.create(INPUT_TOPIC, validStatement.getId().toString(), validStatement));
        testDriver.pipeInput(statementRecordFactory.create(INPUT_TOPIC, validStatement2.getId().toString(), validStatement2));
        // run test
        ProducerRecord<String, DiscussionPostStat> statRecord = testDriver.readOutput(OUTPUT_TOPIC, Serdes.String().deserializer(), new JsonSerde<>(DiscussionPostStat.class).deserializer());
        ProducerRecord<String, DiscussionPostStat> statRecord2 = testDriver.readOutput(OUTPUT_TOPIC, Serdes.String().deserializer(), new JsonSerde<>(DiscussionPostStat.class).deserializer());

        Assert.assertEquals(42, statRecord2.value().getAverageReplyTimeOfPostPerDiscussion().get("activity1").getSum());
    }


    /**
     * @throws Exception
     */
    @Test
    public void shouldWorkWithRealJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Moodle Users from json file, representing an original stream from the database
        List<Statement> statements = null;
        try {
            TypeReference typeReference = new TypeReference<List<Statement>>() {
            };
            Resource resource = new ClassPathResource("edutec-micro-discussion.json");
            File file = resource.getFile();
            statements = mapper.readValue(file, typeReference);
        } catch (IOException e) {
            System.out.println("Unable to save users: " + e.getMessage());
            throw new RuntimeException("resource read failed");
        }
        Assert.assertEquals(47, statements.size());
        statements.stream().forEach(next -> testDriver.pipeInput(statementRecordFactory.create(INPUT_TOPIC, next.getId().toString(), next)));

        List<ProducerRecord<String, DiscussionPostStat>> records = new ArrayList<>();
        records.add(testDriver.readOutput(OUTPUT_TOPIC, Serdes.String().deserializer(),
                new JsonSerde<>(DiscussionPostStat.class).deserializer()));
        while (records.get(records.size() - 1) != null) {
            records.add(testDriver.readOutput(OUTPUT_TOPIC, Serdes.String().deserializer(),
                    new JsonSerde<>(DiscussionPostStat.class).deserializer()));
        }
        records = records.stream().filter(Objects::nonNull).collect(Collectors.toList());
        Assert.assertEquals(47, records.size());
        final HashMap<String, DiscussionPostStat> stringDiscussionPostStatHashMap = new HashMap<>();
        records.forEach(n -> {
            if (!stringDiscussionPostStatHashMap.containsKey(n.key()) || (stringDiscussionPostStatHashMap.containsKey(n.key()) && stringDiscussionPostStatHashMap.get(n.key()).getForumPosts() < n.value().getForumPosts()))
                stringDiscussionPostStatHashMap.put(n.key(), n.value());
        });
        stringDiscussionPostStatHashMap.keySet().forEach(next -> {
            if (!next.equals("franz3 kafka3"))
                Assert.assertTrue(10L == stringDiscussionPostStatHashMap.get(next).getForumPosts());
            else
                Assert.assertTrue(7L == stringDiscussionPostStatHashMap.get(next).getForumPosts());
        });
    }

    private void assertThatOneStatementArrivedForActor(ProducerRecord<String, DiscussionPostStat> statRecord, String actorName, String activityId) {
        DiscussionPostStat stat = new DiscussionPostStat();
        stat.setForumPosts(1L);
        LongSummaryStatistics averageForumPostsPerDiscusison = new LongSummaryStatistics();
        averageForumPostsPerDiscusison.accept(1L);
        stat.setAverageForumPostsPerDiscussion(averageForumPostsPerDiscusison);

        // assertions
        assertEqualityOfLongSummaryStatistic(new LongSummaryStatistics(1l, 1l, 1l, 1l), statRecord.value().getAverageForumPostsPerDiscussion());
        Map<String, Long> expectedMap = Map.of(activityId, 1L);
        statRecord.value().getForumPostsPerDiscussion().forEach((k, v) -> {
            Assert.assertTrue(expectedMap.containsKey(k));
            Assert.assertTrue(expectedMap.get(k).equals(v));
        });
        Assert.assertEquals((Long) 1L, (Long) statRecord.value().getForumPosts());
        Assert.assertEquals(actorName, statRecord.key());

        KeyValueStore<String, NumberOfPostsPerDiscussion> store = testDriver.getKeyValueStore(storeNameProvider.getNumber_of_posts_per_user());
        Assert.assertTrue(
                store.get(actorName)
                        .getNumberOfPostsPerDiscussion()
                        .get(activityId).equals(1L));
    }

    private Statement getValidStatement(String actorName, String activitiyRespDiscussionId) {
        Statement statement = new Statement();
        statement.setId(UUID.randomUUID());
        statement.setActor(getValidActor(actorName));
        statement.setContext(getValidContext(activitiyRespDiscussionId));
        statement.setVerb(ForumPostVerbs.posted());
        statement.setTimestamp(DateTime.now());
        return statement;
    }

    private Context getValidContext(String activitiyRespDiscussionId) {
        Context context = new Context();
        context.setContextActivities(getValidContextActivities(activitiyRespDiscussionId));
        return context;
    }

    private ContextActivities getValidContextActivities(String activitiyRespDiscussionId) {
        ContextActivities contextActivities = new ContextActivities();
        contextActivities.setCategory(getValidCategory(activitiyRespDiscussionId));
        return contextActivities;
    }

    private List<Activity> getValidCategory(String activitiyRespDiscussionId) {
        return List.of(new Activity(URI.create(activitiyRespDiscussionId)));
    }

    private Agent getValidActor(String actorName) {
        Agent actor = new Agent();
        actor.setName(actorName);
        return actor;
    }
}
