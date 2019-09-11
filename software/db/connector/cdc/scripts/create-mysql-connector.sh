#!/bin/bash

curl -X POST connect-debezium:8083/connectors -H "Content-Type: application/json" -d '{
  "name": "moodle-connector",
  "config": {
    "connector.class": "io.debezium.connector.mysql.MySqlConnector",
    "database.hostname": "mysql-db",
    "database.port": "3306",
    "database.user": "debezium",
    "database.password": "dbz",
    "database.server.id":"1",
    "database.server.name": "__.goethe-universitaet-frankfurt.db",
    "table.whitelist": "moodle.mdl_forum,moodle.mdl_forum_posts,moodle.mdl_user,moodle.mdl_quiz,moodle.mdl_quiz_attempts,moodle.mdl_quiz_feedback,moodle.mdl_quiz_grades",
    "database.history.kafka.bootstrap.servers": "kafka:29092",
    "database.history.kafka.topic": "__.goethe-universitaet-frankfurt.db.moodle.cdc.schema.changes.forumetc",
    "include.schema.changes": "false", 
    "value.converter":"org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable":"false",
    "key.converter":"org.apache.kafka.connect.json.JsonConverter",
    "key.converter.schemas.enable":"false"
  }
}'

