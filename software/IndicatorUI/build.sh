#!/bin/bash

# cd into working director
script_full_path=$(dirname "$0")
cd $script_full_path

echo "Calling Docker build for indicator-ui:master"
docker build -t toschio/bachelor-thesis.indicator-ui:master .

echo "done"

