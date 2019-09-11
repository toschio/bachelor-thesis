package com.edutec.serving.models.dtos;

import com.edutec.serving.models.DiscussionPostStatData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.LongSummaryStatistics;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class DiscussionPostStatDataDto {
    private String userID;

    private Long forumPosts = 0L;
    // Map<DiscussionID, #ForumPostsInDiscussion> - counts for each discussion the number of forum posts
    private Map<String, Long> forumPostsPerDiscussion = new HashMap<>();
    // statistics for forumPostsPerDiscussion
    private LongSummaryStatistics averageForumPostsPerDiscussion = new LongSummaryStatistics(0L, 0, 0, 0);

    private LongSummaryStatistics averageReplyTimeOfPost = new LongSummaryStatistics(0L, 0L, 0L, 0L);
    // statistics for replyTimePerDiscussion
    private Map<String, LongSummaryStatistics> averageReplyTimeOfPostPerDiscussion = new HashMap<String, LongSummaryStatistics>();

    public static DiscussionPostStatDataDto of(String key, DiscussionPostStatData value) {
        DiscussionPostStatDataDto discussionPostStatDataDto = new DiscussionPostStatDataDto();
        discussionPostStatDataDto.setUserID(key);
        discussionPostStatDataDto.setAverageForumPostsPerDiscussion(value.getAverageForumPostsPerDiscussion());
        discussionPostStatDataDto.setAverageReplyTimeOfPostPerDiscussion(value.getAverageReplyTimeOfPostPerDiscussion());
        discussionPostStatDataDto.setForumPosts(value.getForumPosts());
        discussionPostStatDataDto.setForumPostsPerDiscussion(value.getForumPostsPerDiscussion());
        discussionPostStatDataDto.setAverageReplyTimeOfPost(value.getAverageReplyTimeOfPost());
        return discussionPostStatDataDto;
    }
}
