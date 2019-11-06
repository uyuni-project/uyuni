#! /bin/sh

HERE=`dirname $0`
. $HERE/VERSION
GITROOT=`readlink -f $HERE/../../../`

INITIAL_CMD="/manager/susemanager-utils/testing/automation/initial-objects.sh"
CMD="/manager/susemanager-utils/testing/docker/scripts/schema_migration_test_oracle-31to41.sh"
CLEAN_CMD="/manager/susemanager-utils/testing/automation/clean-objects.sh"

docker pull $REGISTRY/$ORACLE_CONTAINER
docker run --privileged --rm=true -v "$GITROOT:/manager" $REGISTRY/$ORACLE_CONTAINER /bin/bash -c "${INITIAL_CMD}; ${CMD}; RET=\${?}; ${CLEAN_CMD} && exit \${RET}"
