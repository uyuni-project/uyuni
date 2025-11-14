#!/usr/bin/env bash

WORKSPACE=$1
if [ -z "$WORKSPACE" ]; then
    echo "ERROR: No workspacefolder specified" 1>&2
    exit 1
fi

if [ ! -d "$WORKSPACE" ] || [ ! -x "$WORKSPACE" ]; then
    echo "ERROR: Directory $WORKSPACE does not exist or is not accessible" 1>&2
    exit 1
fi

cd $WORKSPACE

# Install the parent pom
mvn -f microservices/uyuni-java-parent --non-recursive install

# Install branding so we can compile spacewalk-java alone
mvn -f branding install

# Initialize spacewalk-java so we donwload all the dependencies
mvn -f java compile --non-recursive initialize

# Install frontend dependencies
npm --prefix web install