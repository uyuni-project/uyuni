#!/bin/bash

# Resolve libs and run tests
cd /manager/java/spacewalk-java
ant -f manager-build.xml ivy

ant -f manager-build.xml checkstyle

