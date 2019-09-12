#!/bin/bash

# cd into working director
script_full_path=$(dirname "$0")
cd $script_full_path

echo "Calling Maven Install without Tests for Discussion Evaluator"
./mvnw install

echo "Calling Docker build for discussion-evaluator:master"
docker build -t toschio/bachelor-thesis.discussion-evaluator:master .

echo "done"

