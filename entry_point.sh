#!/bin/bash

./gradlew bootJar

cd build/libs/

java -jar scheduler-worker-0.0.1-SNAPSHOT.jar
