///*
// * Copyright 2019 Confluent Inc.
// *
// * Licensed under the Confluent Community License (the "License"); you may not use
// * this file except in compliance with the License.  You may obtain a copy of the
// * License at
// *
// * http://www.confluent.io/confluent-community-license
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations under the License.
// */
//
//package com.edutec;
//
//import org.apache.log4j.Logger;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.Arguments;
//import org.junit.jupiter.params.provider.MethodSource;
//
//import java.io.IOException;
//import java.util.stream.Stream;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * Example class that demonstrates how to unit test UDFs.
// */
//public class SelectCdcIdUdfTests {
//  private static Logger log = Logger.getLogger(OtherIfNull.class);
//  private static final String ID_REPLACER_1 = "\"id\": 1";
//  private static final String ID_REPLACER_2 = "\"id\": 2";
//  private static final String ID_REPLACER_5 = "\"id\": 5";
//  private static final String ID_REPLACER_992 = "\"id\": 992";
//  private static final String JSON_BEFORE = "{\n" +
//          "  \"before\": null,\n" +
//          "  \"after\": {\n" +
//          "    ";
//  private static final String JSON_AFTER = ",\n \"course\": 1,\n \"name\": \"A quiz\",\n \"intro\": \"<p>This is a qiuiz<br></p>\",\n" +
//          "    \"introformat\": 1,\n" +
//          "    \"timeopen\": 1566252000,\n" +
//          "    \"timeclose\": 1567139040,\n" +
//          "    \"timelimit\": 0,\n" +
//          "    \"overduehandling\": \"autosubmit\",\n" +
//          "    \"graceperiod\": 0,\n" +
//          "    \"preferredbehaviour\": \"deferredfeedback\",\n" +
//          "    \"canredoquestions\": 0,\n" +
//          "    \"attempts\": 0,\n" +
//          "    \"attemptonlast\": 0,\n" +
//          "    \"grademethod\": 1,\n" +
//          "    \"decimalpoints\": 2,\n" +
//          "    \"questiondecimalpoints\": -1,\n" +
//          "    \"reviewattempt\": 69904,\n" +
//          "    \"reviewcorrectness\": 4368,\n" +
//          "    \"reviewmarks\": 4368,\n" +
//          "    \"reviewspecificfeedback\": 4368,\n" +
//          "    \"reviewgeneralfeedback\": 4368,\n" +
//          "    \"reviewrightanswer\": 4368,\n" +
//          "    \"reviewoverallfeedback\": 4368,\n" +
//          "    \"questionsperpage\": 1,\n" +
//          "    \"navmethod\": \"free\",\n" +
//          "    \"shuffleanswers\": 1,\n" +
//          "    \"sumgrades\": \"AYag\",\n" +
//          "    \"grade\": \"D0JA\",\n" +
//          "    \"timecreated\": 0,\n" +
//          "    \"timemodified\": 1566361528,\n" +
//          "    \"password\": \"\",\n" +
//          "    \"subnet\": \"\",\n" +
//          "    \"browsersecurity\": \"-\",\n" +
//          "    \"delay1\": 0,\n" +
//          "    \"delay2\": 0,\n" +
//          "    \"showuserpicture\": 0,\n" +
//          "    \"showblocks\": 0,\n" +
//          "    \"completionattemptsexhausted\": 0,\n" +
//          "    \"completionpass\": 0,\n" +
//          "    \"allowofflineattempts\": 0\n" +
//          "  },\n" +
//          "  \"source\": {\n" +
//          "    \"version\": \"0.9.5.Final\",\n" +
//          "    \"connector\": \"mysql\",\n" +
//          "    \"name\": \"__.goethe-universitaet-frankfurt.db\",\n" +
//          "    \"server_id\": 0,\n" +
//          "    \"ts_sec\": 0,\n" +
//          "    \"gtid\": null,\n" +
//          "    \"file\": \"mysql-bin.000004\",\n" +
//          "    \"pos\": 4249,\n" +
//          "    \"row\": 0,\n" +
//          "    \"snapshot\": true,\n" +
//          "    \"thread\": null,\n" +
//          "    \"db\": \"moodle\",\n" +
//          "    \"table\": \"mdl_quiz\",\n" +
//          "    \"query\": null\n" +
//          "  },\n" +
//          "  \"op\": \"c\",\n" +
//          "  \"ts_ms\": 1566367103436\n" +
//          "}\n";
//
//  private static String JSON_AFTER_NULL = "{\n" +
//          "  \"after\": null,\n" +
//          "  \"before\": {\n" +
//          "    \"id\": 55,\n" +
//          "    \"course\": 1\n" +
//          "  },\n" +
//          "  \"source\": {\n" +
//          "    \"version\": \"0.9.5.Final\",\n" +
//          "    \"query\": null\n" +
//          "  },\n" +
//          "  \"op\": \"c\",\n" +
//          "  \"ts_ms\": 1566367103436\n" +
//          "}\n";
//  private static Stream<Arguments> stream() {
//    return Stream.of(Arguments.of(
//            JSON_BEFORE + ID_REPLACER_1 + JSON_AFTER, 1L),
//            Arguments.of(JSON_BEFORE + ID_REPLACER_2 + JSON_AFTER, 2L),
//            Arguments.of(JSON_BEFORE + ID_REPLACER_5 + JSON_AFTER, 5L),
//            Arguments.of(JSON_BEFORE + ID_REPLACER_992 + JSON_AFTER, 992L),
//            Arguments.of(JSON_AFTER_NULL, 55L)
//            );
//  }
//
//  @ParameterizedTest
//  @MethodSource(value = "stream")
//  void selectId(final String source, final Long expected) throws IOException {
//
//    final OtherIfNull selector = new OtherIfNull();
//    assertNotNull(selector.selectId(source));
//    final Long actualResult = selector.selectId(source);
//    assertEquals(expected, actualResult, " id should equal " + expected);
//  }
//}
