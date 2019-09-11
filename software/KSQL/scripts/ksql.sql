-- KSQL OPERATIONS

-- todo the datastructure definitions will be unnecessary as soon as data structures are in avro


-- consume stream directly from db cdc wrapped moodle quiz
CREATE STREAM mdl_quiz (before STRUCT<id BIGINT, course BIGINT, name STRING, intro STRING, introformat INTEGER, timeopen BIGINT, timeclose BIGINT,
    timelimit BIGINT, overduehandling STRING, graceperiod BIGINT, preferredbehavior STRING, canredoquestions INT, attempts INTEGER, attemptonlast BIGINT,
    grademethod INTEGER, decimalpoints DOUBLE, timecreated BIGINT, timemodified BIGINT>, after STRUCT<id BIGINT, course BIGINT, name STRING, intro STRING,
    introformat INTEGER, timeopen BIGINT, timeclose BIGINT, timelimit BIGINT, overduehandling STRING, graceperiod BIGINT, preferredbehavior STRING,
    canredoquestions INT, attempts INTEGER, attemptonlast BIGINT, grademethod INTEGER, decimalpoints DOUBLE, timecreated BIGINT, timemodified BIGINT>)
        WITH (KAFKA_TOPIC='__.goethe-universitaet-frankfurt.db.moodle.mdl_quiz', VALUE_FORMAT='JSON');

-- create a stream from wrapped quiz-stream, unpack cdcd wrapper and select key
CREATE STREAM __GOETHEUNIVERSITAETFRANKFURT_MDLQUIZ_UNPACKED_KEYED AS SELECT SELECTKEY(before->id, after->id) as id, after->course as course,
    after->name as name,after->intro as intro,after->introformat as introformat,after->timeopen as timeopen,after->timeclose as timeclose,
    after->timelimit as timelimit,after->overduehandling as overduehandling,after->graceperiod as graceperiod,after->preferredbehavior as preferredbehavior,
    after->canredoquestions as canredoquestions,after->attempts as attempts,after->attemptonlast as attemptonlast,after->grademethod as gracemethod,
    after->decimalpoints as decimalpoints,after->timecreated as timecreated,after->timemodified as timemodified
        FROM MDL_QUIZ
        PARTITION BY id;

-- consume stream directly from db cdc wrapped moodle quiz attempts
CREATE STREAM mdl_quiz_attempts (
    before STRUCT
        <id BIGINT, quiz BIGINT, userid BIGINT, attempt INTEGER, uniqueid BIGINT, layout STRING,
            currentpage INTEGER, preview INTEGER, state STRING, timestart BIGINT, timefinish BIGINT, timemodified BIGINT, timemodifiedoffline BIGINT,
            timecheckstate BIGINT, sumgrades STRING>,
    after STRUCT
        <id BIGINT, quiz BIGINT, userid BIGINT, attempt INTEGER, uniqueid BIGINT, layout STRING,
        currentpage INTEGER, preview INTEGER, state STRING, timestart BIGINT, timefinish BIGINT, timemodified BIGINT, timemodifiedoffline BIGINT,
        timecheckstate BIGINT, sumgrades STRING>
        )
        WITH (KAFKA_TOPIC='__.goethe-universitaet-frankfurt.db.moodle.mdl_quiz_attempts', VALUE_FORMAT='JSON');

-- create a stream from wrapped quiz-stream, unpack cdcd wrapper and select userid as key
CREATE STREAM __GOETHEUNIVERSITAETFRANKFURT_MDLQUIZATTEMPTS_UNPACKED_KEYED_BY_USER AS
    SELECT SELECTKEY(before->id,after->id) as self_id, after->quiz AS quizid, after->userid as id, after->timestart as timestart,
    after->timefinish as timefinish, after->timemodified as timemodified, after->sumgrades as sumgrades FROM mdl_quiz_attempts
        PARTITION BY id;

-- thought i use it for joins
-- CREATE TABLE mdl_user_unpacked (id BIGINT, username STRING, firstname STRING, lastname STRING, email STRING, lastlogin BIGINT, description STRING,
--     timemodified BIGINT)
--     WITH (KAFKA_TOPIC='__.goethe-universitaet-frankfurt.db.moodle.mdl_user.unpacked', VALUE_FORMAT='JSON', KEY='id');
