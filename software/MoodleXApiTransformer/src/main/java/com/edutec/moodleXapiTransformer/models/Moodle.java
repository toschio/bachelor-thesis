package com.edutec.moodleXapiTransformer.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

public class Moodle {
    @Data
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MdlForum {
        Long id;
        Long course;
        String type;
        String name;
        String intro;
        int introformat;
        boolean assessed;
        Long assesstimestart;
        Long assesstimefinish;
        Long scale;
        Long maxbytes;
        Long maxattachments;
        int forcesubscribe;
        int trackingtype;
        int rsstype;
        int rssarticles;
        Long timemodified;
        int warnafter;
        int blockafter;
        int blockperiod;
        int completiondiscussions;
        int completionreplies;
        int completionposts;
        boolean displaywordcount;
        boolean lockdiscussionafter;

    }

    @Data
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    @EqualsAndHashCode(callSuper = true)
    public static class MdlUser extends TombStone {
        Long id;
        String auth;
        int confirmed;
        int policyagreed;
        int deleted;
        int suspended;
        Long mnethostid;
        String username;
        String password;
        String idnumber;
        String firstname;
        String lastname;
        String email;
        int emailstop;
        String icq;
        String skype;
        String yahoo;
        String aim;
        String msn;
        String phone1;
        String phone2;
        String institution;
        String department;
        String address;
        String city;
        String country;
        String lang;
        String calendartype;
        String theme;
        String timezone;
        Long firstaccess;
        Long lastaccess;
        Long lastlogin;
        Long currentlogin;
        String lastip;
        String secret;
        Long picture;
        String url;
        String description;
        int descriptionformat;
        int mailformat;
        int maildigest;
        int maildisplay;
        int autosubscribe;
        int trackforums;
        Long timecreated;
        Long timemodified;
        Long trustbitmask;
        String imagealt;
        String lastnamephonetic;
        String firstnamephonetic;
        String middlename;
        String alternatename;

    }

    @Data
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MdlForumPost {

        Long id;
        Long discussion;
        Long parent;
        Long userid;
        Long created;
        Long modified;
        //    int mailed;
        //    String subject;
        String message;
        //    int messageformat;
        //    int messagetrust;
        //    String attachment;
        //    int totalscore;
        //    int mailnow;
        //    boolean deleted;

    }

    @Data
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MdlQuizAttempt {
        @JsonAlias(value = "SELF_ID")    // since KSQL publishes everything upper cased
        private Long self_id;
        @JsonAlias(value = "QUIZID") // since KSQL publishes everything upper cased
        private Long quizid;
        @JsonAlias(value = "ID") // since KSQL publishes everything upper cased
        private Long id; // is userid // attention - normally we would not do something like this, this is for KSQL-Show purposes
        @JsonAlias(value = "TIMESTART") // since KSQL publishes everything upper cased
        private Long timestart;
        @JsonAlias(value = "TIMEFINISH") // since KSQL publishes everything upper cased
        private Long timefinish;
        @JsonAlias(value = "TIMEMODIFIED") // since KSQL publishes everything upper cased
        private Long timemodified;
        @JsonAlias(value = "SUMGRADES") // since KSQL publishes everything upper cased
        private String sumgrades;

        public Long getUserId() {
            return this.id;
        }

        public Long getId() {
            return this.self_id;
        }


    }

    @Data
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    // since KSQL publishes everything upper cased
    public static class MdlQuiz {
        @JsonAlias(value = "ID") // since KSQL publishes everything upper cased
        private Long id;
        @JsonAlias(value = "COURSE") // since KSQL publishes everything upper cased
        private Long course;
        @JsonAlias(value = "NAME") // since KSQL publishes everything upper cased
        private String name;
        @JsonAlias(value = "INTRO") // since KSQL publishes everything upper cased
        private String intro;
        @JsonAlias(value = "INTROFORMAT") // since KSQL publishes everything upper cased
        private Integer introformat;
        @JsonAlias(value = "TIMEOPEN") // since KSQL publishes everything upper cased
        private Long timeopen;
        @JsonAlias(value = "TIMECLOSE") // since KSQL publishes everything upper cased
        private Long timeclose;
        @JsonAlias(value = "TIMELIMIT") // since KSQL publishes everything upper cased
        private Long timelimit;
        @JsonAlias(value = "OVERDUEHANDLING") // since KSQL publishes everything upper cased
        private String overduehandling;
        @JsonAlias(value = "GRACEPERIOD") // since KSQL publishes everything upper cased
        private Long graceperiod;
        @JsonAlias(value = "PREFERREDBEHAVIOR") // since KSQL publishes everything upper cased
        private Object preferredbehavior;
        @JsonAlias(value = "CANREDOQUESTIONS") // since KSQL publishes everything upper cased
        private Integer canredoquestions;
        @JsonAlias(value = "ATTEMPTS") // since KSQL publishes everything upper cased
        private Integer attempts;
        @JsonAlias(value = "ATTEMPTONLAST") // since KSQL publishes everything upper cased
        private Integer attemptonlast;
        @JsonAlias(value = "GRACEMETHOD") // since KSQL publishes everything upper cased
        private Integer gracemethod;
        @JsonAlias(value = "DECIMALPOINTS") // since KSQL publishes everything upper cased
        private Float decimalpoints;
        @JsonAlias(value = "TIMECREATED") // since KSQL publishes everything upper cased
        private Long timecreated;
        @JsonAlias(value = "TIMEMODIFIED") // since KSQL publishes everything upper cased
        private Long timemodified;
    }
}
