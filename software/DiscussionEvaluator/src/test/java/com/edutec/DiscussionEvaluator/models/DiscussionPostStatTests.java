package com.edutec.DiscussionEvaluator.models;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.LongSummaryStatistics;

@RunWith(SpringRunner.class)
public class DiscussionPostStatTests {

    @Test
    public void count1() throws Exception {
        NumberOfPostsPerDiscussion numberOfPostsPerDiscussion = new NumberOfPostsPerDiscussion();
        HashMap<String, Long> map = new HashMap<>();
        map.put("disc1", 1L);
        numberOfPostsPerDiscussion.setNumberOfPostsPerDiscussion(map);

        DiscussionPostStat stat = new DiscussionPostStat();
        stat.apply(numberOfPostsPerDiscussion);

        Assert.assertTrue(stat.getAverageForumPostsPerDiscussion().getSum() == 1l);
        Assert.assertTrue(stat.getAverageForumPostsPerDiscussion().getMin() == 1l);
        Assert.assertTrue(stat.getAverageForumPostsPerDiscussion().getMax() == 1l);
        Assert.assertTrue(stat.getAverageForumPostsPerDiscussion().getAverage() == 1l);
        Assert.assertTrue(stat.getAverageForumPostsPerDiscussion().getCount() == 1l);
    }


    @Test
    public void count2() throws Exception {
        NumberOfPostsPerDiscussion numberOfPostsPerDiscussion = new NumberOfPostsPerDiscussion();
        HashMap<String, Long> map = new HashMap<>();
        map.put("disc1", 2L);
        numberOfPostsPerDiscussion.setNumberOfPostsPerDiscussion(map);

        DiscussionPostStat stat = new DiscussionPostStat();
        stat.apply(numberOfPostsPerDiscussion);

        Assert.assertTrue(stat.getAverageForumPostsPerDiscussion().getSum() == 2l);
        Assert.assertTrue(stat.getAverageForumPostsPerDiscussion().getMin() == 2l);
        Assert.assertTrue(stat.getAverageForumPostsPerDiscussion().getMax() == 2l);
        Assert.assertTrue(stat.getAverageForumPostsPerDiscussion().getAverage() == 2l);
        Assert.assertTrue(stat.getAverageForumPostsPerDiscussion().getCount() == 1l);
    }

    @Test
    public void count2Diff() throws Exception {
        NumberOfPostsPerDiscussion numberOfPostsPerDiscussion = new NumberOfPostsPerDiscussion();
        HashMap<String, Long> map = new HashMap<>();
        map.put("disc1", 1L);
        map.put("disc2", 2L);
        numberOfPostsPerDiscussion.setNumberOfPostsPerDiscussion(map);

        DiscussionPostStat stat = new DiscussionPostStat();
        stat.apply(numberOfPostsPerDiscussion);

        Assert.assertTrue(stat.getAverageForumPostsPerDiscussion().getSum() == 3l);
        Assert.assertTrue(stat.getAverageForumPostsPerDiscussion().getMin() == 1l);
        Assert.assertTrue(stat.getAverageForumPostsPerDiscussion().getMax() == 2l);
        Assert.assertTrue(stat.getAverageForumPostsPerDiscussion().getAverage() == 1.5);
        Assert.assertTrue(stat.getAverageForumPostsPerDiscussion().getCount() == 2l);
    }

    @Test
    public void testAddReplyTimeOfPost() throws Exception {
        final DiscussionPostStat discussionPostStat = new DiscussionPostStat();
        final ReplyTimeOfPostsPerDiscussion replyTimeOfPostsPerDiscussion = new ReplyTimeOfPostsPerDiscussion();
        final HashMap<String, LongSummaryStatistics> replyTimeOfPosts = new HashMap<>();
        replyTimeOfPosts.put("discussion1", new LongSummaryStatistics(2, 100, 120, 220));
//        Arrays.asList(
//                new ReplyTimeOfPostsPerDiscussion.ReplyTimeOfPost("a", 100L, "discussion1", "actor"),
//                new ReplyTimeOfPostsPerDiscussion.ReplyTimeOfPost("b", 120L, "discussion1", "actor")
//        ));
        replyTimeOfPosts.put("discussion", new LongSummaryStatistics(1, 10, 10, 10));
//        Arrays.asList(
//                new ReplyTimeOfPostsPerDiscussion.ReplyTimeOfPost("c", 10L, "discussion2", "actor")));
        replyTimeOfPostsPerDiscussion.setReplyTimeOfPostsPerDiscussion(replyTimeOfPosts);
        replyTimeOfPostsPerDiscussion.setActorId("actor");

        discussionPostStat.apply(replyTimeOfPostsPerDiscussion);

        final LongSummaryStatistics expectedResult = new LongSummaryStatistics(3L, 10, 120, 230);
        Assert.assertEquals(expectedResult.getAverage(), discussionPostStat.getAverageReplyTimeOfPost().getAverage(), 0);
        Assert.assertEquals(expectedResult.getCount(), discussionPostStat.getAverageReplyTimeOfPost().getCount());
        Assert.assertEquals(expectedResult.getMax(), discussionPostStat.getAverageReplyTimeOfPost().getMax());
        Assert.assertEquals(expectedResult.getMin(), discussionPostStat.getAverageReplyTimeOfPost().getMin());

        final LongSummaryStatistics expextedResultPerDiscussion1 = new LongSummaryStatistics(2l, 100, 120, 220);
        final LongSummaryStatistics actualResultPerDiscussion1 = discussionPostStat.getAverageReplyTimeOfPostPerDiscussion().get("discussion1");
        Assert.assertEquals(expextedResultPerDiscussion1.getAverage(), actualResultPerDiscussion1.getAverage(), 0);
        Assert.assertEquals(expextedResultPerDiscussion1.getMin(), actualResultPerDiscussion1.getMin());
        Assert.assertEquals(expextedResultPerDiscussion1.getMax(), actualResultPerDiscussion1.getMax());
        Assert.assertEquals(expextedResultPerDiscussion1.getCount(), actualResultPerDiscussion1.getCount());
        Assert.assertEquals(expextedResultPerDiscussion1.getSum(), actualResultPerDiscussion1.getSum());
    }
}
