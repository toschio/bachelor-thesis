package com.edutec.moodleXapiTransformer.streams;


import com.edutec.moodleXapiTransformer.CdcUnpackService;
import com.edutec.moodleXapiTransformer.DateService;
import com.edutec.moodleXapiTransformer.XApiFactory;
import com.edutec.moodleXapiTransformer.XApiTopicNameExtractor;
import com.edutec.moodleXapiTransformer.models.Moodle;
import com.edutec.moodleXapiTransformer.models.xapimodels.Statement;
import com.edutec.moodleXapiTransformer.props.TopicsConfigs;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@EnableKafkaStreams
public class MoodleToDiscussionPipeTests {

    private static final String FORUM_STORE = "forumStore";
    private static final String USER_STORE = "userStore";
    private final static TopicsConfigs.Topic mdlForumPostSource = TopicsConfigs.Topic.of("mdlforumpost");
    private final static TopicsConfigs.Topic mdlForumSource = TopicsConfigs.Topic.of("mdlforumsource");
    private final static TopicsConfigs.Topic mdlUserSource = TopicsConfigs.Topic.of("mdlusersource");
    private final static TopicsConfigs.Topic mdlQuizAttemptKeyedByUserSource = TopicsConfigs.Topic.of("mdlquizattemptkeyedbyuser");
    private final static TopicsConfigs.Topic mdlQuizUnpackedSource = TopicsConfigs.Topic.of("mdlquiz");
    private final static TopicsConfigs.Topic mdlUserUnpacked = TopicsConfigs.Topic.of("mdluserunpacked");
    private final static TopicsConfigs.Topic statementdiscussion = TopicsConfigs.Topic.of("discussion");
    private final static TopicsConfigs.Topic statementassessment = TopicsConfigs.Topic.of("assessment");
    private TopologyTestDriver testDriver;
    private ConsumerRecordFactory<?, CdcWrapper> userConsumerRecordFactory;
    private ConsumerRecordFactory<?, CdcWrapper> forumConsumerRecordFactory;
    private ConsumerRecordFactory<?, CdcWrapper> forumPostConsumerRecordFactory;
    private ConsumerRecordFactory<Long, Moodle.MdlQuiz> quizRecordFactory;
    private ConsumerRecordFactory<Long, Moodle.MdlQuizAttempt> quizAttemtRecordFactory;
    @Mock
    private TopicsConfigs.TopicsProvider topicsProvider;
    @Mock
    private TopicsConfigs.StoreNameProvider storeNameProvider;
    private CdcUnpackService unpackService = new CdcUnpackService();
    private XApiFactory xapiFactory = new XApiFactory(new DateService());
    private XApiTopicNameExtractor topicExtractor;

    @Before
    public void setup() throws Exception {
        initMocks();
        topicExtractor = new XApiTopicNameExtractor(topicsProvider);
        // build topology
        final StreamsBuilder builder = new StreamsBuilder();

        final KTable<Long, Moodle.MdlUser> mdlUserKTable = new MoodleUserCdcUnpackPipe(unpackService, topicsProvider, storeNameProvider).mdlUserToAgentStream(builder);
        final KTable<Long, Moodle.MdlForum> mdlForumKTable = new MoodleForumCdcUnpackPipe(unpackService, topicsProvider, storeNameProvider).unpackMoodleForumCdcWrapper(builder);
        new MoodleToDiscussionPipe(unpackService, xapiFactory, topicsProvider, topicExtractor).constructDisussionStatement(builder, mdlUserKTable, mdlForumKTable);
        new MoodleToAssessmentPipe(xapiFactory, topicsProvider, topicExtractor).constructToAssessmentStatement(builder, mdlUserKTable);
        Topology topology = builder.build();

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, new JsonSerde<>(CdcWrapper.class).getClass());
        config.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, JsonNode.class);

        userConsumerRecordFactory = new ConsumerRecordFactory<String, CdcWrapper>(
                mdlUserSource.getTopicname(), Serdes.String().serializer(), new JsonSerde<CdcWrapper>().serializer());
        forumConsumerRecordFactory = new ConsumerRecordFactory<String, CdcWrapper>(
                mdlForumSource.getTopicname(), Serdes.String().serializer(), new JsonSerde<CdcWrapper>().serializer());
        forumPostConsumerRecordFactory = new ConsumerRecordFactory<String, CdcWrapper>(
                mdlForumPostSource.getTopicname(), Serdes.String().serializer(), new JsonSerde<CdcWrapper>().serializer());
        quizRecordFactory = new ConsumerRecordFactory<Long, Moodle.MdlQuiz>(
                mdlQuizUnpackedSource.getTopicname(), Serdes.Long().serializer(), new JsonSerde<Moodle.MdlQuiz>(Moodle.MdlQuiz.class).serializer());
        quizAttemtRecordFactory = new ConsumerRecordFactory<Long, Moodle.MdlQuizAttempt>(
                mdlQuizAttemptKeyedByUserSource.getTopicname(), Serdes.Long().serializer(), new JsonSerde<Moodle.MdlQuizAttempt>(Moodle.MdlQuizAttempt.class).serializer());
        testDriver = new TopologyTestDriver(topology, config);
    }


    @After()
    public void close() throws Exception {
        testDriver.close();
    }

    /**
     * build one statement from one moodle post, moodle forum and moodle user
     *
     * @throws Exception
     */
    @Test
    public void shouldBuildOneStatement() throws Exception {
        final CdcWrapper<Moodle.MdlUser> user = CdcWrapper.AFTER_MDL_USER();
        final CdcWrapper<Moodle.MdlForum> forum = CdcWrapper.AFTER_MDL_FORUM();
        final CdcWrapper<Moodle.MdlForumPost> forumPost = CdcWrapper.AFTER_MDL_FORUM_POST();
        testDriver.pipeInput(userConsumerRecordFactory.create(mdlUserSource.getTopicname(), user));
        testDriver.pipeInput(forumConsumerRecordFactory.create(mdlForumSource.getTopicname(), forum));
        testDriver.pipeInput(forumPostConsumerRecordFactory.create(mdlForumPostSource.getTopicname(), forumPost));

        final ProducerRecord<String, Statement> stringStatementProducerRecord = testDriver.readOutput(statementdiscussion.getTopicname(), Serdes.String().deserializer(), new JsonSerde<>(Statement.class).deserializer());
        final ProducerRecord<String, Statement> stringStatementProducerRecord2 = testDriver.readOutput(statementdiscussion.getTopicname(), Serdes.String().deserializer(), new JsonSerde<>(Statement.class).deserializer());
        Assert.assertNull(stringStatementProducerRecord2);
        Assert.assertNotNull(stringStatementProducerRecord);

        // check message key
        Assert.assertEquals(user.getAfter().getFirstname()  + " " + user.getAfter().getLastname(), stringStatementProducerRecord.key());
        // check message value
        final Statement value = stringStatementProducerRecord.value();
        Assert.assertEquals(user.getAfter().getFirstname() + " " + user.getAfter().getLastname(), value.getActor().getName());
        Assert.assertEquals(forumPost.getAfter().getMessage(), value.getResult().getResponse());
        Assert.assertEquals(forum.getAfter().getName(), value.getContext().getContextActivities().getCategory().get(0).getDefinition().getName().get("name"));
        Assert.assertEquals(XApiFactory.VerbProviderAndFactory.posted().getId(), value.getVerb().getId());
    }

    /**
     * build two statements from one user, one forum, two forumposts
     *
     * @throws Exception
     */
    @Test
    public void shouldBuildTwoStatement() throws Exception {
        final CdcWrapper<Moodle.MdlUser> user = CdcWrapper.AFTER_MDL_USER();
        final CdcWrapper<Moodle.MdlForum> forum = CdcWrapper.AFTER_MDL_FORUM();
        final CdcWrapper<Moodle.MdlForumPost> forumPost = CdcWrapper.AFTER_MDL_FORUM_POST();
        final CdcWrapper<Moodle.MdlForumPost> forumPost1 = CdcWrapper.AFTER_MDL_FORUM_POST();
        forumPost1.getAfter().setId(2L);
        forumPost1.getAfter().setParent(forumPost.getAfter().getId());

        testDriver.pipeInput(userConsumerRecordFactory.create(mdlUserSource.getTopicname(), user));
        testDriver.pipeInput(forumConsumerRecordFactory.create(mdlForumSource.getTopicname(), forum));
        testDriver.pipeInput(forumPostConsumerRecordFactory.create(mdlForumPostSource.getTopicname(), forumPost));
        testDriver.pipeInput(forumPostConsumerRecordFactory.create(mdlForumPostSource.getTopicname(), forumPost1));

        final ProducerRecord<String, Statement> statementProducerRecord = testDriver.readOutput(statementdiscussion.getTopicname(), Serdes.String().deserializer(), new JsonSerde<>(Statement.class).deserializer());
        final ProducerRecord<String, Statement> statementProducerRecord1 = testDriver.readOutput(statementdiscussion.getTopicname(), Serdes.String().deserializer(), new JsonSerde<>(Statement.class).deserializer());
        final ProducerRecord<String, Statement> statementProducerRecord2 = testDriver.readOutput(statementdiscussion.getTopicname(), Serdes.String().deserializer(), new JsonSerde<>(Statement.class).deserializer());
        Assert.assertNull(statementProducerRecord2);
        Assert.assertNotNull(statementProducerRecord);
        Assert.assertNotNull(statementProducerRecord1);

        // check message keys
        Assert.assertEquals(user.getAfter().getFirstname()  + " " + user.getAfter().getLastname(), statementProducerRecord.key());
        Assert.assertEquals(user.getAfter().getFirstname()  + " " + user.getAfter().getLastname(), statementProducerRecord1.key());


        // check message values
        final Statement value = statementProducerRecord.value();
        Assert.assertEquals(user.getAfter().getFirstname() + " " + user.getAfter().getLastname(), value.getActor().getName());
        Assert.assertEquals(forumPost.getAfter().getMessage(), value.getResult().getResponse());
        Assert.assertEquals(forum.getAfter().getName(), value.getContext().getContextActivities().getCategory().get(0).getDefinition().getName().get("name"));
        Assert.assertEquals(XApiFactory.VerbProviderAndFactory.posted().getId(), value.getVerb().getId());

        final Statement value1 = statementProducerRecord1.value();
        Assert.assertEquals(user.getAfter().getFirstname() + " " + user.getAfter().getLastname(), value1.getActor().getName());
        Assert.assertEquals(forumPost.getAfter().getMessage(), value1.getResult().getResponse());
        Assert.assertEquals(forum.getAfter().getName(), value1.getContext().getContextActivities().getCategory().get(0).getDefinition().getName().get("name"));
        Assert.assertEquals(XApiFactory.VerbProviderAndFactory.replied().getId(), value1.getVerb().getId());
    }

    /**
     * build two statements from one user, one forum, two forumposts and make a change to the user object afterwards as well as to a forum post
     * since we make a join based on the forumpost stream we expect to see no change unless we trigger new forumposts
     *
     * @throws Exception
     */
    @Test
    public void shouldBuildTwoStatementWithChangeToUserAfterwards() throws Exception {
        final CdcWrapper<Moodle.MdlUser> user = CdcWrapper.AFTER_MDL_USER();
        final CdcWrapper<Moodle.MdlForum> forum = CdcWrapper.AFTER_MDL_FORUM();
        final CdcWrapper<Moodle.MdlForumPost> forumPost = CdcWrapper.AFTER_MDL_FORUM_POST();
        final CdcWrapper<Moodle.MdlForumPost> forumPost1 = CdcWrapper.AFTER_MDL_FORUM_POST();
        forumPost1.getAfter().setId(2L);
        forumPost1.getAfter().setParent(forumPost.getAfter().getId());

        final CdcWrapper<Moodle.MdlUser> user1 = CdcWrapper.AFTER_MDL_USER();
        user1.getAfter().setFirstname("another name");

        final CdcWrapper<Moodle.MdlForumPost> forumPost2 = CdcWrapper.AFTER_MDL_FORUM_POST();
        forumPost2.getAfter().setId(2L);
        forumPost2.getAfter().setParent(forumPost.getAfter().getId());
        forumPost2.getAfter().setMessage("another message than bevore");

        final List<ConsumerRecord<byte[], byte[]>> records = new ArrayList<>(5);
        records.add(userConsumerRecordFactory.create(mdlUserSource.getTopicname(), user));
        records.add(forumConsumerRecordFactory.create(mdlForumSource.getTopicname(), forum));
        records.add(forumPostConsumerRecordFactory.create(mdlForumPostSource.getTopicname(), forumPost));
        records.add(forumPostConsumerRecordFactory.create(mdlForumPostSource.getTopicname(), forumPost1));
        records.add(userConsumerRecordFactory.create(mdlUserSource.getTopicname(), user1)); // make change to user
        records.add(forumPostConsumerRecordFactory.create(mdlForumPostSource.getTopicname(), forumPost2)); // make change to second forum post
        testDriver.pipeInput(records); // publish

        final ProducerRecord<String, Statement> statementProducerRecord = testDriver.readOutput(statementdiscussion.getTopicname(), Serdes.String().deserializer(), new JsonSerde<>(Statement.class).deserializer());
        final ProducerRecord<String, Statement> statementProducerRecord1 = testDriver.readOutput(statementdiscussion.getTopicname(), Serdes.String().deserializer(), new JsonSerde<>(Statement.class).deserializer());
        final ProducerRecord<String, Statement> statementProducerRecord2 = testDriver.readOutput(statementdiscussion.getTopicname(), Serdes.String().deserializer(), new JsonSerde<>(Statement.class).deserializer());
        final ProducerRecord<String, Statement> statementProducerRecord3 = testDriver.readOutput(statementdiscussion.getTopicname(), Serdes.String().deserializer(), new JsonSerde<>(Statement.class).deserializer());
        Assert.assertNotNull(statementProducerRecord);
        Assert.assertNotNull(statementProducerRecord1);
        Assert.assertNotNull(statementProducerRecord2);
        Assert.assertNull(statementProducerRecord3);

        // check message keys
        Assert.assertEquals(user.getAfter().getFirstname()  + " " + user.getAfter().getLastname(), statementProducerRecord.key());
        Assert.assertEquals(user.getAfter().getFirstname()  + " " + user1.getAfter().getLastname(), statementProducerRecord1.key());
        Assert.assertEquals(user1.getAfter().getFirstname()  + " " + user1.getAfter().getLastname(), statementProducerRecord2.key());

        // check message values
        final Statement value = statementProducerRecord.value();
        Assert.assertEquals(user.getAfter().getFirstname() + " " + user.getAfter().getLastname(), value.getActor().getName());
        Assert.assertEquals(forumPost.getAfter().getMessage(), value.getResult().getResponse());
        Assert.assertEquals(forum.getAfter().getName(), value.getContext().getContextActivities().getCategory().get(0).getDefinition().getName().get("name"));
        Assert.assertEquals(XApiFactory.VerbProviderAndFactory.posted().getId(), value.getVerb().getId());

        final Statement value1 = statementProducerRecord1.value();
        Assert.assertEquals(user.getAfter().getFirstname() + " " + user.getAfter().getLastname(), value1.getActor().getName());
        Assert.assertEquals(forumPost1.getAfter().getMessage(), value1.getResult().getResponse());
        Assert.assertEquals(forum.getAfter().getName(), value1.getContext().getContextActivities().getCategory().get(0).getDefinition().getName().get("name"));
        Assert.assertEquals(XApiFactory.VerbProviderAndFactory.replied().getId(), value1.getVerb().getId());

        // here both values derived from user and forum post should be changed
        final Statement value2 = statementProducerRecord2.value();
        Assert.assertEquals(user1.getAfter().getFirstname() + " " + user1.getAfter().getLastname(), value2.getActor().getName()); // other actor values
        Assert.assertEquals(forumPost2.getAfter().getMessage(), value2.getResult().getResponse()); // other message values
        Assert.assertEquals(forum.getAfter().getName(), value2.getContext().getContextActivities().getCategory().get(0).getDefinition().getName().get("name"));
        Assert.assertEquals(XApiFactory.VerbProviderAndFactory.replied().getId(), value2.getVerb().getId());
    }


    /**
     * build an assessment from user, quiz and quizattempt and build a discussion from that user, forum and forumpost
     *
     * @throws Exception
     */
    @Test
    @Ignore
    public void shouldBuildAnAssessmentStatementAndADiscussionStatement() throws Exception {
        final CdcWrapper<Moodle.MdlUser> user = CdcWrapper.AFTER_MDL_USER();
        // assessment data
        final Moodle.MdlQuiz quiz = new Moodle.MdlQuiz();
        quiz.setId(3L);
        final Moodle.MdlQuizAttempt quizAttempt = new Moodle.MdlQuizAttempt();
        quizAttempt.setSelf_id(2L);
        quizAttempt.setId(1L);
        quizAttempt.setQuizid(3L);
        quizAttempt.setTimemodified(012345L);
        // forum data
        final CdcWrapper<Moodle.MdlForum> forum = CdcWrapper.AFTER_MDL_FORUM();
        final CdcWrapper<Moodle.MdlForumPost> forumPost = CdcWrapper.AFTER_MDL_FORUM_POST();

        final List<ConsumerRecord<byte[], byte[]>> records = new ArrayList<>(5);
        records.add(userConsumerRecordFactory.create(mdlUserSource.getTopicname(), user));
        records.add(quizRecordFactory.create(mdlQuizUnpackedSource.getTopicname(), quiz));
        records.add(forumConsumerRecordFactory.create(mdlForumSource.getTopicname(), forum));
        records.add(quizAttemtRecordFactory.create(mdlQuizAttemptKeyedByUserSource.getTopicname(), quizAttempt));
        records.add(forumPostConsumerRecordFactory.create(mdlForumPostSource.getTopicname(), forumPost));
        testDriver.pipeInput(records);
        final ProducerRecord<String, Statement> assessmentRecordResult = testDriver.readOutput(
                statementassessment.getTopicname(), Serdes.String().deserializer(), new JsonSerde<>(Statement.class).deserializer());
        final ProducerRecord<String, Statement> assessmentRecordResult1 = testDriver.readOutput(
                statementassessment.getTopicname(), Serdes.String().deserializer(), new JsonSerde<>(Statement.class).deserializer());
        final ProducerRecord<String, Statement> discussionRecordResult = testDriver.readOutput(
                statementdiscussion.getTopicname(), Serdes.String().deserializer(), new JsonSerde<>(Statement.class).deserializer());
        final ProducerRecord<String, Statement> discussionRecordResult1 = testDriver.readOutput(
                statementdiscussion.getTopicname(), Serdes.String().deserializer(), new JsonSerde<>(Statement.class).deserializer());
        Assert.assertNotNull(assessmentRecordResult);
        Assert.assertNull(assessmentRecordResult1);
        Assert.assertNotNull(discussionRecordResult);
        Assert.assertNull(discussionRecordResult1);
    }


    /**
     * initializes and mocks mocked services and providers
     */
    private void initMocks() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(topicsProvider.getMoodle_forum_posts_source()).thenReturn(mdlForumPostSource);
        Mockito.when(topicsProvider.getMoodle_forum_source()).thenReturn(mdlForumSource);
        Mockito.when(topicsProvider.getMoodle_user_source()).thenReturn(mdlUserSource);
        Mockito.when(topicsProvider.getMoodle_quiz_attempts_source_unpacked_keyed_by_user()).thenReturn(mdlQuizAttemptKeyedByUserSource);
        Mockito.when(topicsProvider.getMoodle_quiz_source_unpacked()).thenReturn(mdlQuizUnpackedSource);
        Mockito.when(topicsProvider.getMoodle_user_source_unpacked()).thenReturn(mdlUserUnpacked);
        Mockito.when(topicsProvider.getXapi_statement_discussion()).thenReturn(statementdiscussion);
        Mockito.when(topicsProvider.getXapi_statement_assessment()).thenReturn(statementassessment);
        Mockito.when(storeNameProvider.getMdl_forum_source_unpacked_store()).thenReturn(FORUM_STORE);
        Mockito.when(storeNameProvider.getMdl_user_source_unpacked_store()).thenReturn(USER_STORE);
    }
}
