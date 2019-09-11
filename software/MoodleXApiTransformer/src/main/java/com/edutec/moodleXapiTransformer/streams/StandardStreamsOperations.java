package com.edutec.moodleXapiTransformer.streams;

import org.apache.kafka.streams.kstream.Predicate;


public class StandardStreamsOperations {


    public static final Predicate<Object, Object> KEYNOTNULL = (k, v) -> k != null;
    public static final Predicate<Object, Object> VALUENOTNULL = (k, v) -> v != null;
    public static final Predicate<Object, Object> KEYANDVALUENOTNULL = (k, v) -> KEYNOTNULL.test(k, v) && VALUENOTNULL.test(k, v);

}

