package com.edutec.moodleXapiTransformer.models.xapimodels.json;

import com.fasterxml.jackson.datatype.joda.cfg.FormatConfig;
import com.fasterxml.jackson.datatype.joda.deser.DateTimeDeserializer;
import org.joda.time.DateTime;

public class MyDateTimeDeserializer extends DateTimeDeserializer {
    public MyDateTimeDeserializer() {
        super(DateTime.class, FormatConfig.DEFAULT_DATETIME_PARSER);
    }
}
