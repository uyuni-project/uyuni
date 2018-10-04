#! /bin/sh

HERE=`dirname $0`
. $HERE/VERSION
GITROOT=`readlink -f $HERE/../../../`

# File created by Gitarro with info about a PR, it only exists when we are testing a PR
GITARRO_JSON="${GITROOT}/../.gitarro_pr.json"

# Make it available for the container
cp ${GITARRO_JSON} ${GITROOT}
GITARRO_JSON_CONTAINER="/manager/.gitarro_pr.json"

# we need a special (old) baseimage to migrate to current schema
docker pull $REGISTRY/$PGSQL_CONTAINER

# Check if we run for PR or not
if [ -f ${GITARRO_JSON} ]; then
  echo "Running from PR"
  IDEMPOTENCY_PARAMS=" -p ${GITARRO_JSON_CONTAINER}"
else 
  echo "Running from Branch"
  IDEMPOTENCY_PARAMS=" -v ${IDEMPOTENCY_SCHEMA_BASE_VERSION}"  
fi

MIGRATION_TEST='/manager/susemanager-utils/testing/docker/scripts/schema_migration_test_pgsql-21to31.sh'
IDEMPOTENCY_TEST="/manager/susemanager-utils/testing/docker/scripts/schema_idempotency_test_pgsql.py ${IDEMPOTENCY_PARAMS}"

docker run --privileged --rm=true -v "$GITROOT:/manager" $REGISTRY/$PGSQL_CONTAINER /bin/bash -c "${MIGRATION_TEST} && ${IDEMPOTENCY_TEST}"
