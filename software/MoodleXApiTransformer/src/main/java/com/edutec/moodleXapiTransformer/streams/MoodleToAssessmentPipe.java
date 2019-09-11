package com.edutec.moodleXapiTransformer.streams;

import com.edutec.moodleXapiTransformer.XApiFactory;
import com.edutec.moodleXapiTransformer.XApiTopicNameExtractor;
import com.edutec.moodleXapiTransformer.models.Moodle;
import com.edutec.moodleXapiTransformer.models.StatementDataWrapper;
import com.edutec.moodleXapiTransformer.props.TopicsConfigs;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class MoodleToAssessmentPipe {

    private final Log logger = LogFactory.getLog(getClass());
    // service to construct a statement
    private final XApiFactory xApiFactory;
    private final TopicsConfigs.TopicsProvider topicNameProvider;
    // service extracting topics according to xapi statement's verbs
    private final XApiTopicNameExtractor xApiTopicNameExtractor;

    @Bean
    public KStream<?, ?> constructToAssessmentStatement(
            StreamsBuilder streamsBuilder,
            KTable<Long, Moodle.MdlUser> keyedMoodleUser) {
        //
        // input streams
        //
        // moodle qziz attempts
        // that stream is unpacked by KSQL
        final KStream<String, JsonNode> moodleQuizAttemptsByUserKey = streamsBuilder.stream(
                topicNameProvider.getMoodle_quiz_attempts_source_unpacked_keyed_by_user().getTopicname(), Consumed.with(Serdes.String(), new JsonSerde<JsonNode>(JsonNode.class)));
        // moodle quiz tabled
        // that stream is unpacked by KSQL
        final KTable<Long, Moodle.MdlQuiz> moodleQuiz = streamsBuilder.stream(
                topicNameProvider.getMoodle_quiz_source_unpacked().getTopicname())
                .map((key, value) -> {
                    try {
                        final Moodle.MdlQuiz quiz = new ObjectMapper().readValue(value.toString(), Moodle.MdlQuiz.class);
                        return KeyValue.pair(quiz.getId(), quiz);
                    } catch (IOException e) {
                        logger.error(e);
                        return KeyValue.pair(null, null);
                    }
                }).groupByKey(Serialized.with(Serdes.Long(), new JsonSerde<>(Moodle.MdlQuiz.class)))
                .reduce((o, n) -> n);


        // data operation - producing statements over quizes
        // concerning Moodle Quizes and publishing of Statements
        moodleQuizAttemptsByUserKey.map((String key, JsonNode value) -> {
            try {
                final Moodle.MdlQuizAttempt quizAttempt = new ObjectMapper().readValue(value.toString(), Moodle.MdlQuizAttempt.class);
                return KeyValue.pair(quizAttempt.getUserId(), quizAttempt);
            } catch (IOException e) {
                logger.error(e);
                return KeyValue.pair(null, null);
            }
        })
                //
                // join with Moodle User
                // Moodle Quiz Attempts are keyed by userid by KSQL, so user can be joined
                .filter(StandardStreamsOperations.KEYANDVALUENOTNULL)
                .mapValues(value -> StatementDataWrapper.builder().mdlQuizAttempt(value).build())
                .join(keyedMoodleUser, StatementDataWrapper::setMdlUser,
                        Joined.with(Serdes.Long(), new JsonSerde<>(StatementDataWrapper.class), new JsonSerde<>(Moodle.MdlUser.class)))
                // select quiz id as key to join quiz
                .selectKey((key, value) -> value.getMdlQuizAttempt().getQuizid())
                //
                // join with Moodle Quiz
                .join(moodleQuiz, StatementDataWrapper::setMdlQuiz,
                        Joined.with(Serdes.Long(), new JsonSerde<>(StatementDataWrapper.class), new JsonSerde<>(Moodle.MdlQuiz.class)))
                //
                // build the XApiStatement
                //
                .map((k, v) -> xApiFactory.constructStatement(v))
                // publish the statement
                .filter(StandardStreamsOperations.KEYANDVALUENOTNULL)
                .to(xApiTopicNameExtractor, xApiTopicNameExtractor.getStringStatementSerdes());

        return moodleQuizAttemptsByUserKey;
    }
}
