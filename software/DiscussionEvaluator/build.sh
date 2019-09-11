#!/bin/bash

# cd into working director
script_full_path=$(dirname "$0")
cd $script_full_path

echo "Calling Maven Install without Tests for Discussion Evaluator"
./mvnw -Dmaven.test.skip=true install

echo "Calling Docker build for discussion-evaluator:master"
docker build -t discussion-evaluator:master .

echo "done"

