#!/bin/bash

# cd into working director
script_full_path=$(dirname "$0")
cd $script_full_path

echo "Calling Maven Install without Tests for Serving application"
echo "--------------------------------------------------------------"

./mvnw install

echo "Calling Docker build for serving:master"
docker build -t toschio/bachelor-thesis.serving:master .

echo "done"


