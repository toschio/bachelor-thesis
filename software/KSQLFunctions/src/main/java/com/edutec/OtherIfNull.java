package com.edutec;

import com.edutec.models.CdcWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.ksql.function.udf.Udf;
import io.confluent.ksql.function.udf.UdfDescription;
import io.confluent.ksql.function.udf.UdfParameter;

import java.io.IOException;

@UdfDescription(
        name = "otherIfNull",
        description = "First if notnull, otherwise other",
        version = "0.1",
        author = "Tonio Schwind"
)
public class OtherIfNull {

    @Udf(description = "First if notnull, otherwise other")
    public Long selectIfNotNullLong(@UdfParameter(value = "first", description = "the first value, returned if notnull") final Long first,
                                           @UdfParameter(value = "other", description = "the other value, returned if first is null") final Long other) {
        return first == null ? other : first;
    }

    @Udf(description = "First if notnull, otherwise other")
    public String selectIfNotNullString(@UdfParameter(value = "first", description = "the first value, returned if notnull") final String first,
                                             @UdfParameter(value = "other", description = "the other value, returned if first is null") final String other) {
        return first == null ? other : first;
    }

    @Udf(description = "First if notnull, otherwise other")
    public Integer selectIfNotNullInteger(@UdfParameter(value = "first", description = "the first value, returned if notnull") final Integer first,
                                             @UdfParameter(value = "other", description = "the other value, returned if first is null") final Integer other) {
        return first == null ? other : first;
    }
}
