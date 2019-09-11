package com.edutec.DiscussionEvaluator.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.kafka.streams.kstream.Initializer;

import java.util.HashMap;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.function.BiFunction;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReplyTimeOfPostsPerDiscussion implements Initializer<ReplyTimeOfPostsPerDiscussion> {

    @JsonIgnore
    private static final BiFunction<Long, Long, Long> add = (v1, v2) -> v1 + v2;
    @JsonIgnore
    private static final BiFunction<Long, Long, Long> subtract = (v1, v2) -> v1 - v2;

    private String actorId;

    //    private Map<String, List<ReplyTimeOfPost>> replyTimeOfPostsPerDiscussion = new HashMap<>();
    private Map<String, LongSummaryStatistics> replyTimeOfPostsPerDiscussion = new HashMap<>();

    /**
     * adder for aggregation
     *
     * @param key
     * @param value
     * @param aggregate
     * @return
     */
    public static ReplyTimeOfPostsPerDiscussion add(String key, ReplyTimeOfPostsPerDiscussion.ReplyTimeOfPost value, ReplyTimeOfPostsPerDiscussion aggregate) {
        if (aggregate.actorId == null) {
            aggregate.actorId = value.getActorId();
        }
        // todo this should not happen !
        if (!aggregate.actorId.equals(value.getActorId())) {
            throw new RuntimeException("Reply times of two different actors are being tried to be merged. This is a grave logical error.");
        }
        final LongSummaryStatistics replTime = aggregate.replyTimeOfPostsPerDiscussion.getOrDefault(value.getDiscussionId(), new LongSummaryStatistics(0, 0, 0, 0));
        replTime.accept(value.getReplyTime());
        aggregate.replyTimeOfPostsPerDiscussion.put(value.getDiscussionId(), replTime);
//        aggregate.replyTimeOfPostsPerDiscussion.merge(value.getDiscussionId(), List.of(value),
//                (replyTimeOfPosts, replyTimeOfPosts2) -> Stream.concat(replyTimeOfPosts2.stream(), replyTimeOfPosts.stream()).collect(Collectors.toList()));
        return aggregate;
    }

    /**
     * initializer for Aggregation
     *
     * @return
     */
    @Override
    public ReplyTimeOfPostsPerDiscussion apply() {
        return new ReplyTimeOfPostsPerDiscussion();
    }


    @AllArgsConstructor
    @Data
    @NoArgsConstructor
    public static class ReplyTimeOfPost {
        private String statementId;
        private Long replyTime;
        private String discussionId;
        private String actorId;
    }
}
