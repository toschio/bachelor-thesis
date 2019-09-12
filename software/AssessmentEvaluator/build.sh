#!/bin/bash

# cd into working director
script_full_path=$(dirname "$0")
cd $script_full_path

echo "Calling Maven Install without Tests for MoodleXApiTransformer"
echo "--------------------------------------------------------------"
./mvnw install

echo "Calling Docker build for moodle-xapi-transformer:master"
docker build -t toschio/bachelor-thesis.assessment-evaluator:master .

echo "done"

