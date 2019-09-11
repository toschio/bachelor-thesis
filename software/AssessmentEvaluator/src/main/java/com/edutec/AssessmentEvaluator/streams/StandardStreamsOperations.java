package com.edutec.AssessmentEvaluator.streams;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Windowed;


public class StandardStreamsOperations {

    /**
     * Filter predicate to filter key or message null
     */
    public static final Predicate<Object, Object> KEYANDVALUENOTNULL = (k, v) -> k != null && v != null;

    /**
     * KeyValueMapper to retrieve Key from Windowed<Key>
     * @param <U>
     * @param <V>
     * @return
     */
    public static <U, V> KeyValueMapper<Windowed<U>, V, KeyValue<U, V>> retrieveKeyFromWindowedKey() {
        return (key, value) -> KeyValue.pair(key.key(), value);
    }

}
