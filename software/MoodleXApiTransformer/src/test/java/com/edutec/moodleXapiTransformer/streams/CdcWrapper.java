package com.edutec.moodleXapiTransformer.streams;

import com.edutec.moodleXapiTransformer.models.Moodle;
import lombok.Data;

@Data
class CdcWrapper<T> {
    private T before;
    private T after;
    private CdcWrapper.Source source;
    private String op = "c";
    private Long ts_ms = 1566603183180L;


    static CdcWrapper AFTER_MDL_USER() {
        final CdcWrapper<Moodle.MdlUser> cdcWrapper = new CdcWrapper<Moodle.MdlUser>();
        cdcWrapper.source = Source.MOODLE_USER_SOURCE();
        cdcWrapper.before = null;
        final Moodle.MdlUser mdlUser = new Moodle.MdlUser();
        mdlUser.setId(1L);
        mdlUser.setFirstname("firstname");
        mdlUser.setLastname("lastname");
        cdcWrapper.after = mdlUser;
        return cdcWrapper;
    }

    static CdcWrapper BEFORE_MDL_USER() {
        final CdcWrapper<Moodle.MdlUser> cdcWrapper = new CdcWrapper<Moodle.MdlUser>();
        cdcWrapper.source = Source.MOODLE_USER_SOURCE();
        cdcWrapper.after = null;
        final Moodle.MdlUser mdlUser = new Moodle.MdlUser();
        mdlUser.setId(1L);
        mdlUser.setFirstname("firstname");
        mdlUser.setLastname("lastname");
        cdcWrapper.before = mdlUser;
        return cdcWrapper;
    }

    static CdcWrapper<Moodle.MdlForum> AFTER_MDL_FORUM() {
        final CdcWrapper<Moodle.MdlForum> cdcWrapper = new CdcWrapper<>();
        cdcWrapper.source = Source.MOODLE_FORUM_SOURCE();
        cdcWrapper.before = null;
        final Moodle.MdlForum forum = new Moodle.MdlForum();
        forum.setId(1L);
        forum.setName("a name");
        forum.setIntro("an intro");
        forum.setTimemodified(012234L);
        forum.setCourse(1L);
        cdcWrapper.after = forum;
        return cdcWrapper;
    }

    static CdcWrapper<Moodle.MdlForumPost> AFTER_MDL_FORUM_POST() {
        final CdcWrapper<Moodle.MdlForumPost> cdcWrapper = new CdcWrapper<>();
        cdcWrapper.source = Source.MOODLE_FORUM_POST_SOURCE();
        cdcWrapper.before = null;
        final Moodle.MdlForumPost mdlForumPost = new Moodle.MdlForumPost();
        mdlForumPost.setId(1L);
        mdlForumPost.setCreated(012345L);
        mdlForumPost.setModified(012345L);
        mdlForumPost.setDiscussion(1L);
        mdlForumPost.setMessage("message");
        mdlForumPost.setUserid(1L);
        mdlForumPost.setParent(0L);
        cdcWrapper.after = mdlForumPost;
        return cdcWrapper;
    }


    @Data
    public static class Source {
        private String version = "0.9.5.Final";
        private String connector = "mysql";
        private String name = "_.goethe-universitaet-frankfurt.db";
        private Integer server_id = 0;
        private Long ts_sec = 0L;
        private String file = "mysql-bin.000005";
        private Long pos = 154L;
        private Long row = 0L;
        private boolean snapshot = true;
        private String db = "moodle";
        private String table;
        private String query = null;

        static Source MOODLE_USER_SOURCE() {
            final Source source = new Source();
            source.table = "mdl_user";
            return source;
        }

        static Source MOODLE_FORUM_SOURCE() {
            final Source source = new Source();
            source.table = "mdl_forum";
            return source;
        }

        static Source MOODLE_FORUM_POST_SOURCE() {
            final Source source = new Source();
            source.table = "mdl_forum_posts";
            return source;
        }
    }

}
