package com.edutec.AssessmentEvaluator.models.leapmotionmodels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class LeapMotionFrame {
    private Float currentFrameRate;
    private ArrayList<Device> devices; // todo
    private ArrayList<Gesture> gestures; // todo
    private ArrayList<Hand> hands;
    private Long id;
    private InteractionBox interactionBox;
    private ArrayList<Pointable> pointables;
    private ArrayList<Vector3> r;
    private Float s;
    private Long timestamp;
    private Vector3 t; // always a triple ?

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Pointable {
        private Vector3 direction;
        private Long handId;
        private Long id;
        private Float length;
        private Vector3 stabilizedTipPosition;
        private Float timeVisible;
        private Vector3 tipPosition;
        private Vector3 tipVelocity;
        private Boolean tool;
        private Float touchDistance;
        private String touchZone;
        private Float width;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Hand {
        private Long id;
        private Vector3 direction;
        private Vector3 palmNormal;
        private Vector3 palmPosition;
        private Vector3 palmVelocity;
        private ArrayList<Vector3> r;
        private Float s;
        private Vector3 sphereCenter;
        private Float sphereRadius;
        private Vector3 stabilizedPalmPosition;
        private Vector3 t;
        private Float timeVisible;
    }

    @Data
    @JsonDeserialize(using = Serdes.GestureDeserializer.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static abstract class Gesture {
        private String state; // start, stop
        private String type;
        private Long duration;
        private ArrayList<Long> handIds;
        private Long id;
    }

    @Data
    // none class deserializer needed as deserialization from
    // {@link com.edutec.LeapMotionIndicator.models.leapmotionmodels.Serdes.GestureDeserializer} triggers
    // otherwise a circular reference
    @JsonDeserialize(using = JsonDeserializer.None.class)
    public static class CircleGesture extends Gesture {
        private Vector3 center;
        private Vector3 normal;
        private ArrayList<Long> pointableIds;
        private Float progress;
        private Float radius;
    }

    @Data
    @JsonDeserialize(using = JsonDeserializer.None.class)
    public static class KeyTapGesture extends Gesture {
        private Vector3 direction;
        private ArrayList<Long> pointableIds;
        private Vector3 position;
        private Float progress;
    }

    @Data
    @JsonDeserialize(using = JsonDeserializer.None.class)
    public static class SwipeGesture extends Gesture {
        private Vector3 direction;
        private ArrayList<Long> pointableIds;
        private Vector3 position;
        private Float speed;
        private Vector3 startPosition;
    }

    @Data
    @JsonDeserialize(using = JsonDeserializer.None.class)
    public static class ScreenTapGesture extends Gesture {
        private Float progress;
        private Vector3 position;
        private ArrayList<Long> pointableIds;
        private Vector3 direction;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InteractionBox {
        private Vector3 center;
        private Vector3 size;

    }

    @JsonDeserialize(using = Serdes.Vector3Deserializer.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    @RequiredArgsConstructor
    @JsonSerialize(using = Serdes.Vector3Serializer.class)
    public static class Vector3 {
//        @JsonIgnore
        private final Float second;
//        @JsonIgnore
        private final Float first;
//        @JsonIgnore
        private final Float third;

//        @JsonSerialize
//        public List getJson() {
//            return Arrays.asList(first, second, third);
//        }
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Device {


    }
}
