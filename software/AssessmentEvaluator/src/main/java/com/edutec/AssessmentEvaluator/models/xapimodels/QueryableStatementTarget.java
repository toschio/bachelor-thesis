/*
    Copyright 2013 Rustici Software

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.edutec.AssessmentEvaluator.models.xapimodels;

import java.io.IOException;

/**
 * Essentially empty interface to force type checking for objects of a statement that are queryable
 */
public interface QueryableStatementTarget extends StatementTarget {
    String toJSON(TCAPIVersion version) throws IOException;
}
