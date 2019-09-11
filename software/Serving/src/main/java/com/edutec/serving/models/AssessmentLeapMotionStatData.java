package com.edutec.serving.models;

import com.edutec.serving.models.xapimodels.Statement;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.LongSummaryStatistics;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssessmentLeapMotionStatData {
    private Integer circles = 0;
    private Integer keyTaps = 0;
    private Integer swipes = 0;
    private Integer screenTaps = 0;

    private LongSummaryStatistics circleDuration = new LongSummaryStatistics(0, 0, 0, 0);
    private LongSummaryStatistics keyTapDuration = new LongSummaryStatistics(0, 0, 0, 0);
    private LongSummaryStatistics swipeDuration = new LongSummaryStatistics(0, 0, 0, 0);
    private LongSummaryStatistics screenTapDuration = new LongSummaryStatistics(0, 0, 0, 0);

    private Statement assessment;
}
