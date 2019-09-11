package com.edutec.DiscussionEvaluator.models;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.LongSummaryStatistics;

@RunWith(SpringRunner.class)
public class ReplyTimeOfPostTests {

    @Test
    public void shouldAddReplyTime() throws Exception {
        final ReplyTimeOfPostsPerDiscussion replyTimeOfPostPerDiscussion = new ReplyTimeOfPostsPerDiscussion();
        final ReplyTimeOfPostsPerDiscussion.ReplyTimeOfPost replyTimeOfPost1 = new ReplyTimeOfPostsPerDiscussion.ReplyTimeOfPost();
        replyTimeOfPost1.setActorId("actor1");
        replyTimeOfPost1.setDiscussionId("discussion1");
        replyTimeOfPost1.setReplyTime(100L);
        replyTimeOfPost1.setStatementId("firststatement");
        ReplyTimeOfPostsPerDiscussion.add("a", replyTimeOfPost1, replyTimeOfPostPerDiscussion);

        final LongSummaryStatistics result1 = replyTimeOfPostPerDiscussion.getReplyTimeOfPostsPerDiscussion().get("discussion1");
//        Assert.assertEquals("actor1", result1.getActorId());
//        Assert.assertEquals("discussion1", result1.getDiscussionId());
        Assert.assertEquals((Long) 100L, (Long) result1.getSum());
//        Assert.assertEquals("firststatement", result1.getStatementId());

        replyTimeOfPost1.setStatementId("secondstatement");

        ReplyTimeOfPostsPerDiscussion.add("b", replyTimeOfPost1, replyTimeOfPostPerDiscussion);
        final LongSummaryStatistics result2 = replyTimeOfPostPerDiscussion.getReplyTimeOfPostsPerDiscussion().get("discussion1");
//        Assert.assertEquals("actor1", result2.getActorId());
//        Assert.assertEquals("discussion1", result2.getDiscussionId());
        Assert.assertEquals((Long) 200L, (Long) result2.getSum());
        Assert.assertEquals((Long) 100L, (Long) result2.getMax());
        Assert.assertEquals((Long) 100L, (Long) result2.getMin());
//        Assert.assertEquals("secondstatement", result2.getStatementId());
    }
}
