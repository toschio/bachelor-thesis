package com.edutec.moodleXapiTransformer.models;

import com.edutec.moodleXapiTransformer.models.xapimodels.Statement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class StatementDataWrapper {
    private Statement statement;

    private Moodle.MdlForumPost mdlForumPost;
    private Moodle.MdlForum mdlForum;
    private Moodle.MdlUser mdlUser;

    private Moodle.MdlQuizAttempt mdlQuizAttempt;
    private Moodle.MdlQuiz mdlQuiz;

    public StatementDataWrapper setMdlUser(Moodle.MdlUser mdlUser) {
        this.mdlUser = mdlUser;
        return this;
    }

    public StatementDataWrapper setMdlQuiz(Moodle.MdlQuiz mdlQuiz) {
        this.mdlQuiz = mdlQuiz;
        return this;
    }

    public StatementDataWrapper setMdlForum(Moodle.MdlForum mdlForum) {
        this.mdlForum = mdlForum;
        return this;
    }

    public StatementDataWrapper setMdlForumPost(Moodle.MdlForumPost mdlForumPost) {
        this.mdlForumPost = mdlForumPost;
        return this;
    }

}
