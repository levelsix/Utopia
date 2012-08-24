#!/bin/bash

mvn install:install-file -DgroupId=org.elasticsearch -DartifactId=elasticsearch -Dpackaging=jar -Dversion=0.19.9 -Dfile=lib/elasticsearch-0.19.9.jar -DgeneratePom=true

