package com.edutec.AssessmentEvaluator.models;

import com.edutec.AssessmentEvaluator.models.xapimodels.Statement;
import lombok.Data;

import java.util.LongSummaryStatistics;

@Data
public class GesturePerUserStats {

     Integer circles = 0;
     Integer keyTaps = 0;
     Integer swipes = 0;
     Integer screenTaps = 0;

     LongSummaryStatistics circleDuration = new LongSummaryStatistics(0, 0, 0, 0);
     LongSummaryStatistics keyTapDuration = new LongSummaryStatistics(0, 0, 0, 0);
     LongSummaryStatistics swipeDuration = new LongSummaryStatistics(0, 0, 0, 0);
     LongSummaryStatistics screenTapDuration = new LongSummaryStatistics(0, 0, 0, 0);

    private Statement assessment;

    public GesturePerUserStats with(Statement statement) {
        this.assessment = statement;
        return this;
    }
}
