package com.edutec.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CdcWrapper {
    public CdcValue after;
    public CdcValue before;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CdcValue {
        public Long id; // BIGINT, course BIGINT, name STRING, intro STRING, introformat INTEGER, timeopen BIGINT, timeclose BIGINT, timelimit BIGINT, overduehandling STRING, graceperiod BIGINT, preferredbehavior STRING, canredoquestions INT, attempts INTEGER, attemptonlast BIGINT, grademethod INTEGER, decimalpoints DOUBLE, timecreated BIGINT, timemodified BIGINT
    }
}
