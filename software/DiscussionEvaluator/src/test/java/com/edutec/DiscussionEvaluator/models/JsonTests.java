package com.edutec.DiscussionEvaluator.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;

@RunWith(SpringRunner.class)
public class JsonTests {


    ObjectMapper objectMapper = new ObjectMapper();


    @Test
    public void shouldSerialize() throws Exception {
        NumberOfPostsPerDiscussion numberOfPostsPerDiscussion = new NumberOfPostsPerDiscussion();
        HashMap<String, Long> map = new HashMap<>();
        map.put("goethe-universitaet-frankfurt.moodle.forum.1", 1L);
//        map.put("test2", 2L);
        numberOfPostsPerDiscussion.setNumberOfPostsPerDiscussion(map);

        String result = objectMapper.writeValueAsString(numberOfPostsPerDiscussion);
        Assert.assertEquals("{\"numberOfPostsPerDiscussion\":{\"goethe-universitaet-frankfurt.moodle.forum.1\":1}}", result);

//        NumberOfPostsPerDiscussion back = objectMapper.readValue(result, NumberOfPostsPerDiscussion.class);
//        Assert.assertEquals((Long) 1L, (Long) back.getNumberOfPostsPerDiscussion().get("goethe-universitaet-frankfurt.moodle.forum.1"));
//        Assert.assertEquals((Long) 2L, (Long) back.getNumberOfPostsPerDiscussion().get("test2"));

    }

}
