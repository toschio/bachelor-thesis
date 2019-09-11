package com.edutec.moodleXapiTransformer.streams;

import com.edutec.moodleXapiTransformer.CdcUnpackService;
import com.edutec.moodleXapiTransformer.DateService;
import com.edutec.moodleXapiTransformer.XApiFactory;
import com.edutec.moodleXapiTransformer.XApiTopicNameExtractor;
import com.edutec.moodleXapiTransformer.models.Moodle;
import com.edutec.moodleXapiTransformer.models.xapimodels.Statement;
import com.edutec.moodleXapiTransformer.props.TopicsConfigs;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.record.TimestampType;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.kafka.clients.consumer.ConsumerRecord.NULL_CHECKSUM;

@EnableKafkaStreams
public class MoodleToAssessmentPipeTests {

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
    private ConsumerRecordFactory<?, JsonNode> userJsonConsumerRecordFactory;
    private ConsumerRecordFactory<?, CdcWrapper> forumConsumerRecordFactory;
    private ConsumerRecordFactory<?, CdcWrapper> forumPostConsumerRecordFactory;
    private ConsumerRecordFactory<Long, Moodle.MdlQuiz> quizRecordFactory;
    private ConsumerRecordFactory<String, Moodle.MdlQuizAttempt> quizAttemtRecordFactory;
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
        quizRecordFactory = new ConsumerRecordFactory<Long, Moodle.MdlQuiz>(
                mdlQuizUnpackedSource.getTopicname(), Serdes.Long().serializer(), new JsonSerde<Moodle.MdlQuiz>(Moodle.MdlQuiz.class).serializer());
        quizAttemtRecordFactory = new ConsumerRecordFactory<String, Moodle.MdlQuizAttempt>(
                mdlQuizAttemptKeyedByUserSource.getTopicname(), Serdes.String().serializer(),
                new JsonSerde<Moodle.MdlQuizAttempt>(Moodle.MdlQuizAttempt.class).serializer());
        testDriver = new TopologyTestDriver(topology, config);
    }


    @After()
    public void close() throws Exception {
        testDriver.close();
    }

    /**
     * build an assessment from user, quiz and quizattempt
     *
     * @throws Exception
     */
    @Test
    @Ignore
    public void shouldBuildAnAssessmentStatement() throws Exception {
        final CdcWrapper<Moodle.MdlUser> user = CdcWrapper.AFTER_MDL_USER();
        final Moodle.MdlQuiz quiz = new Moodle.MdlQuiz();
        quiz.setId(3L);
        final Moodle.MdlQuizAttempt quizAttempt = new Moodle.MdlQuizAttempt();
        quizAttempt.setSelf_id(2L);
        quizAttempt.setId(user.getAfter().getId());
        quizAttempt.setQuizid(3L);
        quizAttempt.setTimemodified(012345L);
        final List<ConsumerRecord<byte[], byte[]>> records = new ArrayList<>(5);
        records.add(userConsumerRecordFactory.create(mdlUserSource.getTopicname(), user));
        records.add(quizRecordFactory.create(mdlQuizUnpackedSource.getTopicname(), quiz));
        records.add(quizAttemtRecordFactory.create(mdlQuizAttemptKeyedByUserSource.getTopicname(), quizAttempt.getUserId().toString(), quizAttempt));
        testDriver.pipeInput(records);
        final ProducerRecord<String, Statement> assessmentRecordResult = testDriver.readOutput(
                statementassessment.getTopicname(), Serdes.String().deserializer(), new JsonSerde<>(Statement.class).deserializer());
        final ProducerRecord<String, Statement> assessmentRecordResult1 = testDriver.readOutput(
                statementassessment.getTopicname(), Serdes.String().deserializer(), new JsonSerde<>(Statement.class).deserializer());
        Assert.assertNotNull(assessmentRecordResult);
        Assert.assertNull(assessmentRecordResult1);

        // check message key
        String name = "gmdlquiz" + quiz.getId();
        byte[] bytes = name.getBytes();
        Assert.assertEquals(UUID.nameUUIDFromBytes(bytes).toString(), assessmentRecordResult.key());
        // check message value
        final Statement value = assessmentRecordResult.value();
        Assert.assertEquals(UUID.nameUUIDFromBytes(bytes).toString(), value.getId().toString());
        Assert.assertEquals(user.getAfter().getFirstname() + " " + user.getAfter().getLastname(), value.getActor().getName());
        Assert.assertEquals(XApiFactory.VerbProviderAndFactory.submitAssessment().getId(), value.getVerb().getId());
    }


    /**
     * testing with json directly from stream
     */
    @Test
    public void shouldCalculateAssessmentStatement() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Moodle Users from json file, representing an original stream from the database
        JsonNode moodle_user = null;
        try {
            TypeReference typeReference = new TypeReference<JsonNode>() {
            };
            Resource resource = new ClassPathResource("moodle_user.json");
            File file = resource.getFile();
            moodle_user = mapper.readValue(file, typeReference);
        } catch (IOException e) {
            System.out.println("Unable to save users: " + e.getMessage());
            throw new RuntimeException("resource read failed");
        }
        for (int i = 0; i < moodle_user.size(); i++) {
            final JsonNode jsonNode = moodle_user.get(i);
            final byte[] bytes = mapper.writeValueAsBytes(jsonNode);
            testDriver.pipeInput(new ConsumerRecord<byte[], byte[]>(topicsProvider.getMoodle_user_source().getTopicname(),
                    1, 0L, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC), TimestampType.LOG_APPEND_TIME, (long) NULL_CHECKSUM,
                    0, bytes.length, null, bytes));
        }
        // Moodle quiz source from json, representing stream from KSQL
        List<Moodle.MdlQuiz> moodle_quizes = null;
        try {
            TypeReference typeReference = new TypeReference<List<Moodle.MdlQuiz>>() {
            };
            Resource resource = new ClassPathResource("moodle_quiz_source_unpacked.json");
            File file = resource.getFile();
            moodle_quizes = mapper.readValue(file, typeReference);
        } catch (IOException e) {
            System.out.println("Unable to save users: " + e.getMessage());
            throw new RuntimeException("resource read failed");
        }
        testDriver.pipeInput(moodle_quizes
                .stream()
                .map(mdlquiz -> quizRecordFactory.create(topicsProvider.getMoodle_quiz_source_unpacked().getTopicname(), mdlquiz.getId(), mdlquiz))
                .collect(Collectors.toList()));
        // Moodle quiz attempts from json, represeting stream from KSQL, keyed by user
        List<Moodle.MdlQuizAttempt> moodle_quiz_attempts = null;
        try {
            TypeReference typeReference = new TypeReference<List<Moodle.MdlQuizAttempt>>() {
            };
            Resource resource = new ClassPathResource("quiz_attempts_keyed_by_userunpacked.json");
            File file = resource.getFile();
            moodle_quiz_attempts = mapper.readValue(file, typeReference);
        } catch (IOException e) {
            System.out.println("Unable to save users: " + e.getMessage());
            throw new RuntimeException("resource read failed");
        }
        testDriver.pipeInput(moodle_quiz_attempts
                .stream()
                .map(next -> quizAttemtRecordFactory.create(topicsProvider.getMoodle_quiz_attempts_source_unpacked_keyed_by_user().getTopicname(),
                        next.getUserId().toString(), next))
                .collect(Collectors.toList()));
        // validate output
        final List<ProducerRecord<String, Statement>> results = getResults();
        Assert.assertTrue(results.size() == 10);
        Assert.assertTrue(results.stream().map(ProducerRecord::key).distinct().collect(Collectors.toList()).size() == 10);
        Assert.assertTrue(results.stream()
                .map(n -> n.value().getActor().getName()).distinct().collect(Collectors.toList()).size() == 10);

    }


    /**
     * initializes and mocks mocked services and providers
     */
    private void initMocks() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(topicsProvider.getMoodle_user_source()).thenReturn(mdlUserSource);
        Mockito.when(topicsProvider.getMoodle_quiz_attempts_source_unpacked_keyed_by_user()).thenReturn(mdlQuizAttemptKeyedByUserSource);
        Mockito.when(topicsProvider.getMoodle_quiz_source_unpacked()).thenReturn(mdlQuizUnpackedSource);
        Mockito.when(topicsProvider.getMoodle_user_source_unpacked()).thenReturn(mdlUserUnpacked);
        Mockito.when(topicsProvider.getXapi_statement_assessment()).thenReturn(statementassessment);
        Mockito.when(storeNameProvider.getMdl_user_source_unpacked_store()).thenReturn(USER_STORE);
    }


    private List<ProducerRecord<String, Statement>> getResults() {
        List<ProducerRecord<String, Statement>> records = new ArrayList<>();
        records.add(testDriver.readOutput(topicsProvider.getXapi_statement_assessment().getTopicname(), Serdes.String().deserializer(),
                new JsonSerde<>(Statement.class).deserializer()));
        while (records.get(records.size() - 1) != null) {
            records.add(testDriver.readOutput(topicsProvider.getXapi_statement_assessment().getTopicname(), Serdes.String().deserializer(),
                    new JsonSerde<>(Statement.class).deserializer()));
        }
        return records.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
}
