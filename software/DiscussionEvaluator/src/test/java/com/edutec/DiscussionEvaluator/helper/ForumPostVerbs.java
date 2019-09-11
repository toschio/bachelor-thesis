package com.edutec.DiscussionEvaluator.helper;

import com.edutec.DiscussionEvaluator.models.xapimodels.Verb;

import java.net.URI;
import java.net.URISyntaxException;

public class ForumPostVerbs {

    public static Verb replied() {
        return getVerb("replied");
    }

    public static Verb posted() {
        return getVerb("posted");
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
