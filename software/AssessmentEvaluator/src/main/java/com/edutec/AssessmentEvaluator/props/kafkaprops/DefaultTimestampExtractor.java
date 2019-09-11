package com.edutec.AssessmentEvaluator.props.kafkaprops;

import com.edutec.AssessmentEvaluator.models.leapmotionmodels.LeapMotionFrame;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

/**
 * timestamp extractor to extract actual event time
 */
public class DefaultTimestampExtractor implements TimestampExtractor {
    private Log logger = LogFactory.getLog(DefaultTimestampExtractor.class);

    @Override
    public long extract(ConsumerRecord<Object, Object> record, long previousTimestamp) {
        try {
            if (record.value() instanceof LeapMotionFrame) {
                Long timestamp = ((LeapMotionFrame) record.value()).getTimestamp() / 1000; // to reach milliseconds
                return timestamp;
            }
            return record.timestamp();
        } catch (Exception e) {
            logger.error(e);
        }
        return 0;
    }
}
