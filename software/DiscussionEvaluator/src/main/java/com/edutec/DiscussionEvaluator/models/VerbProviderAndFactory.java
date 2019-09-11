package com.edutec.DiscussionEvaluator.models;

import com.edutec.DiscussionEvaluator.models.xapimodels.Verb;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * provides and generates xapi verbs
 */
public class VerbProviderAndFactory {


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

