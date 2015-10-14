#!/bin/bash

# Resolve libs and run tests
cd /manager/java
ant resolve-ivy

ant -f manager-build.xml checkstyle

