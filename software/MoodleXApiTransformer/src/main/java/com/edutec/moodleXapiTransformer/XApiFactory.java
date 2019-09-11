package com.edutec.moodleXapiTransformer;


import com.edutec.moodleXapiTransformer.models.Moodle;
import com.edutec.moodleXapiTransformer.models.StatementDataWrapper;
import com.edutec.moodleXapiTransformer.models.xapimodels.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.streams.KeyValue;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.UUID;

/**
 * Factory for building XApiStatements
 */
@Service
@RequiredArgsConstructor
public class XApiFactory {

    private final Log logger = LogFactory.getLog(getClass());
    private final DateService dateService;

    /**
     * constructs a XApi Statement from the data in the StatementDataWrapper
     *
     * @param wrapper
     * @return
     */
    public KeyValue<String, Statement> constructStatement(StatementDataWrapper wrapper) {
        String messageKey = null;
        Statement statement = null;
        try {
            statement = new Statement();
            //
            // create a statement concerning a forum post
            if (wrapper.getMdlForumPost() != null && wrapper.getMdlForum() != null && wrapper.getMdlUser() != null) {
                String name = "gmdlpost" + wrapper.getMdlForumPost().getId();
                byte[] bytes = name.getBytes();
                statement.setId(UUID.nameUUIDFromBytes(bytes)); // todo this should be a bijective and simple function that identifies the original id and original system to which the id belongs; UUIDs use hash ...
                statement.setStored(dateService.now());
                statement.setActor(constructAgent(wrapper.getMdlUser()));
                statement.setTimestamp(dateService.convertToDateTime(wrapper.getMdlForumPost().getModified()));
                statement.setContext(ContextProviderAndFactory.of(wrapper.getMdlForum()));
                statement.setResult(constructResult(wrapper.getMdlForumPost()));
                statement.setVerb(VerbProviderAndFactory.of(wrapper.getMdlForumPost()));
                statement.setObject(constructStatementTarget(wrapper.getMdlForumPost()));
                messageKey = statement.getActor().getName();
            }
            //
            // create a statement concerning a quiz
            else if (wrapper.getMdlQuizAttempt() != null && wrapper.getMdlQuiz() != null && wrapper.getMdlUser() != null) {
                String name = "gmdlquiz" + wrapper.getMdlQuizAttempt().getId();
                byte[] bytes = name.getBytes();
                statement.setId(UUID.nameUUIDFromBytes(bytes)); // todo this should be a bijective and simple function that identifies the original id and original system to which the id belongs; UUIDs use hash ...
                statement.setStored(dateService.now());
                statement.setActor(constructAgent(wrapper.getMdlUser()));
                statement.setTimestamp(dateService.convertToDateTime(wrapper.getMdlQuizAttempt().getTimemodified()));
                statement.setVerb(VerbProviderAndFactory.of(wrapper.getMdlQuizAttempt()));
                statement.setContext(ContextProviderAndFactory.of(wrapper.getMdlQuiz()));
                messageKey = statement.getActor().getName();
            } else {
                return KeyValue.pair(null, null);
            }
            Assert.notNull(messageKey, "message key is null as statement has no id");
        } catch (NullPointerException | IllegalArgumentException | URISyntaxException e) {
            logger.error(e);
            return KeyValue.pair(null, null);
        }
        return KeyValue.pair(messageKey, statement);
    }

    private StatementTarget constructStatementTarget(Moodle.MdlForumPost mdlForumPost) {
        if (mdlForumPost.getParent() != 0) {
            String name = "gmdlpost" + mdlForumPost.getParent();
            byte[] bytes = name.getBytes();
            return new StatementRef(UUID.nameUUIDFromBytes(bytes));
        }
        return null;
    }


    private Result constructResult(Moodle.MdlForumPost mdlForumPost) {
        Result result = new Result();
        result.setResponse(mdlForumPost.getMessage());
        return result;
    }


    private Agent constructAgent(Moodle.MdlUser mdlUser) {
        Agent agent = new Agent();
        agent.setName(mdlUser.getFirstname() + " " + mdlUser.getLastname());
        agent.setMbox("mailto:" + mdlUser.getEmail());
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(agent.getMbox().getBytes());
            byte[] hash = digest.digest();
            agent.setMboxSHA1Sum(new String(Hex.encodeHex(hash)));
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        return agent;
    }

    /**
     * provides and generates xapi contexts
     */
    private static class ContextProviderAndFactory {

        static Context of(Object object) throws URISyntaxException {
            if (object instanceof Moodle.MdlForum) {
                return constructContext((Moodle.MdlForum) object);
            }
            if (object instanceof Moodle.MdlQuiz) {
                return constructContext((Moodle.MdlQuiz) object);
            }
            return null;
        }

        private static Context constructContext(Moodle.MdlForum mdlForum) throws URISyntaxException {
            LanguageMap categoryName = new LanguageMap();
            categoryName.put("name", mdlForum.getName());

            Activity category = new Activity();
            ActivityDefinition categoryActivityDefinition = new ActivityDefinition();
            categoryActivityDefinition.setType("context");
            categoryActivityDefinition.setName(categoryName);
            category.setDefinition(categoryActivityDefinition);
            category.setId("goethe-universitaet-frankfurt.moodle.forum." + mdlForum.getId());

            ContextActivities contextActivities = new ContextActivities();
            contextActivities.setCategory(Collections.singletonList(category));

            Context context = new Context();
            context.setPlatform("goethe-universitaet-frankfurt.moodle");
            context.setContextActivities(contextActivities);

            return context;
        }

        private static Context constructContext(Moodle.MdlQuiz quiz) throws URISyntaxException {
            LanguageMap categoryName = new LanguageMap();
            categoryName.put("name", quiz.getName());

            Activity category = new Activity();
            ActivityDefinition categoryActivityDefinition = new ActivityDefinition();
            categoryActivityDefinition.setName(categoryName);
            category.setDefinition(categoryActivityDefinition);
            category.setId("goethe-universitaet-frankfurt.moodle.forum." + quiz.getId());

            ContextActivities contextActivities = new ContextActivities();
            contextActivities.setCategory(Collections.singletonList(category));

            Context context = new Context();
            context.setPlatform("goethe-universitaet-frankfurt.moodle");
            context.setContextActivities(contextActivities);

            return context;
        }
    }

    /**
     * provides and generates xapi verbs
     */
    public static class VerbProviderAndFactory {

        public static Verb of(Object object) {
            if (object instanceof Moodle.MdlForumPost) {
                return constructVerb((Moodle.MdlForumPost) object);
            }
            if (object instanceof Moodle.MdlQuizAttempt) {
                return constructVerb((Moodle.MdlQuizAttempt) object);
            }
            return null;
        }

        private static Verb constructVerb(Moodle.MdlForumPost mdlForumPost) {
            Verb verb = null;
            if (mdlForumPost.getParent() == null || mdlForumPost.getParent().equals(0L)) {
                verb = VerbProviderAndFactory.posted();
            } else {
                verb = VerbProviderAndFactory.replied();
            }
            return verb;
        }

        private static Verb constructVerb(Moodle.MdlQuizAttempt mdlQuizAttempt) {
            return VerbProviderAndFactory.submitAssessment();
        }

        public static Verb replied() {
            return getVerb("replied");
        }

        public static Verb posted() {
            return getVerb("posted");
        }

        public static Verb accessAssessment() {
            return getVerb("access");
        }

        public static Verb submitAssessment() {
            return getVerb("submit");
        }

        private static Verb getVerb(String posted) {
            Verb verb = new Verb();
            try {
                verb.setId(new URI(posted));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            //        LanguageMap display = new LanguageMap();
            //        display.put("en-US", posted);
            //        verb.setDisplay(display);
            return verb;
        }
    }
}
