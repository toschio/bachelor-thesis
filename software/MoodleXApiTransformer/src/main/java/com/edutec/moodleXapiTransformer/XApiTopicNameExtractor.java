package com.edutec.moodleXapiTransformer;

import com.edutec.moodleXapiTransformer.models.xapimodels.Statement;
import com.edutec.moodleXapiTransformer.props.TopicsConfigs;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.RecordContext;
import org.apache.kafka.streams.processor.TopicNameExtractor;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Service;

/**
 * provides topic names for the publication of streams based on xapi verbs in xapi statements
 */
@Service
@RequiredArgsConstructor
public class XApiTopicNameExtractor implements TopicNameExtractor<String, Statement> {

    private final Log logger = LogFactory.getLog(getClass());

    private static final Predicate<? super String, ? super Statement> IS_DISCUSSION = (key, value) -> value.getVerb().getId().equals(XApiFactory.VerbProviderAndFactory.posted().getId())
            || value.getVerb().getId().equals(XApiFactory.VerbProviderAndFactory.replied().getId());
    private static final Predicate<? super String, ? super Statement> IS_ASSESSMENT = (key, value) -> value.getVerb().getId().equals(XApiFactory.VerbProviderAndFactory.accessAssessment().getId())
            || value.getVerb().getId().equals(XApiFactory.VerbProviderAndFactory.submitAssessment().getId());
    private final TopicsConfigs.TopicsProvider topicNameProvider;

    @Override
    public String extract(String key, Statement value, RecordContext recordContext) {
        if (IS_DISCUSSION.test(key, value)) {
            return topicNameProvider.getXapi_statement_discussion().getTopicname();
        }
        if (IS_ASSESSMENT.test(key, value)) {
            return topicNameProvider.getXapi_statement_assessment().getTopicname();
        }
        logger.error("extraction failed");
        return "error"; // todo create error handling topic
    }

    public Produced<String, Statement> getStringStatementSerdes() {
        return Produced.with(Serdes.String(), new JsonSerde<>(Statement.class));
    }
}
