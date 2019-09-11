package com.edutec.moodleXapiTransformer;

import com.edutec.moodleXapiTransformer.models.Moodle;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class JsonTests {
    private final String mdlQuizJson = "{\"ID\":1,\"COURSE\":1,\"NAME\":\"A quiz\",\"INTRO\":\"<p>This is a qiuiz<br></p>\",\"INTROFORMAT\":1,\"TIMEOPEN\":1566252000,\"TIMECLOSE\":1567139040,\"TIMELIMIT\":0,\"OVERDUEHANDLING\":\"autosubmit\",\"GRACEPERIOD\":0,\"PREFERREDBEHAVIOR\":null,\"CANREDOQUESTIONS\":0,\"ATTEMPTS\":0,\"ATTEMPTONLAST\":0,\"GRACEMETHOD\":1,\"DECIMALPOINTS\":2.0,\"TIMECREATED\":0,\"TIMEMODIFIED\":1566361528}";

    @Test
    public void shouldParseJsonAsQuiz() throws Exception {
        final Moodle.MdlQuiz quiz = new ObjectMapper().readValue(mdlQuizJson, Moodle.MdlQuiz.class);
        Assert.assertNotNull(quiz);
        Assert.assertNotNull(quiz.getId());
        Assert.assertNotNull(quiz.getAttemptonlast());
        Assert.assertNotNull(quiz.getName());
        Assert.assertNotNull(quiz.getTimeclose());
        Assert.assertNotNull(quiz.getTimecreated());

    }

}
