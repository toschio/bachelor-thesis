package com.edutec.AssessmentEvaluator.streams;

import com.edutec.AssessmentEvaluator.models.GesturePerUserStats;
import com.edutec.AssessmentEvaluator.models.leapmotionmodels.LeapMotionFrame;
import com.edutec.AssessmentEvaluator.models.xapimodels.Agent;
import com.edutec.AssessmentEvaluator.models.xapimodels.Statement;
import com.edutec.AssessmentEvaluator.props.TopicsConfigs;
import com.edutec.AssessmentEvaluator.props.kafkaprops.DefaultTimestampExtractor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@EnableKafkaStreams
public class AssessmentAnalyticsTests {
    private static final TopicsConfigs.Topic LEAP_MOTION_TOPIC = TopicsConfigs.Topic.of("leap-motion");
    private static final TopicsConfigs.Topic ASSESSMENT_STATEMENT_TOPIC = TopicsConfigs.Topic.of("assessment-statement");
    private static final TopicsConfigs.Topic ANALYTICS_RESULT_TOPIC = TopicsConfigs.Topic.of("assessment-analytics");


    @Mock
    private TopicsConfigs.TopicsProvider topicsProvider;
    @Mock
    private TopicsConfigs.StoreNameProvider storeNameProvider;

    private TopologyTestDriver testDriver;
    private ConsumerRecordFactory<String, Statement> statementRecordFactory;
    private ConsumerRecordFactory<String, LeapMotionFrame> leapMotionRecordFactory;

    @Before()
    public void setup() throws Exception {
        initMocks();
        // Create topology
        StreamsBuilder builder = new StreamsBuilder();
        new AssessmentAnalytics(topicsProvider, storeNameProvider).leapFrame(builder);
        Topology topology = builder.build();

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        config.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);
        config.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, DefaultTimestampExtractor.class.getName());

        statementRecordFactory = new ConsumerRecordFactory<>(topicsProvider.getXapi_statement_assessment().getTopicname(),
                Serdes.String().serializer(), new JsonSerde<>(Statement.class).serializer());
        leapMotionRecordFactory = new ConsumerRecordFactory<>(topicsProvider.getLeap_motion().getTopicname(),
                Serdes.String().serializer(), new JsonSerde<>(LeapMotionFrame.class).serializer());
        // Run it on the test driver
        testDriver = new TopologyTestDriver(topology, config);
    }

    @After
    public void after() {
        this.testDriver.close();
    }

    private void initMocks() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(storeNameProvider.getUser_keyed_session_windowed_leap_motion()).thenReturn("__.goethe-universitaet-frankfurt.myo.frames.user-keyed.sessioned");
        Mockito.when(topicsProvider.getXapi_statement_assessment()).thenReturn(ASSESSMENT_STATEMENT_TOPIC);
        Mockito.when(topicsProvider.getAssessment_analytics()).thenReturn(ANALYTICS_RESULT_TOPIC);
        Mockito.when(topicsProvider.getLeap_motion()).thenReturn(LEAP_MOTION_TOPIC);
    }


    /**
     * publish gestures and swipes, checks for time window working
     *
     * @throws Exception
     */
    @Test
    public void pubishDifferentGesturesWithAStatement() throws Exception {
        Statement statement = publishStatement();

        LeapMotionFrame startCircle = getValidCircleGesture(1L);
        startCircle.getGestures().get(0).setState("start");
        startCircle.setTimestamp((LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - 41900) * 1000);
        LeapMotionFrame updateCircle = getValidCircleGesture(1L);
        updateCircle.getGestures().get(0).setState("update");
        updateCircle.setTimestamp(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * 1000);
        LeapMotionFrame stopCircle = getValidCircleGesture(1L);
        stopCircle.setTimestamp((LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) + 20) * 1000);
        stopCircle.getGestures().get(0).setState("stop");
        LeapMotionFrame swipeFrame = getValidSwipeGesture(2L);
        swipeFrame.setTimestamp((LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) + 10) * 1000);
        swipeFrame.getGestures().get(0).setState("stop");
        LeapMotionFrame circleFrame3 = getValidCircleGesture(3L);
        circleFrame3.getGestures().get(0).setState("stop");
        circleFrame3.setTimestamp(swipeFrame.getTimestamp() + 10 * 1000);
        LeapMotionFrame loneCircle = getValidCircleGesture(4L);
        loneCircle.getGestures().get(0).setState("stop");
        loneCircle.setId(55L);
        loneCircle.setTimestamp(circleFrame3.getTimestamp() + 42001* 1000); // one millisecond after session window closes

        testDriver.pipeInput(leapMotionRecordFactory.create(topicsProvider.getLeap_motion().getTopicname(), statement.getActor().getName(), startCircle));
        testDriver.pipeInput(leapMotionRecordFactory.create(topicsProvider.getLeap_motion().getTopicname(), statement.getActor().getName(), updateCircle));
        testDriver.pipeInput(leapMotionRecordFactory.create(topicsProvider.getLeap_motion().getTopicname(), statement.getActor().getName(), swipeFrame));
        testDriver.pipeInput(leapMotionRecordFactory.create(topicsProvider.getLeap_motion().getTopicname(), statement.getActor().getName(), stopCircle));
        testDriver.pipeInput(leapMotionRecordFactory.create(topicsProvider.getLeap_motion().getTopicname(), statement.getActor().getName(), circleFrame3));
//        testDriver.pipeInput(leapMotionRecordFactory.create(topicsProvider.getLeap_motion().getTopicname(), statement.getActor().getName(), loneCircle,
//                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) + 44300)); // this would be right, if working not with Event Time but Log time
        testDriver.pipeInput(leapMotionRecordFactory.create(topicsProvider.getLeap_motion().getTopicname(), statement.getActor().getName(), loneCircle)); // attention: the message timestamp should be irrelevant

        List<ProducerRecord<String, GesturePerUserStats>> records = getResults();
        Assert.assertNotNull(records);
        // all but the last of the publications are inside the same session window
        final GesturePerUserStats value1 = records.get(0).value();
        verifyAggregatingResult(value1, 0, 0, 0, 0);
        final GesturePerUserStats value2 = records.get(1).value();
        verifyAggregatingResult(value2, 0, 0, 0, 0);
        final GesturePerUserStats value3 = records.get(2).value();
        verifyAggregatingResult(value3, 0, 0, 0, 1);
        final GesturePerUserStats value4 = records.get(3).value();
        verifyAggregatingResult(value4, 1, 0, 0, 1);
        final GesturePerUserStats value5 = records.get(4).value();
        verifyAggregatingResult(value5, 2, 0, 0, 1);
        // for the final publication, we expect count 1 on circle, as it is outside the session window !
        final GesturePerUserStats finalCirlce = records.get(5).value();
        verifyAggregatingResult(finalCirlce, 1, 0, 0, 0);
        records.forEach(next -> {
            if (next != null && next.value() != null) {
                Assert.assertNotNull(next.value().getAssessment().getActor().getName());
            }
        });
    }



    /**
     * publish 2 different swipe gestures and validates overall duration
     *
     * @throws Exception
     */
    @Test
    public void shouldCalculateRightDurationOverTwoCircles() throws Exception {
        Statement statement = publishStatement();
        LeapMotionFrame startCircle = getValidCircleGesture(1L);
        startCircle.getGestures().get(0).setState("start");
        LeapMotionFrame updateCircle = getValidCircleGesture(1L);
        updateCircle.getGestures().get(0).setState("update");
        updateCircle.getGestures().get(0).setDuration(121L);
        LeapMotionFrame stopCircle = getValidCircleGesture(1L);
        stopCircle.getGestures().get(0).setState("stop");
        stopCircle.getGestures().get(0).setDuration(123L);

        LeapMotionFrame startCircle1 = getValidCircleGesture(2L);
        startCircle1.getGestures().get(0).setState("start");
        LeapMotionFrame updateCircle1 = getValidCircleGesture(2L);
        updateCircle1.getGestures().get(0).setState("update");
        LeapMotionFrame stopCircle1 = getValidCircleGesture(2L);
        stopCircle1.getGestures().get(0).setState("stop");

        List<LeapMotionFrame> frames = Arrays.asList(startCircle, startCircle1, updateCircle1, updateCircle, stopCircle, stopCircle1);
        testDriver.pipeInput(frames.stream().map(next -> leapMotionRecordFactory.create(topicsProvider.getLeap_motion().getTopicname(), statement.getActor().getName(),
                next)).collect(Collectors.toList()));

        List<ProducerRecord<String, GesturePerUserStats>> records = getResults();
        Assert.assertNotNull(records);
        Assert.assertEquals(123L, records.get(records.size() - 1).value().getCircleDuration().getMax());
        Assert.assertEquals(100L, records.get(records.size() - 1).value().getCircleDuration().getMin());
        Assert.assertEquals(223, records.get(records.size() - 1).value().getCircleDuration().getSum());
        Assert.assertEquals(2, records.get(records.size() - 1).value().getCircleDuration().getCount());
    }


    /**
     * publish 2 different swipe gestures and validates overall duration
     *
     * @throws Exception
     */
    @Test
    public void testMerger() throws Exception {
        Statement statement = publishStatement();
        LeapMotionFrame startCircle = getValidCircleGesture(1L);
        startCircle.getGestures().get(0).setState("start");
        startCircle.setTimestamp(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)* 1000);
        LeapMotionFrame updateCircle = getValidCircleGesture(1L);
        updateCircle.getGestures().get(0).setState("update");
        updateCircle.setTimestamp(startCircle.getTimestamp() + 10* 1000);
        updateCircle.getGestures().get(0).setDuration(121L);
        LeapMotionFrame stopCircle = getValidCircleGesture(1L);
        stopCircle.getGestures().get(0).setState("stop");
        startCircle.setTimestamp(updateCircle.getTimestamp() + 10* 1000);
        stopCircle.getGestures().get(0).setDuration(123L);

        LeapMotionFrame startCircle1 = getValidCircleGesture(2L);
        startCircle1.setTimestamp(stopCircle.getTimestamp() + 44000* 1000);
        startCircle1.getGestures().get(0).setState("start");
        LeapMotionFrame updateCircle1 = getValidCircleGesture(2L);
        updateCircle1.setTimestamp(startCircle1.getTimestamp() + 10* 1000);
        updateCircle1.getGestures().get(0).setState("update");
        LeapMotionFrame stopCircle1 = getValidCircleGesture(2L);
        stopCircle1.setTimestamp(updateCircle1.getTimestamp() + 10* 1000);
        stopCircle1.getGestures().get(0).setState("stop");

        List<LeapMotionFrame> frames = Arrays.asList(startCircle, updateCircle, stopCircle, startCircle1, updateCircle1, stopCircle1);
        testDriver.pipeInput(frames.stream().map(next -> leapMotionRecordFactory.create(topicsProvider.getLeap_motion().getTopicname(), statement.getActor().getName(),
                next)).collect(Collectors.toList()));

        LeapMotionFrame someCircle = getValidCircleGesture(3L);
        someCircle.getGestures().get(0).setState("start");
        someCircle.setTimestamp(stopCircle.getTimestamp() + 22000* 1000);
        testDriver.pipeInput(leapMotionRecordFactory.create(topicsProvider.getLeap_motion().getTopicname(), statement.getActor().getName(), someCircle));
        List<ProducerRecord<String, GesturePerUserStats>> records = getResults();
        Assert.assertNotNull(records);
        // we expect first two sessions, which become merged
        Assert.assertTrue(records.get(0).value().getCircles() == 0);
        Assert.assertTrue(records.get(1).value().getCircles() == 0);
        Assert.assertTrue(records.get(2).value().getCircles() == 1);
        Assert.assertEquals(123L, records.get(2).value().getCircleDuration().getSum());
        Assert.assertTrue(records.get(3).value().getCircles() == 0);
        Assert.assertTrue(records.get(4).value().getCircles() == 0);
        Assert.assertTrue(records.get(5).value().getCircles() == 1);
        Assert.assertEquals(100L, records.get(5).value().getCircleDuration().getSum());
        Assert.assertTrue(records.get(6).value().getCircles() == 2);
        Assert.assertEquals(223L, records.get(6).value().getCircleDuration().getSum());

    }


    /**
     * publishes a valid assessment statement
     *
     * @return
     */
    private Statement publishStatement() {
        Statement statement = getValidAssessmentStatement();
        testDriver.pipeInput(statementRecordFactory.create(topicsProvider.getXapi_statement_assessment().getTopicname(), statement.getId().toString(),
                statement));
        return statement;
    }

    /**
     * publishes a valid assessment statement
     *
     * @return
     */
    private Statement publishStatement(String username) {
        Statement statement = getValidAssessmentStatement();
        statement.getActor().setName(username);
        testDriver.pipeInput(statementRecordFactory.create(topicsProvider.getXapi_statement_assessment().getTopicname(), statement.getId().toString(),
                statement));
        return statement;
    }

    /**
     * test two statements by different actors based on recorded leap-motion json
     *
     * @throws Exception
     */
    @Test
    public void testWithFramesJSon() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<List<LeapMotionFrame>> typeReference = new TypeReference<List<LeapMotionFrame>>() {
        };
        List<LeapMotionFrame> frames = null;
        try {
            Resource resource = new ClassPathResource("frames.json");
            File file = resource.getFile();
            frames = mapper.readValue(file, typeReference);
        } catch (IOException e) {
            System.out.println("Unable to save users: " + e.getMessage());
        }
        final long now = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)* 1000;
        Assert.assertNotNull(frames);
        Assert.assertTrue(frames.size() > 0);
        Statement statement = publishStatement("user1");
        Statement statement2 = publishStatement("user2");

        final List<ConsumerRecord<byte[], byte[]>> user1Stream = frames.stream()
                .map(n -> leapMotionRecordFactory
                        .create(topicsProvider.getLeap_motion().getTopicname(), statement.getActor().getName(), n, now + (now - (n.getTimestamp() / 1000))))
                .collect(Collectors.toList());
        final List<ConsumerRecord<byte[], byte[]>> user2Stream = frames.stream()
                .map(n -> leapMotionRecordFactory
                        .create(topicsProvider.getLeap_motion().getTopicname(), statement2.getActor().getName(), n, now + (now - (n.getTimestamp() / 1000))))
                .collect(Collectors.toList());
        for (int i = 0; i < user1Stream.size(); i++) {
            testDriver.pipeInput(user1Stream.get(i));
            testDriver.pipeInput(user2Stream.get(i));
        }
        List<ProducerRecord<String, GesturePerUserStats>> records = getResults();
        Assert.assertFalse(records.isEmpty());
        // key validation
        Assert.assertTrue(records.stream().map(n -> n.key()).distinct().collect(Collectors.toList()).contains(statement.getActor().getName()));
        Assert.assertTrue(records.stream().map(n -> n.key()).distinct().collect(Collectors.toList()).contains(statement2.getActor().getName()));
        Assert.assertTrue(records.stream().map(n -> n.key()).distinct().collect(Collectors.toList()).size() == 2);
        // content validaton
        Assert.assertTrue(records.stream().filter(n -> n.key().equals(statement.getActor().getName())).filter(n -> n.value().getCircles().equals(0)).count() > 0);
        Assert.assertTrue(records.stream().filter(n -> n.key().equals(statement.getActor().getName())).anyMatch(n -> n.value().getCircles().equals(1)));
        Assert.assertTrue(records.stream().filter(n -> n.key().equals(statement.getActor().getName())).anyMatch(n -> n.value().getCircles().equals(2)));
        Assert.assertTrue(records.stream().filter(n -> n.key().equals(statement.getActor().getName())).anyMatch(n -> n.value().getCircles().equals(3)));
        Assert.assertFalse(records.stream().filter(n -> n.key().equals(statement.getActor().getName())).anyMatch(n -> n.value().getCircles().compareTo(4) > 0));

        Assert.assertTrue(records.stream().filter(n -> n.key().equals(statement2.getActor().getName())).filter(n -> n.value().getCircles().equals(0)).count() > 0);
        Assert.assertTrue(records.stream().filter(n -> n.key().equals(statement2.getActor().getName())).anyMatch(n -> n.value().getCircles().equals(1)));
        Assert.assertTrue(records.stream().filter(n -> n.key().equals(statement2.getActor().getName())).anyMatch(n -> n.value().getCircles().equals(2)));
        Assert.assertTrue(records.stream().filter(n -> n.key().equals(statement2.getActor().getName())).anyMatch(n -> n.value().getCircles().equals(3)));
        Assert.assertFalse(records.stream().filter(n -> n.key().equals(statement2.getActor().getName())).anyMatch(n -> n.value().getCircles().compareTo(4) > 0));
    }


    /**
     * test two statements by different actors based on recorded leap-motion json
     *
     * @throws Exception
     */
    @Test
    public void testWithJSons() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<Statement> statements = null;
        {
            TypeReference<List<Statement>> typeReference = new TypeReference<List<Statement>>() {
            };
            Resource resource = new ClassPathResource("assessment.json");
            File file = resource.getFile();
            statements = mapper.readValue(file, typeReference);
        }
        Assert.assertNotNull(statements);
        Assert.assertTrue(statements.size() > 0);
        testDriver.pipeInput(statements.stream()
                .map(statement -> statementRecordFactory.create(
                        topicsProvider.getXapi_statement_assessment().getTopicname(), statement.getId().toString(), statement))
                .collect(Collectors.toList()));

        List<LeapMotionFrame> frames = null;
        {
            TypeReference<List<LeapMotionFrame>> typeReference = new TypeReference<List<LeapMotionFrame>>() {
            };
            Resource resource = new ClassPathResource("frames.json");
            File file = resource.getFile();
            frames = mapper.readValue(file, typeReference);
        }
        final long now = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)* 1000;
        Assert.assertNotNull(frames);
        Assert.assertTrue(frames.size() > 0);


        List<Statement> finalStatements = statements;
        final List<ConsumerRecord<byte[], byte[]>> user1Stream = frames.stream()
                .map(n -> leapMotionRecordFactory
                        .create(topicsProvider.getLeap_motion().getTopicname(), finalStatements.get(2).getActor().getName(), n, now + (now - (n.getTimestamp() / 1000))))
                .collect(Collectors.toList());
        final List<ConsumerRecord<byte[], byte[]>> user2Stream = frames.stream()
                .map(n -> leapMotionRecordFactory
                        .create(topicsProvider.getLeap_motion().getTopicname(), finalStatements.get(3).getActor().getName(), n, now + (now - (n.getTimestamp() / 1000))))
                .collect(Collectors.toList());
        for (int i = 0; i < user1Stream.size(); i++) {
            testDriver.pipeInput(user1Stream.get(i));
            testDriver.pipeInput(user2Stream.get(i));
        }
        List<ProducerRecord<String, GesturePerUserStats>> records = getResults();
        Assert.assertFalse(records.isEmpty());
        // key validation
        Assert.assertTrue(records.stream().map(n -> n.key()).distinct().collect(Collectors.toList()).contains(finalStatements.get(2).getActor().getName()));
        Assert.assertTrue(records.stream().map(n -> n.key()).distinct().collect(Collectors.toList()).contains(finalStatements.get(3).getActor().getName()));
        Assert.assertTrue(records.stream().map(n -> n.key()).distinct().collect(Collectors.toList()).size() == 2);
        // content validaton
        Assert.assertTrue(records.stream().map(n -> n.value().getAssessment() != null).distinct().collect(Collectors.toList()).size() == 1);
        Assert.assertTrue(records.stream().map(n -> n.value().getAssessment() != null).distinct().collect(Collectors.toList()).get(0));

        Assert.assertTrue(records.stream().filter(n -> n.key().equals(finalStatements.get(2).getActor().getName())).filter(n -> n.value().getCircles().equals(0)).count() > 0);
        Assert.assertTrue(records.stream().filter(n -> n.key().equals(finalStatements.get(2).getActor().getName())).anyMatch(n -> n.value().getCircles().equals(1)));
        Assert.assertTrue(records.stream().filter(n -> n.key().equals(finalStatements.get(2).getActor().getName())).anyMatch(n -> n.value().getCircles().equals(2)));
        Assert.assertTrue(records.stream().filter(n -> n.key().equals(finalStatements.get(2).getActor().getName())).anyMatch(n -> n.value().getCircles().equals(3)));
        Assert.assertFalse(records.stream().filter(n -> n.key().equals(finalStatements.get(2).getActor().getName())).anyMatch(n -> n.value().getCircles().compareTo(4) > 0));

        Assert.assertTrue(records.stream().filter(n -> n.key().equals(finalStatements.get(3).getActor().getName())).filter(n -> n.value().getCircles().equals(0)).count() > 0);
        Assert.assertTrue(records.stream().filter(n -> n.key().equals(finalStatements.get(3).getActor().getName())).anyMatch(n -> n.value().getCircles().equals(1)));
        Assert.assertTrue(records.stream().filter(n -> n.key().equals(finalStatements.get(3).getActor().getName())).anyMatch(n -> n.value().getCircles().equals(2)));
        Assert.assertTrue(records.stream().filter(n -> n.key().equals(finalStatements.get(3).getActor().getName())).anyMatch(n -> n.value().getCircles().equals(3)));
        Assert.assertFalse(records.stream().filter(n -> n.key().equals(finalStatements.get(3).getActor().getName())).anyMatch(n -> n.value().getCircles().compareTo(4) > 0));
    }


    /**
     * retrieve ProducerRecords, i.e. results from the assessment analytics topic as long as they arrive
     *
     * @return a list of ProducerRecords
     */
    private List<ProducerRecord<String, GesturePerUserStats>> getResults() {
        List<ProducerRecord<String, GesturePerUserStats>> records = new ArrayList<>();
        records.add(testDriver.readOutput(topicsProvider.getAssessment_analytics().getTopicname(), Serdes.String().deserializer(),
                new JsonSerde<>(GesturePerUserStats.class).deserializer()));
        while (records.get(records.size() - 1) != null) {
            records.add(testDriver.readOutput(topicsProvider.getAssessment_analytics().getTopicname(), Serdes.String().deserializer(),
                    new JsonSerde<>(GesturePerUserStats.class).deserializer()));
        }
        return records.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private void verifyAggregatingResult(GesturePerUserStats value, Integer expectedCircles, Integer expectedKeyTaps, Integer expectedScreenTaps, Integer expectedSwipes) {
        Assert.assertEquals(expectedCircles, value.getCircles());
        Assert.assertEquals( expectedKeyTaps, value.getKeyTaps());
        Assert.assertEquals(expectedScreenTaps, value.getScreenTaps());
        Assert.assertEquals(expectedSwipes, value.getSwipes());
    }

    private Statement getValidAssessmentStatement() {
        final Statement statement = new Statement();
        statement.setId(UUID.randomUUID());
        final Agent actor = new Agent();
        actor.setName("user");
        statement.setActor(actor);
        return statement;
    }

    private LeapMotionFrame getValidCircleGesture(long id) {
        final LeapMotionFrame leapMotionFrame = new LeapMotionFrame();

        leapMotionFrame.setTimestamp(LocalDateTime.now().minusMinutes(2L).toEpochSecond(ZoneOffset.UTC));

        final ArrayList<LeapMotionFrame.Gesture> gestures = new ArrayList<>();
        final LeapMotionFrame.CircleGesture circle = new LeapMotionFrame.CircleGesture();
        circle.setId(id);
        circle.setDuration(100L);
        circle.setType("circle");
        gestures.add(circle);
        leapMotionFrame.setGestures(gestures);
        return leapMotionFrame;
    }

    private LeapMotionFrame getValidSwipeGesture(long id) {
        final LeapMotionFrame leapMotionFrame = new LeapMotionFrame();
        leapMotionFrame.setTimestamp(LocalDateTime.now().minusMinutes(2L).toEpochSecond(ZoneOffset.UTC));

        final ArrayList<LeapMotionFrame.Gesture> gestures = new ArrayList<>();
        final LeapMotionFrame.SwipeGesture swipe = new LeapMotionFrame.SwipeGesture();
        swipe.setId(id);
        swipe.setDuration(400L);
        swipe.setType("swipe");
        gestures.add(swipe);
        leapMotionFrame.setGestures(gestures);
        return leapMotionFrame;
    }

}
