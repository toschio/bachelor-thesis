package com.edutec.serving.models.dtos;

import com.edutec.serving.models.AssessmentLeapMotionStatData;
import com.edutec.serving.models.xapimodels.Statement;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.LongSummaryStatistics;

@Data
@AllArgsConstructor
public class AssessmentLeapMotionStatDataDto {
    private final String userID;
    private final Integer circles;
    private final Integer keyTaps;
    private final Integer swipes;
    private final Integer screenTaps;

    private final LongSummaryStatistics circleDuration;
    private final LongSummaryStatistics keyTapDuration;
    private final LongSummaryStatistics swipeDuration;
    private final LongSummaryStatistics screenTapDuration;

    private final Statement assessment;


    public static AssessmentLeapMotionStatDataDto of(String key, AssessmentLeapMotionStatData value) {
        return new AssessmentLeapMotionStatDataDto(
                key, value.getCircles(), value.getKeyTaps(), value.getSwipes(), value.getScreenTaps(),
                value.getCircleDuration(), value.getKeyTapDuration(), value.getSwipeDuration(),
                value.getScreenTapDuration(), value.getAssessment()
        );
    }
}
