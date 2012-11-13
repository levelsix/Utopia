#!/bin/bash

mvn install:install-file -DgroupId=json-java -DartifactId=json-java -Dpackaging=jar -Dversion=1.0 -Dfile=lib/json-java.jar -DgeneratePom=true
