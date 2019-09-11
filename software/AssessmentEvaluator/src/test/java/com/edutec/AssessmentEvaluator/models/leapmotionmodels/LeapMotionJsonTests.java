package com.edutec.AssessmentEvaluator.models.leapmotionmodels;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class LeapMotionJsonTests {

    private String circleGesture = "{\n" +
            "      \"center\": [\n" +
            "        -156.949,\n" +
            "        450.954,\n" +
            "        -109.411\n" +
            "      ],\n" +
            "      \"duration\": 0,\n" +
            "      \"handIds\": [\n" +
            "        2\n" +
            "      ],\n" +
            "      \"id\": 1,\n" +
            "      \"normal\": [\n" +
            "        0.254777,\n" +
            "        0.846783,\n" +
            "        -0.466955\n" +
            "      ],\n" +
            "      \"pointableIds\": [\n" +
            "        21\n" +
            "      ],\n" +
            "      \"progress\": 0.781077,\n" +
            "      \"radius\": 33.0964,\n" +
            "      \"state\": \"start\",\n" +
            "      \"type\": \"circle\"\n" +
            "    }";

    private String leapMotionFrame = "{\n" +
            "  \"currentFrameRate\": 106.301,\n" +
            "  \"devices\": [],\n" +
            "  \"gestures\": [],\n" +
            "  \"hands\": [],\n" +
            "  \"id\": 384579,\n" +
            "  \"interactionBox\": {\n" +
            "    \"center\": [\n" +
            "      0.00000,\n" +
            "      200.000,\n" +
            "      0.00000\n" +
            "    ],\n" +
            "    \"size\": [\n" +
            "      235.247,\n" +
            "      235.247,\n" +
            "      147.751\n" +
            "    ]\n" +
            "  },\n" +
            "  \"pointables\": [],\n" +
            "  \"r\": [\n" +
            "    [\n" +
            "      1.00000,\n" +
            "      0.00000,\n" +
            "      0.00000\n" +
            "    ],\n" +
            "    [\n" +
            "      0.00000,\n" +
            "      1.00000,\n" +
            "      0.00000\n" +
            "    ],\n" +
            "    [\n" +
            "      0.00000,\n" +
            "      0.00000,\n" +
            "      1.00000\n" +
            "    ]\n" +
            "  ],\n" +
            "  \"s\": 1.00000,\n" +
            "  \"t\": [\n" +
            "    0.10000,\n" +
            "    0.20000,\n" +
            "    0.30000\n" +
            "  ],\n" +
            "  \"timestamp\": 1565407620214272\n" +
            "}\n";

    @Test
    public void shouldParseVector3() throws Exception {
        LeapMotionFrame leapMotionFrame = new ObjectMapper().readValue(this.leapMotionFrame, LeapMotionFrame.class);
        Assert.assertEquals(new LeapMotionFrame.Vector3(0.1F, 0.2F, 0.3F), leapMotionFrame.getT());
    }

    @Test
    public void shouldParseCircleGesture() throws Exception {
        LeapMotionFrame.Gesture gesture = new ObjectMapper().readValue(this.circleGesture, LeapMotionFrame.Gesture.class);
        Assert.assertTrue(gesture.getType().equals("circle" ));
        Assert.assertTrue(gesture instanceof LeapMotionFrame.CircleGesture);
    }


    @Test
    public void shouldWriteVector3() throws Exception {
        LeapMotionFrame.Vector3 vector3 = new LeapMotionFrame.Vector3(1F, 2F, 3F);
        String result = new ObjectMapper().writeValueAsString(vector3);
        Assert.assertFalse(result.contains("first"));
        Assert.assertFalse(result.contains("second"));
        Assert.assertFalse(result.contains("third"));
    }
}
