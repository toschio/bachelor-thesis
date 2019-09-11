package com.edutec.DiscussionEvaluator.models;

import com.edutec.DiscussionEvaluator.models.xapimodels.Statement;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;

@NoArgsConstructor
@Data
@ToString
public class NumberOfPostsPerDiscussion {

    private HashMap<String, Long> numberOfPostsPerDiscussion = new HashMap<>();


    /**
     * adder for aggregation
     *
     * @param actorId
     * @param statement
     * @param aggregate
     * @return
     */
    public static NumberOfPostsPerDiscussion add(String actorId, Statement statement, NumberOfPostsPerDiscussion aggregate) {
        String discussionID = statement.getContext().getContextActivities().getCategory().get(0).getId().toString();
        aggregate.numberOfPostsPerDiscussion.computeIfPresent(discussionID, (k, v) -> v + 1L);
        aggregate.numberOfPostsPerDiscussion.putIfAbsent(discussionID, 1L);
        return aggregate;
    }


    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof NumberOfPostsPerDiscussion)) return false;
        final NumberOfPostsPerDiscussion other = (NumberOfPostsPerDiscussion) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$numberOfPostsPerDiscussion = this.getNumberOfPostsPerDiscussion();
        final Object other$numberOfPostsPerDiscussion = other.getNumberOfPostsPerDiscussion();
        if (this$numberOfPostsPerDiscussion == null ? other$numberOfPostsPerDiscussion != null : !this$numberOfPostsPerDiscussion.equals(other$numberOfPostsPerDiscussion))
            return false;
        if (!this.getNumberOfPostsPerDiscussion().equals(other.getNumberOfPostsPerDiscussion()))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof NumberOfPostsPerDiscussion;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $numberOfPostsPerDiscussion = this.getNumberOfPostsPerDiscussion();
        result = result * PRIME + ($numberOfPostsPerDiscussion == null ? 43 : $numberOfPostsPerDiscussion.hashCode());
        return result;
    }
}
