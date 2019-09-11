package com.edutec.DiscussionEvaluator.models;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.LongSummaryStatistics;
import java.util.Map;

@Data
@NoArgsConstructor
@ToString
public class DiscussionPostStat {
    private final Long created = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
    private Long updated;

    private Long forumPosts = 0L;
    // Map<DiscussionID, #ForumPostsInDiscussion> - counts for each discussion the number of forum posts
    private Map<String, Long> forumPostsPerDiscussion = new HashMap<>();
    // statistics for forumPostsPerDiscussion
    private LongSummaryStatistics averageForumPostsPerDiscussion = new LongSummaryStatistics(0L, 0L, 0L, 0L);

    private LongSummaryStatistics averageReplyTimeOfPost = new LongSummaryStatistics(0L, 0L, 0L, 0L);
    // statistics for replyTimePerDiscussion
    private Map<String, LongSummaryStatistics> averageReplyTimeOfPostPerDiscussion = new HashMap<String, LongSummaryStatistics>();

    /**
     * adds the indicator regarding numbers of posts to instance
     *
     * @param numberOfPostsPerDiscussion
     * @return
     */
    public DiscussionPostStat apply(NumberOfPostsPerDiscussion numberOfPostsPerDiscussion) {
        if (numberOfPostsPerDiscussion == null) {
            return this;
        }
        this.setForumPostsPerDiscussion(numberOfPostsPerDiscussion.getNumberOfPostsPerDiscussion());
        this.calculateAverageNumberOfForumPostsPerDiscussion();
        return this;
    }

    /**
     * adds the indicator regarding reply time of replies to instance
     *
     * @param replyTimeOfPostsPerDiscussion
     * @return
     */
    public DiscussionPostStat apply(ReplyTimeOfPostsPerDiscussion replyTimeOfPostsPerDiscussion) {
        if (replyTimeOfPostsPerDiscussion == null) {
            return this;
        }
        replyTimeOfPostsPerDiscussion.getReplyTimeOfPostsPerDiscussion().forEach((discussionId, replyTimeOfPosts) -> {
            averageReplyTimeOfPostPerDiscussion.merge(discussionId, replyTimeOfPosts,
                    (longSummaryStatistics, longSummaryStatistics2) -> {
                        longSummaryStatistics2.combine(longSummaryStatistics);
                        return longSummaryStatistics2;
                    });
        });
        // calculate overall average
        this.averageReplyTimeOfPost = this.averageReplyTimeOfPostPerDiscussion.values()
                .stream()
                .reduce(new LongSummaryStatistics(0L, 0L, 0L, 0L), (acc, longSummaryStatistics2) -> {
                    acc.combine(longSummaryStatistics2);
                    return acc;
                });
        setUpdate();
        return this;
    }

    /**
     * calculates LongSummaryStatistics representing the forumPostsPerDiscussion
     *
     * @return
     */
    public void calculateAverageNumberOfForumPostsPerDiscussion() {
        this.averageForumPostsPerDiscussion = this.forumPostsPerDiscussion.values().stream().collect(LongSummaryStatistics::new, LongSummaryStatistics::accept, LongSummaryStatistics::combine); // mapToLong(x -> (Long) x).summaryStatistics();
    }


    /**
     * cd
     * setter for Number of Posts Per Discussion that returns <this>
     *
     * @param numberOfPostsPerDiscussion
     * @return
     */
    private void setForumPostsPerDiscussion(Map<String, Long> numberOfPostsPerDiscussion) {
        this.forumPostsPerDiscussion = numberOfPostsPerDiscussion;
        this.forumPosts = numberOfPostsPerDiscussion.values().stream().mapToLong(Long::longValue).sum();
    }


    private void setUpdate() {
        this.updated = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
    }
}

