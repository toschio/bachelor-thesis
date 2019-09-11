package com.edutec.serving.models.xapimodels;

import java.io.IOException;
import java.util.HashMap;

public interface StatementsQueryInterface {
    TCAPIVersion getVersion();

    HashMap<String, String> toParameterMap() throws IOException;
}
