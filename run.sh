#!/bin/bash

java -jar \
  -Dserver.port=8081 \
  ./build/libs/school.hours-0.0.1-SNAPSHOT.jar > ~/school.hours.innexgo.com.txt &
