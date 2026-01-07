#!/bin/bash

# Resolve libs and run tests
cd /manager/java
ant -f manager-build.xml ivy

ant -f manager-build.xml checkstyle

