#!/bin/bash

# per deployment settings
# check out application.properties
# to see what env vars you need to export
source settings.sh

java -jar ./build/libs/school.hours-0.0.1-SNAPSHOT.jar > log.txt
