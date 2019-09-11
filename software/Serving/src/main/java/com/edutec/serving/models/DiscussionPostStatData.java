package com.edutec.serving.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.LongSummaryStatistics;
import java.util.Map;

@Data
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscussionPostStatData {

    private Long created;
    private Long updated;

    private Long forumPosts = 0L;
    // statistics for replyTimePerDiscussion
    private Map<String, LongSummaryStatistics> averageReplyTimeOfPostPerDiscussion = new HashMap<String, LongSummaryStatistics>();
    // Map<DiscussionID, #ForumPostsInDiscussion> - counts for each discussion the number of forum posts
    private Map<String, Long> forumPostsPerDiscussion = new HashMap<>();

    // statistics for forumPostsPerDiscussion
    private LongSummaryStatistics averageReplyTimeOfPost = new LongSummaryStatistics(0L, 0L, 0L, 0L);
    private LongSummaryStatistics averageForumPostsPerDiscussion = new LongSummaryStatistics(0L, 0, 0, 0);

    private Long originalId;
}
