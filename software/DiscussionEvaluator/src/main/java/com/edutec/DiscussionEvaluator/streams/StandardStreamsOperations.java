package com.edutec.DiscussionEvaluator.streams;

import com.edutec.DiscussionEvaluator.models.xapimodels.Statement;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Predicate;


public class StandardStreamsOperations {

    public static final Predicate<Object, Object> KEYNOTNULL = (k, v) -> k != null;
    public static final Predicate<Object, Object> VALUENOTNULL = (k, v) -> v != null;
    public static final Predicate<Object, Object> KEYANDVALUENOTNULL = (k, v) -> KEYNOTNULL.test(k, v) && VALUENOTNULL.test(k, v);


    public static final KeyValueMapper<String, Statement, String> KEY_BY_ACTOR = (key, value) -> value.getActor().getName();

}
