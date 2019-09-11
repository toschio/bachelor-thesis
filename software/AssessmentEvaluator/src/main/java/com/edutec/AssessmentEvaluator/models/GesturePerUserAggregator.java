package com.edutec.AssessmentEvaluator.models;

import com.edutec.AssessmentEvaluator.models.leapmotionmodels.LeapMotionFrame;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Merger;

import java.util.HashMap;

@Data
public class GesturePerUserAggregator extends GesturePerUserStats {

    // used for keeping durations of same gesture types with different ids, which are not yet finished
    private HashMap<Long, Long> circleGesturesDuration = new HashMap<>();
    private HashMap<Long, Long> keyTapGesturesDuration = new HashMap<>();
    private HashMap<Long, Long> swipeGesturesDuration = new HashMap<>();
    private HashMap<Long, Long> screenGesturesDuration = new HashMap<>();

    @JsonIgnore
    public static final Aggregator<String, LeapMotionFrame, GesturePerUserAggregator> AGGREGATE = (key, value, aggregate) -> aggregate == null? new GesturePerUserAggregator() : aggregate.aggregate(key, value);
    @JsonIgnore
    public static final Merger<? super String, GesturePerUserAggregator> MERGE = (aggKey, aggOne, aggTwo) -> aggTwo.merge(aggKey, aggOne, aggTwo);
    @JsonIgnore
    private Log logger = LogFactory.getLog(GesturePerUserAggregator.class);

    public GesturePerUserAggregator() {
        super();
    }
    /**
     * aggregator
     *
     * @param userName
     * @param value
     * @return
     */
    public GesturePerUserAggregator aggregate(final String userName, final LeapMotionFrame value) {
        if (value.getGestures() == null || value.getGestures().isEmpty()) {
            return this;
        }
        value.getGestures().forEach(gesture -> {
            logger.info(gesture);
            if (gesture == null) {
                return;
            }
            logger.info(gesture.toString());
            if (gesture.getType() == null) {
                return;
            }
            switch (gesture.getType()) {
                case "circle":
                    if (gesture.getState().equals("start")) {
                        circleGesturesDuration.putIfAbsent(gesture.getId(), gesture.getDuration());
                    } else if (gesture.getState().equals("update")) {
                        circleGesturesDuration.put(gesture.getId(), gesture.getDuration());
                    } else {
                        circleGesturesDuration.put(gesture.getId(), gesture.getDuration());
                        calculateCircles(gesture.getId());
                    }
                    break;
                case "keyTap":
                    if (gesture.getState().equals("start")) {
                        keyTapGesturesDuration.putIfAbsent(gesture.getId(), gesture.getDuration());
                    } else if (gesture.getState().equals("update")) {
                        keyTapGesturesDuration.put(gesture.getId(), gesture.getDuration());
                    } else {
                        keyTapGesturesDuration.put(gesture.getId(), gesture.getDuration());
                        calculateKeyTaps(gesture.getId());
                    }
                    break;
                case "swipe":
                    if (gesture.getState().equals("start")) {
                        swipeGesturesDuration.putIfAbsent(gesture.getId(), gesture.getDuration());
                    } else if (gesture.getState().equals("update")) {
                        swipeGesturesDuration.put(gesture.getId(), gesture.getDuration());
                    } else {
                        swipeGesturesDuration.put(gesture.getId(), gesture.getDuration());
                        calculateSwipes(gesture.getId());
                    }
                    break;
                case "screenTap":
                    if (gesture.getState().equals("start")) {
                        screenGesturesDuration.putIfAbsent(gesture.getId(), gesture.getDuration());
                    } else if (gesture.getState().equals("update")) {
                        screenGesturesDuration.put(gesture.getId(), gesture.getDuration());
                    } else {
                        screenGesturesDuration.put(gesture.getId(), gesture.getDuration());
                        calculateScreenTaps(gesture.getId());
                    }
                    break;
                default:
                    logger.error("Unknown type of gesture.");
            }
        });
        return this;
    }


    /**
     * merges two sessions
     * @param Key
     * @param v1
     * @param v2
     * @return
     */
    public GesturePerUserAggregator merge(String Key, GesturePerUserAggregator v1, GesturePerUserAggregator v2) {
        v2.circleGesturesDuration.putAll(v1.circleGesturesDuration);
        v2.circleDuration.combine(v1.circleDuration);
        v2.circles += v1.circles;
        v2.swipeGesturesDuration.putAll(v1.swipeGesturesDuration);
        v2.swipeDuration.combine(v1.swipeDuration);
        v2.swipes += v1.swipes;
        v2.keyTapGesturesDuration.putAll(v1.keyTapGesturesDuration);
        v2.keyTapDuration.combine(v1.keyTapDuration);
        v2.keyTaps += v1.keyTaps;
        v2.screenGesturesDuration.putAll(v1.screenGesturesDuration);
        v2.screenTapDuration.combine(v1.screenTapDuration);
        v2.screenTaps += v1.screenTaps;
        return v2;
    }

    private void calculateScreenTaps(Long gestureId) {
        screenTaps++;
        screenTapDuration.accept(screenGesturesDuration.remove(gestureId));
    }


    private void calculateSwipes(Long gestureId) {
        swipes++;
        swipeDuration.accept(swipeGesturesDuration.remove(gestureId));
    }

    private void calculateKeyTaps(Long gestureId) {
        keyTaps++;
        keyTapDuration.accept(keyTapGesturesDuration.remove(gestureId));
    }

    private void calculateCircles(Long gestureId) {
        circles++;
        circleDuration.accept(circleGesturesDuration.remove(gestureId));
    }

}
